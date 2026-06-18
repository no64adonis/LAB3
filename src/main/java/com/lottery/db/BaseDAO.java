package com.lottery.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import com.lottery.util.LogHelper;

public abstract class BaseDAO {
    protected static final Logger logger = Logger.getLogger(BaseDAO.class.getName());

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    protected void logError(String msg, Exception e) { LogHelper.error(logger, "", msg, e); }
    protected void logError(String msg, String method, Exception e) { LogHelper.error(logger, method, msg, e); }
    protected void logInfo(String msg) { logger.info(msg); }
    protected void logInfo(String msg, String method) { LogHelper.info(logger, method, msg); }
    protected void logWarning(String msg) { logger.warning(msg); }
    protected void logWarning(String msg, String method) { LogHelper.warn(logger, method, msg); }
    protected void logDebug(String msg) { logger.fine(msg); }
    protected void logDebug(String msg, String method) { LogHelper.debug(logger, method, msg); }
}