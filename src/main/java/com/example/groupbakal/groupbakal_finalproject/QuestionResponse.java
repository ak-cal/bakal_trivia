package com.example.groupbakal.groupbakal_finalproject;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuestionResponse {
    @JsonProperty("response_code")
    private int responseCode;

    @JsonProperty("results")
    private List<Question> results;

    public QuestionResponse() {}

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<Question> getResults() {
        return results;
    }

    public void setResults(List<Question> results) {
        this.results = results;
    }
}

