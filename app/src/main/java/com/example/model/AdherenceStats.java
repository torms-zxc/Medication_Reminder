package com.example.model;

public class AdherenceStats {
    private int totalDoses;
    private int takenCount;
    private int skippedCount;
    private int adherencePercentage;

    public AdherenceStats(int totalDoses, int takenCount, int skippedCount) {
        this.totalDoses = totalDoses;
        this.takenCount = takenCount;
        this.skippedCount = skippedCount;
        if (totalDoses > 0) {
            this.adherencePercentage = (int) Math.round(((double) takenCount / totalDoses) * 100);
        } else {
            this.adherencePercentage = 100;
        }
    }

    public int getTotalDoses() {
        return totalDoses;
    }

    public int getTakenCount() {
        return takenCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getAdherencePercentage() {
        return adherencePercentage;
    }
}
