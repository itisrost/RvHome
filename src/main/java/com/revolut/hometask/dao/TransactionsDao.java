package com.revolut.hometask.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    private final String TRANSACTION_INSERT_QUERY = "INSERT INTO transactions VALUES(NULL,?,?,?,?)";

    private final String SELECT_ALL_TRANSACTIONS = "SELECT * FROM transactions";

    private final String SELECT_TR_BY_ID = "SELECT * FROM transactions where transaction_id = ?";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection connection;
    private AccountsDao accountsDao;

    public void makeTransaction(int debitAccountId, int creditAccountId, BigDecimal amount) throws SQLException, IllegalArgumentException {
        Account debitAccount = accountsDao.getAccountById(debitAccountId);
        if (debitAccount == null) {
            throw new IllegalArgumentException("Requested debit account does not exist.");
        }
        if (debitAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Requested transfer amount exceeds account balance.");
        }

        Account creditAccount = accountsDao.getAccountById(creditAccountId);
        if (creditAccount == null) {
            throw new IllegalArgumentException("Requested credit account does not exist.");
        }
        if (!StringUtils.equals(debitAccount.getCurrency(), creditAccount.getCurrency())) {
            throw new IllegalArgumentException("Requested accounts have different currencies.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement(TRANSACTION_INSERT_QUERY);
        preparedStatement.setInt(1, debitAccountId);
        preparedStatement.setInt(2, creditAccountId);
        preparedStatement.setBigDecimal(3, amount);
        preparedStatement.setString(4, dateTimeFormatter.format(LocalDateTime.now()));

        try {
            connection.setAutoCommit(false);
            accountsDao.updateBalance(debitAccountId, debitAccount.getBalance().subtract(amount));
            accountsDao.updateBalance(creditAccountId, debitAccount.getBalance().add(amount));
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        } finally {
            preparedStatement.close();
            connection.setAutoCommit(true);
        }
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

    public Transaction getTransactionById(int id) throws SQLException {
        Transaction result = null;

        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TR_BY_ID);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            result = new Transaction(rs.getInt("transaction_id"),
                    rs.getInt("debit_account"),
                    rs.getInt("credit_account"),
                    rs.getBigDecimal("amount"),
                    dateTimeFromString(rs.getString("date")));
        }
        rs.close();
        preparedStatement.close();

        return result;
    }

    private LocalDateTime dateTimeFromString(String value) {
        return LocalDateTime.parse(value, dateTimeFormatter);
    }
}