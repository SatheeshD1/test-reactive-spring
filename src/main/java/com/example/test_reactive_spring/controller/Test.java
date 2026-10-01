package com.example.test_reactive_spring.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Test {

    public static void main(String args[]){
//        String filename = "all_6_digit_otps.txt";
        String filename = "all_mobile.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Loop from 000000 to 999999
//            for (int i = 0; i < 1_000_000; i++) {
            for (int i = 900000; i < 1_000_000; i++) {
                // Format as 6 digits with leading zeros
//                String otp = String.format("%06d", i);
                String otp = String.format("%06d", i)+ "8225";
                writer.write(otp);
                writer.newLine();  // Each OTP on a new line
            }

            System.out.println("Successfully generated 1,000,000 possible 6-digit OTPs.");
            System.out.println("File saved as: " + filename);
            System.out.println("File size will be approximately 7-8 MB.");

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
