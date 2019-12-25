package com.revolut.hometask.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import com.revolut.hometask.model.Account;

@AllArgsConstructor
public class AccountsDao {

    private final String ACCOUNT_INSERT_QUERY = "INSERT INTO accounts VALUES(NULL,?,?,?)";

    private final String SELECT_ALL_ACCOUNTS = "SELECT * FROM accounts";

    private final String SELECT_ACC_BY_ID = "SELECT * FROM accounts where account_id = ?";

    private final String UPDATE_BALANCE_BY_ID = "UPDATE accounts SET balance = ? WHERE account_id = ?";

    private Connection connection;

    public void addAccount(String owner, BigDecimal balance, String currency) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(ACCOUNT_INSERT_QUERY);
        preparedStatement.setString(1, owner);
        preparedStatement.setBigDecimal(2, balance);
        preparedStatement.setString(3, currency);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public Account getAccountById(int id) throws SQLException {
        Account result = null;

        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ACC_BY_ID);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            result = new Account(rs.getInt("account_id"),
                    rs.getString("owner"),
                    rs.getBigDecimal("balance"),
                    rs.getString("currency"));
        }
        rs.close();
        preparedStatement.close();

        return result;
    }

    public Collection<Account> getAccounts() throws SQLException {
        Collection<Account> result = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(SELECT_ALL_ACCOUNTS);
        while (rs.next()) {
            result.add(new Account(rs.getInt("account_id"),
                    rs.getString("owner"),
                    rs.getBigDecimal("balance"),
                    rs.getString("currency")));
        }
        rs.close();
        statement.close();
        return result;
    }

    public void updateBalance(int accountId, BigDecimal amount) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BALANCE_BY_ID);
        preparedStatement.setBigDecimal(1, amount);
        preparedStatement.setInt(2, accountId);
        int updated = preparedStatement.executeUpdate();
        preparedStatement.close();
        if (updated != 1) {
            throw new SQLException("No accs updated");
        }
    }
}