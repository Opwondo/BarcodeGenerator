package com.example.barcodegenerator;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private HistoryActivity activity;
    private DatabaseHelper databaseHelper;

    public HistoryAdapter(List<HistoryItem> historyList, HistoryActivity activity) {
        this.historyList = historyList;
        this.activity = activity;
        this.databaseHelper = new DatabaseHelper(activity);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<HistoryItem> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView contentText;
        private TextView typeText;
        private TextView timestampText;
        private ImageButton favoriteButton;
        private ImageButton shareButton;
        private ImageButton deleteButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.content_text);
            typeText = itemView.findViewById(R.id.type_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            shareButton = itemView.findViewById(R.id.share_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(HistoryItem item) {
            contentText.setText(item.getContent());
            typeText.setText(item.getType() + " • " + item.getFormat());
            timestampText.setText(android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", item.getTimestamp()));

            // Update favorite button icon
            updateFavoriteIcon(item.isFavorite());

            // Favorite button click
            favoriteButton.setOnClickListener(v -> toggleFavorite(item));

            // Share button click
            shareButton.setOnClickListener(v -> shareItem(item));

            // Delete button click
            deleteButton.setOnClickListener(v -> deleteItem(item));

            // Item click to view details
            itemView.setOnClickListener(v -> viewItemDetails(item));
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            int favoriteIcon = isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border;
            favoriteButton.setImageResource(favoriteIcon);
        }

        private void toggleFavorite(HistoryItem item) {
            boolean newFavoriteStatus = !item.isFavorite();
            databaseHelper.toggleFavorite(item.getId(), newFavoriteStatus);
            item.setFavorite(newFavoriteStatus);

            updateFavoriteIcon(newFavoriteStatus);

            String message = newFavoriteStatus ? "Added to favorites" : "Removed from favorites";
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

            // Refresh the list if we're in favorites tab
            activity.refreshData();
        }

        private void shareItem(HistoryItem item) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, item.getContent());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Barcode Content: " + item.getFormat());
            activity.startActivity(Intent.createChooser(shareIntent, "Share Barcode Content"));
        }

        private void deleteItem(HistoryItem item) {
            databaseHelper.deleteItem(item.getId());
            historyList.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
            Toast.makeText(activity, "Item deleted", Toast.LENGTH_SHORT).show();

            // Refresh empty state
            activity.refreshData();
        }

        private void viewItemDetails(HistoryItem item) {
            Intent intent = new Intent(activity, ScanResultActivity.class);
            intent.putExtra("SCANNED_BARCODE", item.getContent());
            intent.putExtra("BARCODE_FORMAT", item.getFormat());
            activity.startActivity(intent);
        }
    }
}