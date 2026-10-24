package com.example;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.HistoryLogAdapter;
import com.example.database.MedicationHistoryDatabaseHelper;
import com.example.model.AdherenceStats;
import com.example.model.HistoryLogModel;
import com.example.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class HistoryLogsActivity extends AppCompatActivity {

    private MaterialToolbar toolbarHistory;
    private ImageButton btnClearHistory;
    private TextView tvAdherencePercent, tvStatTaken, tvStatSkipped, tvStatTotal;
    private ChipGroup chipGroupHistoryFilter;
    private RecyclerView rvHistoryLogs;
    private LinearLayout layoutEmptyHistory;

    private HistoryLogAdapter historyAdapter;
    private MedicationHistoryDatabaseHelper medDbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_logs);

        sessionManager = new SessionManager(this);
        medDbHelper = new MedicationHistoryDatabaseHelper(this);

        initViews();
        setupListeners();
        setupRecyclerView();
        loadHistoryAndStats();
    }

    private void initViews() {
        toolbarHistory = findViewById(R.id.toolbarHistory);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        tvAdherencePercent = findViewById(R.id.tvAdherencePercent);
        tvStatTaken = findViewById(R.id.tvStatTaken);
        tvStatSkipped = findViewById(R.id.tvStatSkipped);
        tvStatTotal = findViewById(R.id.tvStatTotal);
        chipGroupHistoryFilter = findViewById(R.id.chipGroupHistoryFilter);
        rvHistoryLogs = findViewById(R.id.rvHistoryLogs);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);
    }

    private void setupListeners() {
        toolbarHistory.setNavigationOnClickListener(v -> finish());

        btnClearHistory.setOnClickListener(v -> showClearHistoryConfirmation());

        chipGroupHistoryFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            loadHistoryLogs();
        });
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryLogAdapter(this);
        rvHistoryLogs.setLayoutManager(new LinearLayoutManager(this));
        rvHistoryLogs.setAdapter(historyAdapter);
    }

    private void loadHistoryAndStats() {
        loadStats();
        loadHistoryLogs();
    }

    private void loadStats() {
        String userId = sessionManager.getUserId();
        AdherenceStats stats = medDbHelper.getStatsForUser(userId);

        tvAdherencePercent.setText(stats.getAdherencePercentage() + "%");
        tvStatTaken.setText(String.valueOf(stats.getTakenCount()));
        tvStatSkipped.setText(String.valueOf(stats.getSkippedCount()));
        tvStatTotal.setText("Total recorded doses: " + stats.getTotalDoses());
    }

    private void loadHistoryLogs() {
        String userId = sessionManager.getUserId();
        int checkedChipId = chipGroupHistoryFilter.getCheckedChipId();
        String filter = "ALL";

        if (checkedChipId == R.id.chipFilterTakenOnly) {
            filter = "TAKEN";
        } else if (checkedChipId == R.id.chipFilterSkippedOnly) {
            filter = "SKIPPED";
        }

        List<HistoryLogModel> logs = medDbHelper.getLogsForUser(userId, filter);
        historyAdapter.setLogs(logs);

        if (logs.isEmpty()) {
            layoutEmptyHistory.setVisibility(View.VISIBLE);
            rvHistoryLogs.setVisibility(View.GONE);
        } else {
            layoutEmptyHistory.setVisibility(View.GONE);
            rvHistoryLogs.setVisibility(View.VISIBLE);
        }
    }

    private void showClearHistoryConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear History Logs")
                .setMessage("Are you sure you want to clear all administration history logs? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    String userId = sessionManager.getUserId();
                    boolean cleared = medDbHelper.clearLogsForUser(userId);
                    if (cleared) {
                        Toast.makeText(this, "History logs cleared successfully.", Toast.LENGTH_SHORT).show();
                        loadHistoryAndStats();
                    } else {
                        Toast.makeText(this, "Could not clear history.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
