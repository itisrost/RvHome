package dao;

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
import model.Account;
import model.Transaction;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public class TransactionsDao {

    private static final String TRANSACTION_INSERT_QUERY = "INSERT INTO transactions VALUES(NULL,{0},{1},{2},{3})";

    private Connection connection;
    private AccountsDao accountsDao;

    public void makeTransaction(int accountFromId, int accountToId, BigDecimal amount) throws SQLException, IllegalArgumentException {
        Account accountFrom = accountsDao.getAccountById(accountFromId);
        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Requested transfer amount exceeds account balance.");
        }
        Account accountTo = accountsDao.getAccountById(accountToId);
        if (!StringUtils.equals(accountFrom.getCurrency(), accountTo.getCurrency())) {
            throw new IllegalArgumentException("Requested accounts have different currencies.");
        }
//        String debtQuery = MessageFormat.format(UPDATE_BALANCE_BY_ID, accountFrom.getBalance().subtract(amount).toString(), accountFromId);
//        String credQuery = MessageFormat.format(UPDATE_BALANCE_BY_ID, accountTo.getBalance().add(amount).toString(), accountToId);

        accountsDao.updateBalance(accountFromId, accountFrom.getBalance().subtract(amount));
        accountsDao.updateBalance(accountToId, accountFrom.getBalance().add(amount));
        String createTransactionQuery = MessageFormat.format(TRANSACTION_INSERT_QUERY, accountFromId, accountToId, amount.toString(), dateTimeToString(LocalDateTime.now()));

        Statement statement = connection.createStatement();
        statement.execute(createTransactionQuery);
        statement.close();
    }


//    public Collection<Transaction> getTransactions() throws SQLException {
//        Collection<Account> result = new ArrayList<>();
//        Statement statement = connection.createStatement();
//        ResultSet rs = statement.executeQuery(SELECT_ALL_ACCOUNTS);
//        while (rs.next()) {
//            result.add(new Account(rs.getInt("account_id"),
//                    rs.getString("owner"),
//                    rs.getBigDecimal("balance"),
//                    rs.getString("currency")));
//        }
//        rs.close();
//        statement.closeOnCompletion();
//        return result;
//    }

    private static String dateTimeToString(LocalDateTime dateTime) {
        return "'" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime) + "'";
    }
}
