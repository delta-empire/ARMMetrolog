package ru.sergeipavlov.armmetrolog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class TimeActivity extends AppCompatActivity {

    private TextInputEditText secondInput;
    private TextInputEditText microsecondInput;
    private TextInputEditText millisecondInput;
    private TextInputEditText minuteInput;
    private TextInputEditText hourInput;
    private TextInputEditText dayInput;
    private TextInputEditText weekInput;
    private TextInputEditText monthInput;
    private TextInputEditText yearInput;
    private TextInputEditText centuryInput;
    private TextInputEditText millenniumInput;

    private boolean isUpdating;

    private enum Unit {
        SECOND,
        MICROSECOND,
        MILLISECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        CENTURY,
        MILLENNIUM
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        MaterialToolbar toolbar = findViewById(R.id.time_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        secondInput = findViewById(R.id.time_second_input);
        microsecondInput = findViewById(R.id.time_microsecond_input);
        millisecondInput = findViewById(R.id.time_millisecond_input);
        minuteInput = findViewById(R.id.time_minute_input);
        hourInput = findViewById(R.id.time_hour_input);
        dayInput = findViewById(R.id.time_day_input);
        weekInput = findViewById(R.id.time_week_input);
        monthInput = findViewById(R.id.time_month_input);
        yearInput = findViewById(R.id.time_year_input);
        centuryInput = findViewById(R.id.time_century_input);
        millenniumInput = findViewById(R.id.time_millennium_input);

        updateTimes(Unit.SECOND, 0.0, 3, -1, -1);

        secondInput.addTextChangedListener(createWatcher(Unit.SECOND));
        microsecondInput.addTextChangedListener(createWatcher(Unit.MICROSECOND));
        millisecondInput.addTextChangedListener(createWatcher(Unit.MILLISECOND));
        minuteInput.addTextChangedListener(createWatcher(Unit.MINUTE));
        hourInput.addTextChangedListener(createWatcher(Unit.HOUR));
        dayInput.addTextChangedListener(createWatcher(Unit.DAY));
        weekInput.addTextChangedListener(createWatcher(Unit.WEEK));
        monthInput.addTextChangedListener(createWatcher(Unit.MONTH));
        yearInput.addTextChangedListener(createWatcher(Unit.YEAR));
        centuryInput.addTextChangedListener(createWatcher(Unit.CENTURY));
        millenniumInput.addTextChangedListener(createWatcher(Unit.MILLENNIUM));
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
                if (value.isEmpty() || isIncompleteNumber(value)) {
                    return;
                }

                double parsedValue;
                try {
                    parsedValue = Double.parseDouble(value.replace(',', '.'));
                } catch (NumberFormatException exception) {
                    return;
                }

                int fractionDigits = Math.max(getFractionDigits(value), 3);

                updateTimes(unit, parsedValue, fractionDigits, selectionStart, selectionEnd);
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

    private void updateTimes(@NonNull Unit sourceUnit, double value, int fractionDigits,
                              int selectionStart, int selectionEnd) {
        double seconds = toSeconds(sourceUnit, value);
        double microseconds = secondsToMicroseconds(seconds);
        double milliseconds = secondsToMilliseconds(seconds);
        double minutes = secondsToMinutes(seconds);
        double hours = secondsToHours(seconds);
        double days = secondsToDays(seconds);
        double weeks = secondsToWeeks(seconds);
        double months = secondsToMonths(seconds);
        double years = secondsToYears(seconds);
        double centuries = secondsToCenturies(seconds);
        double millennia = secondsToMillennia(seconds);

        isUpdating = true;
        String formatPattern = "%1$." + fractionDigits + "f";

        updateEditText(secondInput, seconds, formatPattern);
        updateEditText(microsecondInput, microseconds, formatPattern);
        updateEditText(millisecondInput, milliseconds, formatPattern);
        updateEditText(minuteInput, minutes, formatPattern);
        updateEditText(hourInput, hours, formatPattern);
        updateEditText(dayInput, days, formatPattern);
        updateEditText(weekInput, weeks, formatPattern);
        updateEditText(monthInput, months, formatPattern);
        updateEditText(yearInput, years, formatPattern);
        updateEditText(centuryInput, centuries, formatPattern);
        updateEditText(millenniumInput, millennia, formatPattern);

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
                                @NonNull String formatPattern) {
        setFormattedText(editText, value, formatPattern);
    }

    private void setFormattedText(@NonNull TextInputEditText editText, double value,
                                  @NonNull String formatPattern) {
        String formatted = String.format(Locale.US, formatPattern, value);
        editText.setText(formatted);
    }

    @Nullable
    private TextInputEditText getEditText(@NonNull Unit unit) {
        switch (unit) {
            case SECOND:
                return secondInput;
            case MICROSECOND:
                return microsecondInput;
            case MILLISECOND:
                return millisecondInput;
            case MINUTE:
                return minuteInput;
            case HOUR:
                return hourInput;
            case DAY:
                return dayInput;
            case WEEK:
                return weekInput;
            case MONTH:
                return monthInput;
            case YEAR:
                return yearInput;
            case CENTURY:
                return centuryInput;
            case MILLENNIUM:
                return millenniumInput;
            default:
                return null;
        }
    }

    private double toSeconds(@NonNull Unit unit, double value) {
        switch (unit) {
            case SECOND:
                return value;
            case MICROSECOND:
                return value / 1_000_000.0;
            case MILLISECOND:
                return value / 1_000.0;
            case MINUTE:
                return value * 60.0;
            case HOUR:
                return value * 3_600.0;
            case DAY:
                return value * 86_400.0;
            case WEEK:
                return value * 604_800.0;
            case MONTH:
                return value * 2_628_000.0;
            case YEAR:
                return value * 31_536_000.0;
            case CENTURY:
                return value * 3_153_600_000.0;
            case MILLENNIUM:
                return value * 31_536_000_000.0;
            default:
                return value;
        }
    }

    private double secondsToMicroseconds(double seconds) {
        return seconds * 1_000_000.0;
    }

    private double secondsToMilliseconds(double seconds) {
        return seconds * 1_000.0;
    }

    private double secondsToMinutes(double seconds) {
        return seconds / 60.0;
    }

    private double secondsToHours(double seconds) {
        return seconds / 3_600.0;
    }

    private double secondsToDays(double seconds) {
        return seconds / 86_400.0;
    }

    private double secondsToWeeks(double seconds) {
        return seconds / 604_800.0;
    }

    private double secondsToMonths(double seconds) {
        return seconds / 2_628_000.0;
    }

    private double secondsToYears(double seconds) {
        return seconds / 31_536_000.0;
    }

    private double secondsToCenturies(double seconds) {
        return seconds / 3_153_600_000.0;
    }

    private double secondsToMillennia(double seconds) {
        return seconds / 31_536_000_000.0;
    }
}
