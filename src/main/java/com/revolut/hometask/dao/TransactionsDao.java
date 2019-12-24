package com.revolut.hometask.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import com.revolut.hometask.model.Account;
import com.revolut.hometask.model.Transaction;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public class TransactionsDao {

    private final String TRANSACTION_INSERT_QUERY = "INSERT INTO transactions VALUES(NULL,{0},{1},{2},{3})";
    private final String SELECT_ALL_TRANSACTIONS = "SELECT * FROM transactions";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection connection;
    private AccountsDao accountsDao;

    public void makeTransaction(int debitAccountId, int creditAccountId, BigDecimal amount) throws SQLException, IllegalArgumentException {
        Account debitAccount = accountsDao.getAccountById(debitAccountId);
        if (debitAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Requested transfer amount exceeds account balance.");
        }
        Account creditAccount = accountsDao.getAccountById(creditAccountId);
        if (!StringUtils.equals(debitAccount.getCurrency(), creditAccount.getCurrency())) {
            throw new IllegalArgumentException("Requested accounts have different currencies.");
        }
//        String debtQuery = MessageFormat.format(UPDATE_BALANCE_BY_ID, accountFrom.getBalance().subtract(amount).toString(), accountFromId);
//        String credQuery = MessageFormat.format(UPDATE_BALANCE_BY_ID, accountTo.getBalance().add(amount).toString(), accountToId);

        accountsDao.updateBalance(debitAccountId, debitAccount.getBalance().subtract(amount));
        accountsDao.updateBalance(creditAccountId, debitAccount.getBalance().add(amount));
        String createTransactionQuery = MessageFormat.format(TRANSACTION_INSERT_QUERY, debitAccountId, creditAccountId, amount.toString(), dateTimeToString(LocalDateTime.now()));

        Statement statement = connection.createStatement();
        statement.execute(createTransactionQuery);
        statement.close();
    }


    public Collection<Transaction> getTransactions() throws SQLException {
        Collection<Transaction> result = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(SELECT_ALL_TRANSACTIONS);
        while (rs.next()) {
            result.add(new Transaction(rs.getInt("transaction_id"),
                    rs.getInt("debit_account"),
                    rs.getInt("credit_account"),
                    rs.getBigDecimal("amount"),
                    dateTimeFromString(rs.getString("date"))));
        }
        rs.close();
        statement.closeOnCompletion();
        return result;
    }

    private String dateTimeToString(LocalDateTime dateTime) {
        return "'" + dateTimeFormatter.format(dateTime) + "'";
    }

    private LocalDateTime dateTimeFromString (String value){
        return LocalDateTime.parse(value, dateTimeFormatter);
    }
}
