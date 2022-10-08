package com.example.osu_parse;

public class Score {
    private String scoring_type;
    private String scorePoints;
    private String mods;
    private String accuracy;

    public Score(String scoring_type, String scorePoints, String mods, String accuracy) {
        this.scoring_type = scoring_type;
        this.scorePoints = scorePoints;
        this.mods = mods;
        this.accuracy = accuracy;
    }

    public String getScoring_type() {
        return scoring_type;
    }

    public String getScorePoints() {
        return scorePoints;
    }

    public String getMods() {
        return mods;
    }

    public String getAccuracy() {
        return accuracy;
    }

    @Override
    public String toString() {
        return "\nScore{" +
                "scoring_type='" + scoring_type + '\'' +
                ", scorePoints='" + scorePoints + '\'' +
                ", mods='" + mods + '\'' +
                ", accuracy='" + accuracy + '\'' +
                '}';
    }
}
