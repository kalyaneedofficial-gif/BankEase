package com.bankease.model;

public class Account {
    public static final String SAVINGS = "SAVINGS";
    public static final String CURRENT = "CURRENT";
    public static final double SAVINGS_MIN_BALANCE = 500.0;

    private final int accountNo;
    private final int customerId;
    private final String type;
    private final double balance;

    public Account(int accountNo, int customerId, String type, double balance) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.type = type;
        this.balance = balance;
    }

    public int getAccountNo() { return accountNo; }
    public int getCustomerId() { return customerId; }
    public String getType() { return type; }
    public double getBalance() { return balance; }

    /** Minimum balance that must always remain in the account. */
    public double getMinBalance() {
        return SAVINGS.equals(type) ? SAVINGS_MIN_BALANCE : 0.0;
    }

    @Override
    public String toString() {
        return String.format("Acc No: %d | Customer ID: %d | Type: %s | Balance: %.2f",
                accountNo, customerId, type, balance);
    }
}
