package com.example.groupbakal.groupbakal_finalproject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;

public class FirebaseHelper {

    private FirebaseDatabase database;
    private DatabaseReference leaderboardRef;

    static {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/triviagameapp-d5569-firebase-adminsdk-53nxq-b36dd7bcbc.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://triviagameapp-d5569-default-rtdb.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing Firebase", e);
        }
    }

    public FirebaseHelper() {
        database = FirebaseDatabase.getInstance();
        leaderboardRef = database.getReference("leaderboard");
    }
    public DatabaseReference getLeaderboardReference(String gameMode) {
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference();

        return baseRef.child(gameMode);
    }
}
