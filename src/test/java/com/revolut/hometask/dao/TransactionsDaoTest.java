package com.revolut.hometask.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import com.revolut.hometask.model.Transaction;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class TransactionsDaoTest {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    Connection connection;
    AccountsDao accountsDao;
    TransactionsDao transactionsDao;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "sa");
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase("db.changelog/create-tables-with-data.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
        accountsDao = new AccountsDao(connection);
        transactionsDao = new TransactionsDao(connection, accountsDao);
    }

    @Test
    public void getTransactions_Success() throws Exception {
        Collection<Transaction> result = transactionsDao.getTransactions();
        assertEquals(2, result.size());
    }

    @Test
    public void getTransactionById_NotExisting() throws Exception {
        Transaction result = transactionsDao.getTransactionById(10);
        assertNull(result);
    }

    @Test
    public void getTransactionById_Success() throws Exception {
        Transaction result = transactionsDao.getTransactionById(1);
        assertEquals(1, result.getDebitAccount());
        assertEquals(2, result.getCreditAccount());
        assertEquals(0, result.getAmount().compareTo(new BigDecimal(2000)));
        assertEquals(LocalDateTime.parse("2019-12-25 10:00:00", dateTimeFormatter),  result.getDate());
    }

    @Test
    public void getTransactionByAccountId_NotExisting() throws Exception {
        Collection<Transaction> result = transactionsDao.getTransactionsByAccountId(10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTransactionByAccountId_Success() throws Exception {
        Collection<Transaction> result = transactionsDao.getTransactionsByAccountId(2);
        assertEquals(2, result.size());
    }

    @Test
    public void addTransaction_Success() throws Exception {
        Transaction newTransaction = new Transaction(3, 2, 3, new BigDecimal(10000), "RUR", LocalDateTime.now());
        transactionsDao.addTransaction(newTransaction.getDebitAccount(), newTransaction.getCreditAccount(), newTransaction.getAmount());

        Transaction result = transactionsDao.getTransactionById(3);
        assertEquals(newTransaction.getDebitAccount(), result.getDebitAccount());
        assertEquals(newTransaction.getCreditAccount(), result.getCreditAccount());
        assertEquals(0, result.getAmount().compareTo(newTransaction.getAmount()));
    }

    @After
    public void finish() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}