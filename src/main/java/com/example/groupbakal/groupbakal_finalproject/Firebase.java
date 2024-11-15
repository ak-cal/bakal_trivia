package com.example.groupbakal.groupbakal_finalproject;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Firebase {
    private static final Map<String, String> DATABASE_URLS = new HashMap<>();
    static {
        DATABASE_URLS.put("Classic", "https://triviagameapp-d5569-default-rtdb.firebaseio.com/Classic");
        DATABASE_URLS.put("Blitz", "https://triviagameapp-d5569-default-rtdb.firebaseio.com/Blitz");
        DATABASE_URLS.put("Endless", "https://triviagameapp-d5569-default-rtdb.firebaseio.com/Endless");
    }

    private static OkHttpClient client = new OkHttpClient();
    public static void savePlayerScore(String playerName, int totalScore, String mode, String difficulty) {
        try {
            String baseUrl = DATABASE_URLS.get(mode);
            if (baseUrl == null) {
                throw new IllegalArgumentException("Invalid game mode: " + mode);
            }
            JSONObject playerRecord = new JSONObject();
            playerRecord.put("name", playerName);
            playerRecord.put("score", totalScore);

            String path;

            if (mode.equals("Blitz")) {
                if (difficulty == null || difficulty.isEmpty()) {
                    throw new IllegalArgumentException("Difficulty must be selected for Blitz mode.");
                }

                path = baseUrl + "/" + difficulty + ".json";
            } else if (mode.equals("Classic") || mode.equals("Endless")) {
                path = baseUrl + "/" + difficulty + ".json";

            } else {
                throw new IllegalArgumentException("Invalid game mode: " + mode);
            }

            RequestBody body = RequestBody.create(playerRecord.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(path)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        System.out.println("Player score saved successfully!");
                    } else {
                        System.out.println("Error saving player score: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
