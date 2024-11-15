package com.example.groupbakal.groupbakal_finalproject;

public class PlayerProfile {
    private static String playerName = "Guest";

    public static String getPlayerName() {
        return playerName;
    }
    public static void setPlayerName(String playerName) {
        PlayerProfile.playerName = playerName;
    }
}

