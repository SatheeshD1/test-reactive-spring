package com.example.test_reactive_spring.dto;

public class CurlResponse {
    private int statusCode;
    private String headers;
    private String body;

    public CurlResponse(int statusCode, String headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    // Getters
    public int getStatusCode() { return statusCode; }
    public String getHeaders() { return headers; }
    public String getBody() { return body; }
}
