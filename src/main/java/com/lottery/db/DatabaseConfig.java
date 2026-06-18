package com.lottery.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String SERVER = "localhost";
    private static final int PORT = 1433;
    private static final String DATABASE = "master";
    private static final String USERNAME = "lux";
    private static final String PASSWORD = "No64Adonis*";
    
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "SQL Server JDBC Driver not found", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        String connectionUrl = String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true;", 
                                           SERVER, PORT, DATABASE);
        
        logger.info("Attempting to connect with URL: " + connectionUrl);
        
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        
        return DriverManager.getConnection(connectionUrl, props);
    }
}