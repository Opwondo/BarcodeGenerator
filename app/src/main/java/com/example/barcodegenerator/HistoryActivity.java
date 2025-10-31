package com.example.barcodegenerator;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private ImageButton backButton;
    private TextView historyTab, favoritesTab;
    private TextView emptyStateText;
    private View emptyStateView;
    private boolean showingFavorites = false;

    private DatabaseHelper databaseHelper;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupClickListeners();

        databaseHelper = new DatabaseHelper(this);
        setupRecyclerView();
    }

    private void initViews() {
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        backButton = findViewById(R.id.back_button);
        historyTab = findViewById(R.id.history_tab);
        favoritesTab = findViewById(R.id.favorites_tab);
        emptyStateView = findViewById(R.id.empty_state_view);
        emptyStateText = findViewById(R.id.empty_state_text);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        historyTab.setOnClickListener(v -> switchToHistory());
        favoritesTab.setOnClickListener(v -> switchToFavorites());
    }

    private void switchToHistory() {
        showingFavorites = false;
        historyTab.setBackgroundResource(R.drawable.tab_selected);
        favoritesTab.setBackgroundResource(R.drawable.tab_unselected);
        emptyStateText.setText("No scan history\nStart scanning to see your history here");
        refreshData();
    }

    private void switchToFavorites() {
        showingFavorites = true;
        favoritesTab.setBackgroundResource(R.drawable.tab_selected);
        historyTab.setBackgroundResource(R.drawable.tab_unselected);
        emptyStateText.setText("No favorites yet\nAdd favorites by tapping the heart icon");
        refreshData();
    }

    private void setupRecyclerView() {
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshData();
    }

    public void refreshData() {
        List<HistoryItem> items;
        if (showingFavorites) {
            items = databaseHelper.getFavorites();
        } else {
            items = databaseHelper.getAllHistory();
        }

        if (adapter == null) {
            adapter = new HistoryAdapter(items, this);
            historyRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(items);
        }

        checkEmptyState(items);
    }

    private void checkEmptyState(List<HistoryItem> items) {
        if (items.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}