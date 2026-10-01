//package com.example.test_reactive_spring.controller;
//
//import okhttp3.*;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class CurlTest {
//
//    private static final Pattern URL_PATTERN = Pattern.compile("^curl\\s+'(https?://[^']+)'");
//    private static final Pattern HEADER_PATTERN = Pattern.compile("-H\\s+'([^:]+):\\s*(.+)'");
//    private static final Pattern COOKIE_PATTERN = Pattern.compile("-b\\s+'(.+)'");
//    private static final Pattern DATA_RAW_PATTERN = Pattern.compile("--data-raw\\s+'(\\{.*\\}|\\[.*\\]|.*)'");
//
//    public static void main(String[] args) throws IOException {
//        String filePath = "curl_request.txt"; // Change to your file path
//        String curlContent = Files.readString(Paths.get(filePath));
//
//        executeCurl(curlContent);
//    }
//
//    public static void executeCurl(String curlCommand) throws IOException {
//        OkHttpClient client = new OkHttpClient();
//
//        // Extract URL
//        Matcher urlMatcher = URL_PATTERN.matcher(curlCommand);
//        if (!urlMatcher.find()) {
//            throw new IllegalArgumentException("Could not find URL in curl command");
//        }
//        String url = urlMatcher.group(1);
//
//        // Extract headers
//        Headers.Builder headersBuilder = new Headers.Builder();
//        Matcher headerMatcher = HEADER_PATTERN.matcher(curlCommand);
//        while (headerMatcher.find()) {
//            String key = headerMatcher.group(1).trim();
//            String value = headerMatcher.group(2).trim();
//            headersBuilder.add(key, value);
//        }
//
//        // Extract cookies if -b is present
//        Matcher cookieMatcher = COOKIE_PATTERN.matcher(curlCommand);
//        if (cookieMatcher.find()) {
//            String cookieString = cookieMatcher.group(1);
//            headersBuilder.add("Cookie", cookieString);
//        }
//
//        // Extract body
//        String jsonBody = null;
//        Matcher dataMatcher = DATA_RAW_PATTERN.matcher(curlCommand);
//        if (dataMatcher.find()) {
//            jsonBody = dataMatcher.group(1);
//        }
//
//        // Build request
//        Request.Builder requestBuilder = new Request.Builder()
//                .url(url)
//                .headers(headersBuilder.build());
//
//        if (jsonBody != null) {
//            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
//            requestBuilder.post(body);
//        } else {
//            requestBuilder.get(); // fallback to GET if no body
//        }
//
//        Request request = requestBuilder.build();
//
//        // Execute
//        try (Response response = client.newCall(request).execute()) {
//            System.out.println("Status Code: " + response.code());
//            System.out.println("Response Headers:");
//            System.out.println(response.headers());
//
//            String responseBody = response.body() != null ? response.body().string() : "";
//            System.out.println("Response Body:");
//            System.out.println(responseBody);
//        }
//    }
//}
