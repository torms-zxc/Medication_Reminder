package com.example;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.MedicationAdapter;
import com.example.database.MedicationHistoryDatabaseHelper;
import com.example.model.HistoryLogModel;
import com.example.model.MedicationModel;
import com.example.util.AlarmScheduler;
import com.example.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements MedicationAdapter.OnMedicationActionListener {

    private SessionManager sessionManager;
    private MedicationHistoryDatabaseHelper medDbHelper;

    private TextView tvCurrentDate, tvUserGreeting, tvQuickAdherence, tvScheduleCount;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvMedications;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnEmptyAdd, btnNavHistory;
    private ImageButton btnLogout;
    private ExtendedFloatingActionButton fabAddMedication;

    private MedicationAdapter medicationAdapter;
    private List<MedicationModel> allMedications = new ArrayList<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification reminders enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Enable notifications in system settings for timely alarms.", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadMedications();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        medDbHelper = new MedicationHistoryDatabaseHelper(this);

        initViews();
        setupNotificationChannelAndPermissions();
        setupRecyclerView();
        setupListeners();
        updateHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications();
    }

    private void initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvUserGreeting = findViewById(R.id.tvUserGreeting);
        tvQuickAdherence = findViewById(R.id.tvQuickAdherence);
        tvScheduleCount = findViewById(R.id.tvScheduleCount);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        rvMedications = findViewById(R.id.rvMedications);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnEmptyAdd = findViewById(R.id.btnEmptyAdd);
        btnNavHistory = findViewById(R.id.btnNavHistory);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddMedication = findViewById(R.id.fabAddMedication);
    }

    private void setupNotificationChannelAndPermissions() {
        // Initialize channel for Android 8.0+
        AlarmScheduler.createNotificationChannel(this);

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupRecyclerView() {
        medicationAdapter = new MedicationAdapter(this, this);
        rvMedications.setLayoutManager(new LinearLayoutManager(this));
        rvMedications.setAdapter(medicationAdapter);
    }

    private void setupListeners() {
        fabAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEditMedicationActivity.class);
            addEditLauncher.launch(intent);
        });

        btnEmptyAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEditMedicationActivity.class);
            addEditLauncher.launch(intent);
        });

        btnNavHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryLogsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterMedications();
        });
    }

    private void updateHeaderInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        tvCurrentDate.setText(dateFormat.format(new Date()));

        String userName = sessionManager.getUserName();
        tvUserGreeting.setText("Welcome, " + userName + "!");
    }

    private void loadMedications() {
        String userId = sessionManager.getUserId();
        allMedications = medDbHelper.getMedicationsForUser(userId);

        filterMedications();
        updateAdherenceSummary();
    }

    private void filterMedications() {
        int checkedChipId = chipGroupFilter.getCheckedChipId();
        List<MedicationModel> filteredList = new ArrayList<>();

        for (MedicationModel med : allMedications) {
            if (checkedChipId == R.id.chipFilterMorning) {
                // AM: hour < 12
                if (med.getTimeHour() < 12) {
                    filteredList.add(med);
                }
            } else if (checkedChipId == R.id.chipFilterAfternoon) {
                // PM: hour >= 12
                if (med.getTimeHour() >= 12) {
                    filteredList.add(med);
                }
            } else {
                filteredList.add(med);
            }
        }

        medicationAdapter.setMedications(filteredList);

        if (allMedications.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvMedications.setVisibility(View.GONE);
            tvScheduleCount.setText("0 items");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvMedications.setVisibility(View.VISIBLE);
            tvScheduleCount.setText(filteredList.size() + " scheduled");
        }
    }

    private void updateAdherenceSummary() {
        int total = allMedications.size();
        int takenToday = 0;
        for (MedicationModel med : allMedications) {
            if ("TAKEN".equalsIgnoreCase(med.getTodayStatus())) {
                takenToday++;
            }
        }
        tvQuickAdherence.setText(takenToday + " of " + total + " Doses Taken Today");
    }

    @Override
    public void onTakeClicked(MedicationModel medication) {
        String userId = sessionManager.getUserId();
        HistoryLogModel log = new HistoryLogModel(
                0,
                userId,
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getTimeFormatted(),
                System.currentTimeMillis(),
                "TAKEN",
                "Administered from Home screen"
        );

        medDbHelper.insertLog(log);
        medication.setTodayStatus("TAKEN");
        medicationAdapter.notifyDataSetChanged();
        updateAdherenceSummary();

        Snackbar.make(findViewById(android.R.id.content),
                "✓ " + medication.getName() + " marked as Taken!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSkipClicked(MedicationModel medication) {
        String userId = sessionManager.getUserId();
        HistoryLogModel log = new HistoryLogModel(
                0,
                userId,
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getTimeFormatted(),
                System.currentTimeMillis(),
                "SKIPPED",
                "Skipped from Home screen"
        );

        medDbHelper.insertLog(log);
        medication.setTodayStatus("SKIPPED");
        medicationAdapter.notifyDataSetChanged();
        updateAdherenceSummary();

        Snackbar.make(findViewById(android.R.id.content),
                medication.getName() + " marked as Skipped.", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClicked(MedicationModel medication) {
        Intent intent = new Intent(HomeActivity.this, AddEditMedicationActivity.class);
        intent.putExtra("medication", medication);
        addEditLauncher.launch(intent);
    }

    @Override
    public void onDeleteClicked(MedicationModel medication) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete the schedule for " + medication.getName() + "? This will cancel upcoming alarms.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = medDbHelper.deleteMedication(medication.getId(), sessionManager.getUserId());
                    if (deleted) {
                        AlarmScheduler.cancelMedicationReminder(this, medication.getId());
                        Toast.makeText(this, medication.getName() + " removed from schedules.", Toast.LENGTH_SHORT).show();
                        loadMedications();
                    } else {
                        Toast.makeText(this, "Could not delete medication schedule.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to log out of your medication account?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
