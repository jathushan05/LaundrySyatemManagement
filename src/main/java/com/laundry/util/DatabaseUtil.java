package com.laundry.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Backwards-compatible database facade. The actual configuration is managed
 * by DatabaseManager (Singleton Pattern).
 */
public final class DatabaseUtil {
    private DatabaseUtil() { }

    public static Connection getConnection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }
}
