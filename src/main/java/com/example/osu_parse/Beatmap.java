package com.example.osu_parse;

import java.util.HashMap;
import java.util.Map;

public class Beatmap {
    private Map<String, Score> userToScore;
    private String name;

    public Beatmap(String name) {
        this.userToScore = new HashMap<>();
        this.name = name;
    }

    public Map<String, Score> getUserToScore() {
        return userToScore;
    }

    public void put(String userId, Score score) {
        this.userToScore.put(userId, score);
    }

    public Score get(String user) {
        if (userToScore.containsKey(user)) {
            return userToScore.get(user);
        }
        return new Score("-", "0", "-", "0");
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "\n" + "Beatmap{" +
                "userToScore=" + userToScore +
                '}';
    }
}
