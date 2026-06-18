package com.lottery.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogHelper {

    private LogHelper() {}

    public static void info(Logger logger, String method, String msg) {
        logger.info("[" + method + "] " + msg);
    }

    public static void warn(Logger logger, String method, String msg) {
        logger.warning("[" + method + "] " + msg);
    }

    public static void error(Logger logger, String method, String msg) {
        logger.severe("[" + method + "] " + msg);
    }

    public static void error(Logger logger, String method, String msg, Exception e) {
        logger.log(Level.SEVERE, "[" + method + "] " + msg, e);
    }

    public static void debug(Logger logger, String method, String msg) {
        logger.fine("[" + method + "] " + msg);
    }
}
