package ru.sergeipavlov.armmetrolog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class UnitsAdapter extends RecyclerView.Adapter<UnitsAdapter.UnitViewHolder> {

    interface OnUnitClickListener {
        void onUnitClick(@NonNull String unitTitle);
    }

    private final List<String> units;
    private final OnUnitClickListener clickListener;

    UnitsAdapter(@NonNull List<String> units, @NonNull OnUnitClickListener clickListener) {
        this.units = units;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_main_menu, parent, false);
        return new UnitViewHolder(view, clickListener);
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
        private final OnUnitClickListener clickListener;

        UnitViewHolder(@NonNull View itemView, @NonNull OnUnitClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;
            titleView = itemView.findViewById(R.id.menu_item_title);
        }

        void bind(@NonNull String title) {
            titleView.setText(title);
            itemView.setOnClickListener(v -> clickListener.onUnitClick(title));
        }
    }
}
