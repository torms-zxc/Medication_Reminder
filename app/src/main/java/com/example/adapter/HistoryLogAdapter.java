package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.HistoryLogModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryLogAdapter extends RecyclerView.Adapter<HistoryLogAdapter.LogViewHolder> {

    private final Context context;
    private List<HistoryLogModel> logList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    public HistoryLogAdapter(Context context) {
        this.context = context;
    }

    public void setLogs(List<HistoryLogModel> logs) {
        this.logList = logs != null ? logs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        HistoryLogModel log = logList.get(position);

        holder.tvLogMedName.setText(log.getMedicationName());
        String dosageAndTime = log.getDosage() + " • Scheduled: " + log.getScheduledTime();
        holder.tvLogDosageAndTime.setText(dosageAndTime);

        String actionTimeFormatted = "Logged: " + dateFormat.format(new Date(log.getActionTime()));
        if (log.getNotes() != null && !log.getNotes().isEmpty()) {
            actionTimeFormatted += " (" + log.getNotes() + ")";
        }
        holder.tvLogActionTime.setText(actionTimeFormatted);

        if ("TAKEN".equalsIgnoreCase(log.getStatus())) {
            holder.tvLogStatusBadge.setText("TAKEN");
            holder.tvLogStatusBadge.setBackgroundResource(R.drawable.badge_rounded_green);
            holder.tvLogStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.success));

            holder.ivLogStatusIcon.setImageResource(R.drawable.ic_check);
            holder.ivLogStatusIcon.setBackgroundResource(R.drawable.badge_rounded_green);
            holder.ivLogStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));
        } else {
            holder.tvLogStatusBadge.setText("SKIPPED");
            holder.tvLogStatusBadge.setBackgroundResource(R.drawable.badge_rounded_amber);
            holder.tvLogStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning));

            holder.ivLogStatusIcon.setImageResource(R.drawable.ic_skip);
            holder.ivLogStatusIcon.setBackgroundResource(R.drawable.badge_rounded_amber);
            holder.ivLogStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning));
        }
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogStatusIcon;
        TextView tvLogMedName, tvLogDosageAndTime, tvLogActionTime, tvLogStatusBadge;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogStatusIcon = itemView.findViewById(R.id.ivLogStatusIcon);
            tvLogMedName = itemView.findViewById(R.id.tvLogMedName);
            tvLogDosageAndTime = itemView.findViewById(R.id.tvLogDosageAndTime);
            tvLogActionTime = itemView.findViewById(R.id.tvLogActionTime);
            tvLogStatusBadge = itemView.findViewById(R.id.tvLogStatusBadge);
        }
    }
}
