package ru.sergeipavlov.armmetrolog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.MenuItemViewHolder> {

    private final List<String> items;
    @Nullable
    private final OnItemClickListener itemClickListener;

    public MainMenuAdapter(@NonNull List<String> items) {
        this(items, null);
    }

    public MainMenuAdapter(@NonNull List<String> items, @Nullable OnItemClickListener listener) {
        this.items = items;
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_main_menu, parent, false);
        return new MenuItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        holder.bind(items.get(position), itemClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MenuItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.menu_item_title);
        }

        void bind(String title, @Nullable OnItemClickListener listener) {
            titleView.setText(title);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    interface OnItemClickListener {
        void onItemClick(int position);
    }
}
