package com.bankease.service;

import com.bankease.model.Account;
import com.bankease.model.Customer;
import com.bankease.model.Transaction;
import com.bankease.repository.AccountRepository;
import com.bankease.repository.CustomerRepository;
import com.bankease.repository.Database;
import com.bankease.repository.TransactionRepository;
import com.bankease.util.InputValidator;
import com.bankease.util.InsufficientFundsException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Business logic of the bank: customers, accounts and money movement. */
public class BankService {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Database db;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public BankService(Database db) {
        this.db = db;
        this.customers = new CustomerRepository(db);
        this.accounts = new AccountRepository(db);
        this.transactions = new TransactionRepository(db);
    }

    @FunctionalInterface
    private interface Work<T> {
        T run(Connection c) throws SQLException, InsufficientFundsException;
    }

    /** Runs the work as one database transaction: all steps succeed or none are saved. */
    private <T> T inTransaction(Work<T> work) throws InsufficientFundsException {
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | InsufficientFundsException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    // ---------- Customers ----------

    public int addCustomer(String name, String email, String phone) {
        if (!InputValidator.isValidName(name)) {
            throw new IllegalArgumentException("Invalid name.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must be a 10-digit Indian mobile number.");
        }
        try {
            return customers.add(name.trim(), email.trim().toLowerCase(), phone.trim());
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("A customer with this email already exists.");
            }
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public List<Customer> getCustomers() {
        try {
            return customers.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    // ---------- Accounts ----------

    public int openAccount(int customerId, String type, double openingDeposit) {
        if (!InputValidator.isValidAccountType(type)) {
            throw new IllegalArgumentException("Account type must be SAVINGS or CURRENT.");
        }
        String t = type.toUpperCase();
        double min = Account.SAVINGS.equals(t) ? Account.SAVINGS_MIN_BALANCE : 0.0;
        if (!InputValidator.isPositiveAmount(openingDeposit) || openingDeposit < min) {
            throw new IllegalArgumentException("Opening deposit must be positive and at least " + min + ".");
        }
        try {
            if (customers.findById(customerId).isEmpty()) {
                throw new IllegalArgumentException("Customer not found.");
            }
            return inTransaction(c -> {
                int no = accounts.create(c, customerId, t, openingDeposit);
                transactions.add(c, no, "OPENING", openingDeposit, openingDeposit, now(), "Account opened");
                return no;
            });
        } catch (SQLException | InsufficientFundsException e) {
            throw new RuntimeException("Could not open account: " + e.getMessage(), e);
        }
    }

    public List<Account> getAccounts() {
        try {
            return accounts.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public double getBalance(int accountNo) {
        try {
            return accounts.findByNo(accountNo)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found."))
                    .getBalance();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    // ---------- Money movement ----------

    public double deposit(int accountNo, double amount) {
        requirePositive(amount);
        try {
            return inTransaction(c -> post(c, accountNo, amount, "DEPOSIT", "Cash deposit"));
        } catch (InsufficientFundsException e) {
            throw new IllegalStateException(e); // cannot happen for a deposit
        }
    }

    public double withdraw(int accountNo, double amount) throws InsufficientFundsException {
        requirePositive(amount);
        return inTransaction(c -> post(c, accountNo, -amount, "WITHDRAWAL", "Cash withdrawal"));
    }

    public void transfer(int fromNo, int toNo, double amount) throws InsufficientFundsException {
        requirePositive(amount);
        if (fromNo == toNo) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }
        inTransaction(c -> {
            post(c, fromNo, -amount, "TRANSFER_OUT", "To account " + toNo);
            post(c, toNo, amount, "TRANSFER_IN", "From account " + fromNo);
            return null;
        });
    }

    public List<Transaction> getStatement(int accountNo, int limit) {
        try {
            if (accounts.findByNo(accountNo).isEmpty()) {
                throw new IllegalArgumentException("Account not found.");
            }
            return transactions.findByAccount(accountNo, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    // ---------- helpers ----------

    /** Applies a signed change to an account balance and records the transaction. */
    private double post(Connection c, int accountNo, double delta, String type, String note)
            throws SQLException, InsufficientFundsException {
        Account acc = accounts.findByNo(c, accountNo)
                .orElseThrow(() -> new IllegalArgumentException("Account " + accountNo + " not found."));
        double newBalance = acc.getBalance() + delta;
        if (newBalance < acc.getMinBalance()) {
            throw new InsufficientFundsException("Insufficient funds in account " + accountNo
                    + ". Minimum balance of " + acc.getMinBalance() + " must be maintained.");
        }
        accounts.updateBalance(c, accountNo, newBalance);
        transactions.add(c, accountNo, type, Math.abs(delta), newBalance, now(), note);
        return newBalance;
    }

    private void requirePositive(double amount) {
        if (!InputValidator.isPositiveAmount(amount)) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    private String now() {
        return LocalDateTime.now().format(TS);
    }
}
