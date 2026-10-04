package com.bankease.ui;

import com.bankease.model.Account;
import com.bankease.model.Customer;
import com.bankease.model.Transaction;
import com.bankease.repository.Database;
import com.bankease.service.BankService;
import com.bankease.util.InputValidator;
import com.bankease.util.InsufficientFundsException;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static BankService bank;

    public static void main(String[] args) {
        try {
            bank = new BankService(new Database("bankease.db"));
        } catch (RuntimeException e) {
            System.out.println("Startup error: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Enter choice: ");
            try {
                switch (choice.trim()) {
                    case "1": addCustomer(); break;
                    case "2": viewCustomers(); break;
                    case "3": openAccount(); break;
                    case "4": viewAccounts(); break;
                    case "5": deposit(); break;
                    case "6": withdraw(); break;
                    case "7": transfer(); break;
                    case "8": checkBalance(); break;
                    case "9": miniStatement(); break;
                    case "0": running = false; System.out.println("Thank you for using BankEase."); break;
                    default: System.out.println("Invalid choice. Please select 0-9.");
                }
            } catch (InsufficientFundsException e) {
                System.out.println("Transaction failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=====================================");
        System.out.println("        BANKEASE MANAGEMENT SYSTEM");
        System.out.println("=====================================");
        System.out.println("1. Add Customer");
        System.out.println("2. View Customers");
        System.out.println("3. Open Account");
        System.out.println("4. View Accounts");
        System.out.println("5. Deposit");
        System.out.println("6. Withdraw");
        System.out.println("7. Transfer Funds");
        System.out.println("8. Check Balance");
        System.out.println("9. Mini Statement");
        System.out.println("0. Exit");
        System.out.println("=====================================");
    }

    private static String prompt(String text) {
        System.out.print(text);
        return in.hasNextLine() ? in.nextLine() : "0";
    }

    private static void addCustomer() {
        String name = prompt("Enter customer name: ");
        String email = prompt("Enter email: ");
        String phone = prompt("Enter 10-digit phone: ");
        int id = bank.addCustomer(name, email, phone);
        System.out.println("Customer added successfully. Customer ID: " + id);
    }

    private static void viewCustomers() {
        List<Customer> list = bank.getCustomers();
        if (list.isEmpty()) System.out.println("No customers found.");
        list.forEach(System.out::println);
    }

    private static void openAccount() {
        int customerId = InputValidator.parseInt(prompt("Enter customer ID: "));
        String type = prompt("Account type (SAVINGS/CURRENT): ");
        double deposit = InputValidator.parseAmount(prompt("Opening deposit: "));
        int no = bank.openAccount(customerId, type, deposit);
        System.out.println("Account opened successfully. Account No: " + no);
    }

    private static void viewAccounts() {
        List<Account> list = bank.getAccounts();
        if (list.isEmpty()) System.out.println("No accounts found.");
        list.forEach(System.out::println);
    }

    private static void deposit() {
        int no = InputValidator.parseInt(prompt("Enter account number: "));
        double amt = InputValidator.parseAmount(prompt("Enter amount: "));
        double bal = bank.deposit(no, amt);
        System.out.printf("Deposit successful. New balance: %.2f%n", bal);
    }

    private static void withdraw() throws InsufficientFundsException {
        int no = InputValidator.parseInt(prompt("Enter account number: "));
        double amt = InputValidator.parseAmount(prompt("Enter amount: "));
        double bal = bank.withdraw(no, amt);
        System.out.printf("Withdrawal successful. New balance: %.2f%n", bal);
    }

    private static void transfer() throws InsufficientFundsException {
        int from = InputValidator.parseInt(prompt("From account number: "));
        int to = InputValidator.parseInt(prompt("To account number: "));
        double amt = InputValidator.parseAmount(prompt("Enter amount: "));
        bank.transfer(from, to, amt);
        System.out.println("Transfer successful.");
    }

    private static void checkBalance() {
        int no = InputValidator.parseInt(prompt("Enter account number: "));
        System.out.printf("Current balance: %.2f%n", bank.getBalance(no));
    }

    private static void miniStatement() {
        int no = InputValidator.parseInt(prompt("Enter account number: "));
        List<Transaction> list = bank.getStatement(no, 5);
        if (list.isEmpty()) System.out.println("No transactions found.");
        list.forEach(System.out::println);
    }
}
