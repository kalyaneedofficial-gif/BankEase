package com.bankease.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Manages the SQLite connection and creates the schema on first use. */
public class Database {
    private final String url;

    public Database(String filePath) {
        this.url = "jdbc:sqlite:" + filePath;
        initialize();
    }

    public Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    private void initialize() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "phone TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "account_no INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customer_id INTEGER NOT NULL REFERENCES customers(id),"
                    + "type TEXT NOT NULL,"
                    + "balance REAL NOT NULL CHECK (balance >= 0))");
            s.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "account_no INTEGER NOT NULL REFERENCES accounts(account_no),"
                    + "type TEXT NOT NULL,"
                    + "amount REAL NOT NULL,"
                    + "balance_after REAL NOT NULL,"
                    + "timestamp TEXT NOT NULL,"
                    + "note TEXT)");
            // Make account numbers start from 1001
            s.execute("INSERT INTO sqlite_sequence (name, seq) "
                    + "SELECT 'accounts', 1000 "
                    + "WHERE NOT EXISTS (SELECT 1 FROM sqlite_sequence WHERE name = 'accounts')");
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialise database: " + e.getMessage(), e);
        }
    }
}
