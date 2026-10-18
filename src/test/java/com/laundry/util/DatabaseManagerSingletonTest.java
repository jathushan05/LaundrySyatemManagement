package com.laundry.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Demonstrates the Singleton requirement: every call to getInstance()
 * returns the same DatabaseManager object.
 */
class DatabaseManagerSingletonTest {

    @Test
    void returnsTheSameInstance() {
        DatabaseManager first = DatabaseManager.getInstance();
        DatabaseManager second = DatabaseManager.getInstance();

        assertSame(first, second);
    }
}
