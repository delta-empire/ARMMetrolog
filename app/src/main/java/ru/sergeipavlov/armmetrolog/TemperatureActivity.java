package ru.sergeipavlov.armmetrolog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class TemperatureActivity extends AppCompatActivity {

    private TextInputEditText kelvinInput;
    private TextInputEditText celsiusInput;
    private TextInputEditText fahrenheitInput;
    private TextInputEditText rankineInput;
    private TextInputEditText reaumurInput;

    private boolean isUpdating;

    private enum Unit {
        KELVIN,
        CELSIUS,
        FAHRENHEIT,
        RANKINE,
        REAUMUR
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        MaterialToolbar toolbar = findViewById(R.id.temperature_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        kelvinInput = findViewById(R.id.temperature_kelvin_input);
        celsiusInput = findViewById(R.id.temperature_celsius_input);
        fahrenheitInput = findViewById(R.id.temperature_fahrenheit_input);
        rankineInput = findViewById(R.id.temperature_rankine_input);
        reaumurInput = findViewById(R.id.temperature_reaumur_input);

        setupSymbolDialog(R.id.temperature_kelvin_symbol, R.string.temperature_kelvin, R.string.temperature_kelvin_description);
        setupSymbolDialog(R.id.temperature_celsius_symbol, R.string.temperature_celsius, R.string.temperature_celsius_description);
        setupSymbolDialog(R.id.temperature_fahrenheit_symbol, R.string.temperature_fahrenheit, R.string.temperature_fahrenheit_description);
        setupSymbolDialog(R.id.temperature_rankine_symbol, R.string.temperature_rankine, R.string.temperature_rankine_description);
        setupSymbolDialog(R.id.temperature_reaumur_symbol, R.string.temperature_reaumur, R.string.temperature_reaumur_description);

        updateTemperatures(Unit.KELVIN, 0.0, 3, null, -1, -1);

        kelvinInput.addTextChangedListener(createWatcher(Unit.KELVIN));
        celsiusInput.addTextChangedListener(createWatcher(Unit.CELSIUS));
        fahrenheitInput.addTextChangedListener(createWatcher(Unit.FAHRENHEIT));
        rankineInput.addTextChangedListener(createWatcher(Unit.RANKINE));
        reaumurInput.addTextChangedListener(createWatcher(Unit.REAUMUR));
    }

    private void setupSymbolDialog(int viewId, @StringRes int titleRes, @StringRes int messageRes) {
        TextView symbolView = findViewById(viewId);
        symbolView.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(android.R.string.ok, null)
                .show());
    }

    private TextWatcher createWatcher(@NonNull Unit unit) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdating) {
                    return;
                }

                TextInputEditText sourceEditText = getEditText(unit);
                if (sourceEditText == null) {
                    return;
                }

                int selectionStart = sourceEditText.getSelectionStart();
                int selectionEnd = sourceEditText.getSelectionEnd();

                String rawValue = editable.toString();
                String value = rawValue.trim();
                if (isIncompleteNumber(value)) {
                    return;
                }

                boolean treatAsZero = value.isEmpty();
                double parsedValue = 0.0;
                if (!treatAsZero) {
                    try {
                        parsedValue = Double.parseDouble(value.replace(',', '.'));
                    } catch (NumberFormatException exception) {
                        return;
                    }
                }

                int fractionDigits = treatAsZero
                        ? 3
                        : Math.max(getFractionDigits(value), 3);
                CharSequence sourceTextOverride = treatAsZero ? rawValue : null;

                updateTemperatures(unit, parsedValue, fractionDigits, sourceTextOverride, selectionStart, selectionEnd);
            }
        };
    }

    private boolean isIncompleteNumber(@NonNull String value) {
        return "-".equals(value)
                || ".".equals(value)
                || ",".equals(value)
                || "-.".equals(value)
                || "-,".equals(value);
    }

    private int getFractionDigits(@NonNull String value) {
        int separatorIndex = Math.max(value.lastIndexOf('.'), value.lastIndexOf(','));
        if (separatorIndex < 0) {
            return 0;
        }
        return value.length() - separatorIndex - 1;
    }

    private void updateTemperatures(@NonNull Unit sourceUnit, double value, int fractionDigits,
                                    @Nullable CharSequence sourceTextOverride, int selectionStart, int selectionEnd) {
        double kelvin = toKelvin(sourceUnit, value);
        double celsius = kelvinToCelsius(kelvin);
        double fahrenheit = kelvinToFahrenheit(kelvin);
        double rankine = kelvinToRankine(kelvin);
        double reaumur = kelvinToReaumur(kelvin);

        isUpdating = true;
        String formatPattern = "%1$." + fractionDigits + "f";

        updateEditText(kelvinInput, kelvin, formatPattern, sourceUnit == Unit.KELVIN, sourceTextOverride);
        updateEditText(celsiusInput, celsius, formatPattern, sourceUnit == Unit.CELSIUS, sourceTextOverride);
        updateEditText(fahrenheitInput, fahrenheit, formatPattern, sourceUnit == Unit.FAHRENHEIT, sourceTextOverride);
        updateEditText(rankineInput, rankine, formatPattern, sourceUnit == Unit.RANKINE, sourceTextOverride);
        updateEditText(reaumurInput, reaumur, formatPattern, sourceUnit == Unit.REAUMUR, sourceTextOverride);

        TextInputEditText sourceEditText = getEditText(sourceUnit);
        if (sourceEditText != null) {
            int length = sourceEditText.getText() != null ? sourceEditText.getText().length() : 0;
            int start = selectionStart < 0 ? length : Math.min(selectionStart, length);
            int end = selectionEnd < 0 ? start : Math.min(selectionEnd, length);
            sourceEditText.setSelection(start, end);
        }

        isUpdating = false;
    }

    private void updateEditText(@NonNull TextInputEditText editText, double value,
                                @NonNull String formatPattern, boolean isSource,
                                @Nullable CharSequence sourceTextOverride) {
        if (isSource && sourceTextOverride != null) {
            editText.setText(sourceTextOverride);
        } else {
            setFormattedText(editText, value, formatPattern);
        }
    }

    private void setFormattedText(@NonNull TextInputEditText editText, double value, @NonNull String formatPattern) {
        String formatted = String.format(Locale.US, formatPattern, value);
        editText.setText(formatted);
    }

    private TextInputEditText getEditText(@NonNull Unit unit) {
        switch (unit) {
            case KELVIN:
                return kelvinInput;
            case CELSIUS:
                return celsiusInput;
            case FAHRENHEIT:
                return fahrenheitInput;
            case RANKINE:
                return rankineInput;
            case REAUMUR:
                return reaumurInput;
            default:
                return null;
        }
    }

    private double toKelvin(@NonNull Unit unit, double value) {
        switch (unit) {
            case KELVIN:
                return value;
            case CELSIUS:
                return value + 273.15;
            case FAHRENHEIT:
                return (value + 459.67) * 5.0 / 9.0;
            case RANKINE:
                return value * 5.0 / 9.0;
            case REAUMUR:
                return (value * 5.0 / 4.0) + 273.15;
            default:
                return value;
        }
    }

    private double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    private double kelvinToFahrenheit(double kelvin) {
        return kelvin * 9.0 / 5.0 - 459.67;
    }

    private double kelvinToRankine(double kelvin) {
        return kelvin * 9.0 / 5.0;
    }

    private double kelvinToReaumur(double kelvin) {
        return (kelvin - 273.15) * 4.0 / 5.0;
    }
}
