package com.laundry.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Pattern:
 * one shared database configuration/manager is used by the application.
 * A connection is still opened per operation; the Singleton is not a shared
 * JDBC connection and therefore avoids stale connections and thread-safety problems.
 */
public final class DatabaseManager {
    private static volatile DatabaseManager instance;
    private final Properties properties = new Properties();

    private DatabaseManager() {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) throw new IllegalStateException("database.properties was not found on the classpath");
            properties.load(input);
            Class.forName(properties.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) instance = new DatabaseManager();
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        String url = value("LAUNDRY_DB_URL", "db.url");
        String username = value("LAUNDRY_DB_USERNAME", "db.username");
        String password = value("LAUNDRY_DB_PASSWORD", "db.password");
        return DriverManager.getConnection(url, username, password);
    }

    private String value(String environmentName, String propertyName) {
        String environmentValue = System.getenv(environmentName);
        return environmentValue == null || environmentValue.isBlank()
                ? properties.getProperty(propertyName)
                : environmentValue;
    }
}
