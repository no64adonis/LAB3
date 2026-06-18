package com.lottery.util;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lottery.config.AppConfig;

public class LotteryNumberParser {

    public static class ParseResult {
        private final Integer[] numbers = new Integer[6];
        private boolean valid = true;
        private String errorMessage;

        public Integer[] getNumbers() {
            return numbers;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String toCsvString() {
            return Arrays.stream(numbers)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    public static ParseResult parse(String num1, String num2, String num3,
            String num4, String num5, String num6) {
        ParseResult result = new ParseResult();
        String[] inputs = { num1, num2, num3, num4, num5, num6 };

        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] != null && !inputs[i].trim().isEmpty()) {
                try {
                    int value = Integer.parseInt(inputs[i].trim());
                    if (!InputValidator.isValidLotteryNumber(value)) {
                        result.valid = false;
                        result.errorMessage = "Number " + (i + 1) + " must be between 1 and 99.";
                        break;
                    }
                    result.numbers[i] = value;
                } catch (NumberFormatException e) {
                    result.valid = false;
                    result.errorMessage = "Number " + (i + 1) + " must be a valid number.";
                    break;
                }
            }
        }
        return result;
    }

    public static Integer[] generateRandom(Integer[] existing, SecureRandom random) {
        Integer[] result = Arrays.copyOf(existing, existing.length);
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                result[i] = random.nextInt(AppConfig.MAX_LOTTERY_NUMBER) + AppConfig.MIN_LOTTERY_NUMBER;
            }
        }
        return result;
    }
}
