package com.example;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.database.MedicationHistoryDatabaseHelper;
import com.example.model.MedicationModel;
import com.example.util.AlarmScheduler;
import com.example.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class AddEditMedicationActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private MedicationModel existingMedication = null;

    private MaterialToolbar toolbarAddEdit;
    private TextInputLayout tilMedName, tilDosage, tilInstructions;
    private TextInputEditText etMedName, etDosage, etInstructions;
    private ChipGroup chipGroupDosageSuggestions, chipGroupCategory, chipGroupInstructionSuggestions;
    private MaterialCardView cardSelectTime;
    private TextView tvSelectedTime;
    private MaterialButton btnChangeTime, btnSaveSchedule;
    private MaterialSwitch switchReminderActive;

    private int selectedHour = 8;
    private int selectedMinute = 0;
    private String selectedTimeFormatted = "08:00 AM";

    private MedicationHistoryDatabaseHelper medDbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medication);

        medDbHelper = new MedicationHistoryDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        initViews();
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        toolbarAddEdit = findViewById(R.id.toolbarAddEdit);
        tilMedName = findViewById(R.id.tilMedName);
        tilDosage = findViewById(R.id.tilDosage);
        tilInstructions = findViewById(R.id.tilInstructions);

        etMedName = findViewById(R.id.etMedName);
        etDosage = findViewById(R.id.etDosage);
        etInstructions = findViewById(R.id.etInstructions);

        chipGroupDosageSuggestions = findViewById(R.id.chipGroupDosageSuggestions);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupInstructionSuggestions = findViewById(R.id.chipGroupInstructionSuggestions);

        cardSelectTime = findViewById(R.id.cardSelectTime);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnChangeTime = findViewById(R.id.btnChangeTime);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        switchReminderActive = findViewById(R.id.switchReminderActive);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("medication")) {
            existingMedication = (MedicationModel) getIntent().getSerializableExtra("medication");
            if (existingMedication != null) {
                isEditMode = true;
                toolbarAddEdit.setTitle(R.string.edit_schedule);
                btnSaveSchedule.setText(R.string.edit_schedule);

                etMedName.setText(existingMedication.getName());
                etDosage.setText(existingMedication.getDosage());
                etInstructions.setText(existingMedication.getInstructions());

                selectedHour = existingMedication.getTimeHour();
                selectedMinute = existingMedication.getTimeMinute();
                selectedTimeFormatted = existingMedication.getTimeFormatted();
                tvSelectedTime.setText(selectedTimeFormatted);

                switchReminderActive.setChecked(existingMedication.isActive());

                selectCategoryChip(existingMedication.getCategory());
            }
        }
    }

    private void selectCategoryChip(String category) {
        if (category == null) return;
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            View child = chipGroupCategory.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equalsIgnoreCase(category)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private String getSelectedCategory() {
        int checkedId = chipGroupCategory.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                return chip.getText().toString();
            }
        }
        return "Tablet";
    }

    private void setupListeners() {
        toolbarAddEdit.setNavigationOnClickListener(v -> finish());

        View.OnClickListener timePickerClick = v -> openTimePicker();
        cardSelectTime.setOnClickListener(timePickerClick);
        btnChangeTime.setOnClickListener(timePickerClick);

        // Dosage suggestion chips click
        for (int i = 0; i < chipGroupDosageSuggestions.getChildCount(); i++) {
            View child = chipGroupDosageSuggestions.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> etDosage.setText(chip.getText().toString()));
            }
        }

        // Instruction suggestion chips click
        for (int i = 0; i < chipGroupInstructionSuggestions.getChildCount(); i++) {
            View child = chipGroupInstructionSuggestions.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    String existing = etInstructions.getText() != null ? etInstructions.getText().toString().trim() : "";
                    String chipText = chip.getText().toString();
                    if (existing.isEmpty()) {
                        etInstructions.setText(chipText);
                    } else if (!existing.contains(chipText)) {
                        etInstructions.setText(existing + ". " + chipText);
                    }
                });
            }
        }

        btnSaveSchedule.setOnClickListener(v -> handleSave());
    }

    private void openTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    selectedTimeFormatted = formatTime(hourOfDay, minute);
                    tvSelectedTime.setText(selectedTimeFormatted);
                },
                selectedHour,
                selectedMinute,
                false // 12-hour AM/PM format
        );
        dialog.show();
    }

    private String formatTime(int hour, int minute) {
        String period = (hour >= 12) ? "PM" : "AM";
        int displayHour = (hour > 12) ? (hour - 12) : (hour == 0 ? 12 : hour);
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, period);
    }

    private void handleSave() {
        tilMedName.setError(null);
        tilDosage.setError(null);

        String medName = etMedName.getText() != null ? etMedName.getText().toString().trim() : "";
        String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";
        String instructions = etInstructions.getText() != null ? etInstructions.getText().toString().trim() : "";
        String category = getSelectedCategory();
        boolean isActive = switchReminderActive.isChecked();

        if (medName.isEmpty()) {
            tilMedName.setError("Please enter the medication name");
            etMedName.requestFocus();
            return;
        }

        if (dosage.isEmpty()) {
            tilDosage.setError("Please specify the dosage amount");
            etDosage.requestFocus();
            return;
        }

        String userId = sessionManager.getUserId();

        if (isEditMode && existingMedication != null) {
            existingMedication.setName(medName);
            existingMedication.setDosage(dosage);
            existingMedication.setTimeHour(selectedHour);
            existingMedication.setTimeMinute(selectedMinute);
            existingMedication.setTimeFormatted(selectedTimeFormatted);
            existingMedication.setInstructions(instructions);
            existingMedication.setCategory(category);
            existingMedication.setActive(isActive);

            boolean updated = medDbHelper.updateMedication(existingMedication);
            if (updated) {
                if (isActive) {
                    AlarmScheduler.scheduleMedicationReminder(this, existingMedication);
                } else {
                    AlarmScheduler.cancelMedicationReminder(this, existingMedication.getId());
                }
                Toast.makeText(this, "Medication schedule updated successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update schedule.", Toast.LENGTH_SHORT).show();
            }
        } else {
            MedicationModel newMed = new MedicationModel(
                    0,
                    userId,
                    medName,
                    dosage,
                    selectedHour,
                    selectedMinute,
                    selectedTimeFormatted,
                    instructions,
                    category,
                    isActive,
                    System.currentTimeMillis()
            );

            long insertedId = medDbHelper.insertMedication(newMed);
            if (insertedId != -1) {
                newMed.setId((int) insertedId);
                if (isActive) {
                    AlarmScheduler.scheduleMedicationReminder(this, newMed);
                }
                Toast.makeText(this, "Medication schedule saved! Reminder set for " + selectedTimeFormatted, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to save medication schedule to database.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
