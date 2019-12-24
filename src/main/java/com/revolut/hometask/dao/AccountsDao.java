package com.revolut.hometask.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import com.revolut.hometask.model.Account;

@AllArgsConstructor
public class AccountsDao {

    private final String ACCOUNT_INSERT_QUERY = "INSERT INTO accounts VALUES(NULL,{0},{1},{2})";

    private final String SELECT_ALL_ACCOUNTS = "SELECT * FROM accounts";

    private final String SELECT_ACC_BY_ID = "SELECT * FROM accounts where account_id = {0}";

    private final String UPDATE_BALANCE_BY_ID = "UPDATE accounts SET balance = {0} WHERE account_id = {1}";

    private Connection connection;

    public void addAccount(String owner, BigDecimal balance, String currency) throws SQLException {
        String query = MessageFormat.format(ACCOUNT_INSERT_QUERY, surroundWithApostrophe(owner), balance.toString(), surroundWithApostrophe(currency));
        Statement statement = connection.createStatement();
        statement.execute(query);
        statement.close();
    }

    public Account getAccountById(int id) throws SQLException {
        Account result = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(MessageFormat.format(SELECT_ACC_BY_ID, id));
        while (rs.next()) {
            result = new Account(rs.getInt("account_id"),
                    rs.getString("owner"),
                    rs.getBigDecimal("balance"),
                    rs.getString("currency"));
        }
        rs.close();
        statement.closeOnCompletion();

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
        statement.closeOnCompletion();
        return result;
    }

    public void updateBalance(int accountId, BigDecimal amount) throws SQLException, IllegalArgumentException {
        String query = MessageFormat.format(UPDATE_BALANCE_BY_ID, amount.toString(), accountId);
        Statement statement = connection.createStatement();
        statement.execute(query);
        statement.close();
    }

    private String surroundWithApostrophe(String string) {
        return "'" + string + "'";
    }
}
