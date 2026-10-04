# BankEase – Bank Management System (Java + SQLite)

Command-line banking application: customers, accounts, deposit, withdraw,
transfer, balance enquiry and mini statement. Data is stored in SQLite via JDBC.

## Setup
Put these jars in `lib/`:
- sqlite-jdbc (Xerial)  
- junit-platform-console-standalone (for tests)

## Build & run (Windows uses `;` instead of `:` in classpaths)
```
javac -cp "lib/*" -d out src/com/bankease/model/*.java src/com/bankease/util/*.java src/com/bankease/repository/*.java src/com/bankease/service/*.java src/com/bankease/ui/*.java
java -cp "out:lib/*" com.bankease.ui.Main
```

## Run tests
```
javac -cp "lib/*:out" -d out-test test/com/bankease/service/BankServiceTest.java
java -jar lib/junit-platform-console-standalone-*.jar execute --class-path "out:out-test:lib/sqlite-jdbc-*.jar" --scan-class-path
```
