package com.revolut.hometask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.revolut.hometask.controllers.AccountsController;
import com.revolut.hometask.controllers.TransactionsController;
import com.revolut.hometask.dao.AccountsDao;
import com.revolut.hometask.dao.TransactionsDao;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import static spark.Spark.*;

public class RvlService {

    private static ObjectMapper objectMapper = getObjectMapper();

    private static AccountsController accountsController;
    private static TransactionsController transactionsController;

    public static void main(String[] args) {

        Connection connection = getConnection();

        accountsController = new AccountsController(new AccountsDao(connection));
        transactionsController = new TransactionsController(new TransactionsDao(connection, new AccountsDao(connection)));

        try {
            Connection liquibaseConnection = getConnection();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(liquibaseConnection));
            Liquibase liquibase = new liquibase.Liquibase("db.changelog/create-tables.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
            if (liquibaseConnection != null) {
                liquibaseConnection.close();
            }

            get("/accounts", (req, res) -> {
                res.type("application/json");
                return writeJson(accountsController.getAccounts());
            });

            get("/accounts/:id", (req, res) -> {
                res.type("application/json");
                return writeJson(accountsController.getAccountById(req.params(":id")));
            });

            post("/accounts", (req, res) -> {
                res.type("application/json");
                return writeJson(accountsController.createAccount(req.queryParams("owner"),
                        req.queryParams("balance"),
                        req.queryParams("currency")));
            });

            get("/transactions", (req, res) -> {
                res.type("application/json");
                return writeJson(transactionsController.getTransactions());
            });

            get("/transactions/:id", (req, res) -> {
                res.type("application/json");
                return writeJson(transactionsController.getTransactionById(req.params(":id")));
            });

            get("/transactions/account/:id", (req, res) -> {
                res.type("application/json");
                return writeJson(transactionsController.getTransactionsByAccountId(req.params(":id")));
            });

            post("/transactions", (req, res) -> {
                res.type("application/json");
                return writeJson(transactionsController.createTransaction(req.queryParams("debitAccountId"),
                        req.queryParams("creditAccountId"),
                        req.queryParams("amount")));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UTILS
    private static Connection getConnection() {
        String url = "jdbc:h2:mem:test";
        String user = "sa";
        String passwd = "sa";
        try {
            Connection connection = DriverManager.getConnection(url, user, passwd);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String writeJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not convert object to JSON", e);
        }
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}