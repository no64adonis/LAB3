package com.lottery.service;

import java.util.logging.Logger;
import com.lottery.util.LogHelper;

public abstract class BaseService {
    protected static final Logger logger = Logger.getLogger(BaseService.class.getName());

    protected void logInfo(String msg) { logger.info(msg); }
    protected void logWarning(String msg) { logger.warning(msg); }
    protected void logSevere(String msg) { logger.severe(msg); }

    protected void logInfo(String method, String msg) { LogHelper.info(logger, method, msg); }
    protected void logWarning(String method, String msg) { LogHelper.warn(logger, method, msg); }
    protected void logSevere(String method, String msg) { LogHelper.error(logger, method, msg); }
}