package com.bankease.service;

import com.bankease.repository.Database;
import com.bankease.util.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BankServiceTest {
    private BankService bank;

    @BeforeEach
    void setUp(@TempDir Path dir) {
        bank = new BankService(new Database(dir.resolve("test.db").toString()));
    }

    private int newAccount(double opening) {
        int cid = bank.addCustomer("Test User", "user" + System.nanoTime() + "@mail.com", "9876543210");
        return bank.openAccount(cid, "SAVINGS", opening);
    }

    @Test
    void addCustomerShouldCreateCustomer() {
        int id = bank.addCustomer("Kalyanee Deshmukh", "kalyanee@mail.com", "9876543210");
        assertTrue(id > 0);
        assertEquals(1, bank.getCustomers().size());
    }

    @Test
    void addCustomerShouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> bank.addCustomer("Test User", "not-an-email", "9876543210"));
    }

    @Test
    void openAccountShouldStoreOpeningBalance() {
        int acc = newAccount(1000);
        assertEquals(1000.0, bank.getBalance(acc), 0.001);
    }

    @Test
    void depositShouldIncreaseBalance() {
        int acc = newAccount(1000);
        bank.deposit(acc, 500);
        assertEquals(1500.0, bank.getBalance(acc), 0.001);
    }

    @Test
    void withdrawBelowMinimumBalanceShouldFail() {
        int acc = newAccount(1000);
        assertThrows(InsufficientFundsException.class, () -> bank.withdraw(acc, 600));
        assertEquals(1000.0, bank.getBalance(acc), 0.001);
    }

    @Test
    void transferShouldMoveFundsBetweenAccounts() throws Exception {
        int a = newAccount(2000);
        int b = newAccount(1000);
        bank.transfer(a, b, 700);
        assertEquals(1300.0, bank.getBalance(a), 0.001);
        assertEquals(1700.0, bank.getBalance(b), 0.001);
    }
}
