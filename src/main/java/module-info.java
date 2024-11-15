module com.example.groupbakal.groupbakal_finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires javafx.media;
    requires okhttp3;
    requires org.json;
    requires firebase.admin;
    requires com.google.auth.oauth2;
    requires com.google.auth;

    opens com.example.groupbakal.groupbakal_finalproject to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.example.groupbakal.groupbakal_finalproject;
}