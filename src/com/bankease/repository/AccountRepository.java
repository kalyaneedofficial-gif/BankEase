package com.bankease.repository;

import com.bankease.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepository {
    private final Database db;

    public AccountRepository(Database db) {
        this.db = db;
    }

    /** Creates an account using the supplied connection (part of a larger transaction). */
    public int create(Connection c, int customerId, String type, double balance) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, type, balance) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setString(2, type);
            ps.setDouble(3, balance);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Optional<Account> findByNo(Connection c, int accountNo) throws SQLException {
        String sql = "SELECT account_no, customer_id, type, balance FROM accounts WHERE account_no = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Account> findByNo(int accountNo) throws SQLException {
        try (Connection c = db.getConnection()) {
            return findByNo(c, accountNo);
        }
    }

    public void updateBalance(Connection c, int accountNo, double newBalance) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE accounts SET balance = ? WHERE account_no = ?")) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountNo);
            ps.executeUpdate();
        }
    }

    public List<Account> findAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT account_no, customer_id, type, balance FROM accounts ORDER BY account_no")) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private Account map(ResultSet rs) throws SQLException {
        return new Account(rs.getInt("account_no"), rs.getInt("customer_id"),
                rs.getString("type"), rs.getDouble("balance"));
    }
}
