package com.bankease.repository;

import com.bankease.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final Database db;

    public TransactionRepository(Database db) {
        this.db = db;
    }

    public void add(Connection c, int accountNo, String type, double amount,
                    double balanceAfter, String timestamp, String note) throws SQLException {
        String sql = "INSERT INTO transactions (account_no, type, amount, balance_after, timestamp, note) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setDouble(4, balanceAfter);
            ps.setString(5, timestamp);
            ps.setString(6, note);
            ps.executeUpdate();
        }
    }

    /** Returns the most recent transactions of an account, newest first. */
    public List<Transaction> findByAccount(int accountNo, int limit) throws SQLException {
        String sql = "SELECT id, account_no, type, amount, balance_after, timestamp, note "
                + "FROM transactions WHERE account_no = ? ORDER BY id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(rs.getInt("id"), rs.getInt("account_no"),
                            rs.getString("type"), rs.getDouble("amount"),
                            rs.getDouble("balance_after"), rs.getString("timestamp"),
                            rs.getString("note")));
                }
            }
        }
        return list;
    }
}
