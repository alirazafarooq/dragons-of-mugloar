
package com.example.mugloar.service;

import com.example.mugloar.model.Mission;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class MissionDecoder {

    private MissionDecoder() {
    }

    public static Mission decode(Mission mission) {
        if (mission == null || mission.encrypted() == null) {
            return mission;
        }

        String message = mission.message();
        String probability = mission.probability();
        String adID = mission.adId();

        switch (mission.encrypted()) {
            case "1" -> {
                message = decodeBase64(message);
                probability = decodeBase64(probability);
                adID = decodeBase64(adID);
            }
            case "2" -> {
                message = decodeRot13(message);
                probability = decodeRot13(probability);
                adID = decodeBase64(adID);
            }
            default -> {
                // Unknown encoding: preserve the original fields.
            }
        }

        return new Mission(
                adID,
                message,
                mission.reward(),
                mission.expiresIn(),
                mission.encrypted(),
                probability
        );
    }

    private static String decodeBase64(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            // Preserve the original value if it is not valid Base64.
            return value;
        }
    }

    private static String decodeRot13(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder result = new StringBuilder(value.length());

        for (char character : value.toCharArray()) {
            if (character >= 'a' && character <= 'z') {
                result.append((char) ('a' + (character - 'a' + 13) % 26));
            } else if (character >= 'A' && character <= 'Z') {
                result.append((char) ('A' + (character - 'A' + 13) % 26));
            } else {
                // Keep spaces, punctuation, accents, and other characters.
                result.append(character);
            }
        }

        return result.toString();
    }
}
