package com.revolut.hometask.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;

import com.revolut.hometask.model.Account;
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


public class AccountsDaoTest {

    Connection connection;
    AccountsDao accountsDao;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "sa");
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new liquibase.Liquibase("db.changelog/create-tables-with-data.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
        accountsDao = new AccountsDao(connection);
    }

    @Test
    public void getAccounts_Success() throws Exception {
        Collection<Account> result = accountsDao.getAccounts();
        assertEquals(3, result.size());
    }

    @Test
    public void getAccountById_NotExisting() throws Exception {
        Account result = accountsDao.getAccountById(10);
        assertNull(result);
    }

    @Test
    public void getAccountById_Success() throws Exception {
        Account result = accountsDao.getAccountById(1);
        assertEquals("Rost Morozov", result.getOwner());
        assertEquals(0, result.getBalance().compareTo(new BigDecimal(10000)));
        assertEquals("RUR", result.getCurrency());
    }

    @Test
    public void addAccount_Success() throws Exception {
        Account newAccount = new Account(4, "Ivan Ivanov", new BigDecimal(10000), "RUB");
        accountsDao.addAccount(newAccount.getOwner(), newAccount.getBalance(), newAccount.getCurrency());

        Account result = accountsDao.getAccountById(4);
        assertEquals(newAccount.getOwner(), result.getOwner());
        assertEquals(0, result.getBalance().compareTo(newAccount.getBalance()));
        assertEquals(newAccount.getCurrency(), result.getCurrency());
    }

    @After
    public void finish() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}