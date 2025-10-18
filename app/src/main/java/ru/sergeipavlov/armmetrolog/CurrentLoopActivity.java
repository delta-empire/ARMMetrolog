package ru.sergeipavlov.armmetrolog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrentLoopActivity extends AppCompatActivity {

    private TextInputEditText physicalStartInput;
    private TextInputEditText physicalEndInput;
    private TextInputEditText physicalValueInput;
    private TextInputEditText signalStartInput;
    private TextInputEditText signalEndInput;
    private TextInputEditText signalValueInput;
    private Spinner scaleTypeSpinner;

    private boolean isUpdating;
    private CurrentLoopScaleType currentScaleType = CurrentLoopScaleType.LINEAR;

    private final DecimalFormat valueFormatter;

    public CurrentLoopActivity() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        valueFormatter = new DecimalFormat("0.###", symbols);
        valueFormatter.setGroupingUsed(false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_loop);

        MaterialToolbar toolbar = findViewById(R.id.current_loop_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        bindViews();
        setupSpinner();
        initializeDefaultValues();
        setupTextWatchers();
        recalculatePhysicalFromSignal();
    }

    private void bindViews() {
        physicalStartInput = findViewById(R.id.physical_start_input);
        physicalEndInput = findViewById(R.id.physical_end_input);
        physicalValueInput = findViewById(R.id.physical_value_input);
        signalStartInput = findViewById(R.id.signal_start_input);
        signalEndInput = findViewById(R.id.signal_end_input);
        signalValueInput = findViewById(R.id.signal_value_input);
        scaleTypeSpinner = findViewById(R.id.scale_type_spinner);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.current_loop_scale_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scaleTypeSpinner.setAdapter(adapter);
        scaleTypeSpinner.setSelection(CurrentLoopScaleType.LINEAR.ordinal());
        scaleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentScaleType = CurrentLoopScaleType.values()[position];
                recalculatePhysicalFromSignal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void initializeDefaultValues() {
        isUpdating = true;
        physicalStartInput.setText(formatValue(0));
        physicalEndInput.setText(formatValue(100));
        physicalValueInput.setText(formatValue(50));
        signalStartInput.setText(formatValue(4));
        signalEndInput.setText(formatValue(20));
        signalValueInput.setText(formatValue(12));
        isUpdating = false;
    }

    private void setupTextWatchers() {
        TextWatcher physicalValueWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }
                recalculateSignalFromPhysical();
            }
        };
        physicalValueInput.addTextChangedListener(physicalValueWatcher);

        TextWatcher recalculatePhysicalWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }
                recalculatePhysicalFromSignal();
            }
        };
        physicalStartInput.addTextChangedListener(recalculatePhysicalWatcher);
        physicalEndInput.addTextChangedListener(recalculatePhysicalWatcher);
        signalStartInput.addTextChangedListener(recalculatePhysicalWatcher);
        signalEndInput.addTextChangedListener(recalculatePhysicalWatcher);
        signalValueInput.addTextChangedListener(recalculatePhysicalWatcher);
    }

    private void recalculateSignalFromPhysical() {
        Double scv = getInputValue(physicalValueInput, null);
        if (scv == null) {
            return;
        }
        double scs = getInputValue(physicalStartInput, 0.0);
        double sce = getInputValue(physicalEndInput, 100.0);
        double sgs = getInputValue(signalStartInput, 0.0);
        double sge = getInputValue(signalEndInput, 20.0);

        double result = currentScaleType.toSignal(scv, scs, sce, sgs, sge);
        applyResult(signalValueInput, result);
    }

    private void recalculatePhysicalFromSignal() {
        Double sgv = getInputValue(signalValueInput, null);
        if (sgv == null) {
            return;
        }
        double scs = getInputValue(physicalStartInput, 0.0);
        double sce = getInputValue(physicalEndInput, 100.0);
        double sgs = getInputValue(signalStartInput, 0.0);
        double sge = getInputValue(signalEndInput, 20.0);

        double result = currentScaleType.toPhysical(sgv, scs, sce, sgs, sge);
        applyResult(physicalValueInput, result);
    }

    private void applyResult(@NonNull TextInputEditText target, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return;
        }
        double rounded = Math.round(value * 1000d) / 1000d;
        String formatted = formatValue(rounded);
        Editable current = target.getText();
        if (current != null && formatted.equals(current.toString())) {
            return;
        }
        isUpdating = true;
        target.setText(formatted);
        target.setSelection(formatted.length());
        isUpdating = false;
    }

    private String formatValue(double value) {
        return valueFormatter.format(value);
    }

    @Nullable
    private Double getInputValue(@NonNull TextInputEditText editText, @Nullable Double defaultValue) {
        Editable text = editText.getText();
        if (text == null) {
            return defaultValue;
        }
        String raw = text.toString().trim();
        if (raw.isEmpty()) {
            return defaultValue;
        }
        if (raw.equals("-") || raw.equals(".") || raw.equals("-.") || raw.equals(",") || raw.equals("-,")) {
            return null;
        }
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private enum CurrentLoopScaleType {
        LINEAR {
            @Override
            double toSignal(double scv, double scs, double sce, double sgs, double sge) {
                double denominator = sce - scs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (scv - scs) / denominator;
                return ratio * (sge - sgs) + sgs;
            }

            @Override
            double toPhysical(double sgv, double scs, double sce, double sgs, double sge) {
                double denominator = sge - sgs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (sgv - sgs) / denominator;
                return ratio * (sce - scs) + scs;
            }
        },
        LINEAR_DESCENDING {
            @Override
            double toSignal(double scv, double scs, double sce, double sgs, double sge) {
                double denominator = sce - scs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (scv - scs) / denominator;
                return ratio * (sgs - sge) + sge;
            }

            @Override
            double toPhysical(double sgv, double scs, double sce, double sgs, double sge) {
                double denominator = sgs - sge;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (sgv - sge) / denominator;
                return ratio * (sce - scs) + scs;
            }
        },
        QUADRATIC {
            @Override
            double toSignal(double scv, double scs, double sce, double sgs, double sge) {
                double denominator = sce - scs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (scv - scs) / denominator;
                return Math.pow(ratio, 2) * (sge - sgs) + sgs;
            }

            @Override
            double toPhysical(double sgv, double scs, double sce, double sgs, double sge) {
                double denominator = sge - sgs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (sgv - sgs) / denominator;
                if (ratio < 0) {
                    return Double.NaN;
                }
                return Math.sqrt(ratio) * (sce - scs) + scs;
            }
        },
        QUADRATIC_DESCENDING {
            @Override
            double toSignal(double scv, double scs, double sce, double sgs, double sge) {
                double denominator = sce - scs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (scv - scs) / denominator;
                return Math.pow(ratio, 2) * (sgs - sge) + sge;
            }

            @Override
            double toPhysical(double sgv, double scs, double sce, double sgs, double sge) {
                double denominator = sgs - sge;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (sgv - sge) / denominator;
                if (ratio < 0) {
                    return Double.NaN;
                }
                return Math.sqrt(ratio) * (sce - scs) + scs;
            }
        },
        ROOT {
            @Override
            double toSignal(double scv, double scs, double sce, double sgs, double sge) {
                double denominator = sce - scs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (scv - scs) / denominator;
                if (ratio < 0) {
                    return Double.NaN;
                }
                return Math.sqrt(ratio) * (sge - sgs) + sgs;
            }

            @Override
            double toPhysical(double sgv, double scs, double sce, double sgs, double sge) {
                double denominator = sge - sgs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (sgv - sgs) / denominator;
                return Math.pow(ratio, 2) * (sce - scs) + scs;
            }
        },
        ROOT_DESCENDING {
            @Override
            double toSignal(double scv, double scs, double sce, double sgs, double sge) {
                double denominator = sce - scs;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (scv - scs) / denominator;
                if (ratio < 0) {
                    return Double.NaN;
                }
                return Math.sqrt(ratio) * (sgs - sge) + sge;
            }

            @Override
            double toPhysical(double sgv, double scs, double sce, double sgs, double sge) {
                double denominator = sgs - sge;
                if (denominator == 0) {
                    return Double.NaN;
                }
                double ratio = (sgv - sge) / denominator;
                return Math.pow(ratio, 2) * (sce - scs) + scs;
            }
        };

        abstract double toSignal(double scv, double scs, double sce, double sgs, double sge);

        abstract double toPhysical(double sgv, double scs, double sce, double sgs, double sge);
    }
}
