package com.lottery.config;

public final class AppConfig {

    private AppConfig() {
    }

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_PAGINATION_LIMIT = 1000;

    public static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000L; 
    public static final long SESSION_ROTATION_MS = 15 * 60 * 1000L; 

    public static final int MIN_LOTTERY_NUMBER = 1;
    public static final int MAX_LOTTERY_NUMBER = 9;
    public static final int LOTTERY_NUMBERS_COUNT = 6;

    public static final int BCRYPT_LOG_ROUNDS = 12;
}
