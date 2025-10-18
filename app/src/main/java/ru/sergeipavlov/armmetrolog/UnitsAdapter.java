package ru.sergeipavlov.armmetrolog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class UnitsAdapter extends RecyclerView.Adapter<UnitsAdapter.UnitViewHolder> {

    private final List<String> units;

    UnitsAdapter(@NonNull List<String> units) {
        this.units = units;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_main_menu, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        holder.bind(units.get(position));
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    static class UnitViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;

        UnitViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.menu_item_title);
        }

        void bind(@NonNull String title) {
            titleView.setText(title);
            itemView.setOnClickListener(null);
        }
    }
}
