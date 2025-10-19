package ru.sergeipavlov.armmetrolog;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

public class UnitsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        MaterialToolbar toolbar = findViewById(R.id.units_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.units_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new UnitsAdapter(getUnitsItems(), this::onUnitSelected));
    }

    @NonNull
    private List<String> getUnitsItems() {
        return Arrays.asList(
                getString(R.string.units_temperature),
                getString(R.string.units_time),
                getString(R.string.units_speed)
        );
    }
    private void onUnitSelected(@NonNull String unit) {
        if (unit.equals(getString(R.string.units_temperature))) {
            startActivity(new Intent(this, TemperatureActivity.class));
        } else if (unit.equals(getString(R.string.units_time))) {
            startActivity(new Intent(this, TimeActivity.class));
        }
    }
}
