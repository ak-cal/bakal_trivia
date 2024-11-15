package com.example.groupbakal.groupbakal_finalproject;

public class PlayerData {
    private String name;
    private String difficulty;
    private Integer score;

    public PlayerData(String name, String difficulty, Integer score) {
        this.name = name;
        this.difficulty = difficulty;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getScore() {
        return score;
    }
}

