package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.MedicationModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    public interface OnMedicationActionListener {
        void onTakeClicked(MedicationModel medication);
        void onSkipClicked(MedicationModel medication);
        void onEditClicked(MedicationModel medication);
        void onDeleteClicked(MedicationModel medication);
    }

    private final Context context;
    private List<MedicationModel> medicationList = new ArrayList<>();
    private final OnMedicationActionListener listener;

    public MedicationAdapter(Context context, OnMedicationActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMedications(List<MedicationModel> list) {
        this.medicationList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        MedicationModel med = medicationList.get(position);

        holder.tvMedName.setText(med.getName());
        holder.tvMedCategory.setText(med.getCategory() != null ? med.getCategory() : "Medicine");
        holder.tvMedDosage.setText(med.getDosage());
        holder.tvMedTime.setText(med.getTimeFormatted());

        if (med.getInstructions() != null && !med.getInstructions().trim().isEmpty()) {
            holder.layoutInstructions.setVisibility(View.VISIBLE);
            holder.tvMedInstructions.setText(med.getInstructions().trim());
        } else {
            holder.layoutInstructions.setVisibility(View.GONE);
        }

        // Configure today's status badge
        String status = med.getTodayStatus();
        if ("TAKEN".equalsIgnoreCase(status)) {
            holder.tvTodayStatus.setText("Taken Today ✓");
            holder.tvTodayStatus.setBackgroundResource(R.drawable.badge_rounded_green);
            holder.tvTodayStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
            holder.btnTakeDose.setEnabled(false);
            holder.btnTakeDose.setText("Taken");
            holder.btnSkipDose.setEnabled(true);
        } else if ("SKIPPED".equalsIgnoreCase(status)) {
            holder.tvTodayStatus.setText("Skipped Today");
            holder.tvTodayStatus.setBackgroundResource(R.drawable.badge_rounded_amber);
            holder.tvTodayStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
            holder.btnTakeDose.setEnabled(true);
            holder.btnTakeDose.setText("Take Dose");
            holder.btnSkipDose.setEnabled(false);
        } else {
            holder.tvTodayStatus.setText("Pending");
            holder.tvTodayStatus.setBackgroundResource(R.drawable.badge_rounded_sky);
            holder.tvTodayStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary));
            holder.btnTakeDose.setEnabled(true);
            holder.btnTakeDose.setText("Take Dose");
            holder.btnSkipDose.setEnabled(true);
        }

        holder.btnTakeDose.setOnClickListener(v -> {
            if (listener != null) listener.onTakeClicked(med);
        });

        holder.btnSkipDose.setOnClickListener(v -> {
            if (listener != null) listener.onSkipClicked(med);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClicked(med);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(med);
        });
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedIcon;
        TextView tvMedName, tvMedCategory, tvMedDosage, tvMedTime, tvTodayStatus, tvMedInstructions;
        LinearLayout layoutInstructions;
        ImageButton btnEdit, btnDelete;
        MaterialButton btnTakeDose, btnSkipDose;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedIcon = itemView.findViewById(R.id.ivMedIcon);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedCategory = itemView.findViewById(R.id.tvMedCategory);
            tvMedDosage = itemView.findViewById(R.id.tvMedDosage);
            tvMedTime = itemView.findViewById(R.id.tvMedTime);
            tvTodayStatus = itemView.findViewById(R.id.tvTodayStatus);
            tvMedInstructions = itemView.findViewById(R.id.tvMedInstructions);
            layoutInstructions = itemView.findViewById(R.id.layoutInstructions);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnTakeDose = itemView.findViewById(R.id.btnTakeDose);
            btnSkipDose = itemView.findViewById(R.id.btnSkipDose);
        }
    }
}
