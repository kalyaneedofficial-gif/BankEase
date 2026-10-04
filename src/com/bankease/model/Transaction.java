package com.bankease.model;

public class Transaction {
    private final int id;
    private final int accountNo;
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String timestamp;
    private final String note;

    public Transaction(int id, int accountNo, String type, double amount,
                       double balanceAfter, String timestamp, String note) {
        this.id = id;
        this.accountNo = accountNo;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
        this.note = note;
    }

    public int getId() { return id; }
    public int getAccountNo() { return accountNo; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getTimestamp() { return timestamp; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return String.format("%s | %-12s | %10.2f | Balance: %10.2f | %s",
                timestamp, type, amount, balanceAfter, note);
    }
}
