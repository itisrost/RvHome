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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class RvlService {

    private static final Logger logger = LoggerFactory.getLogger(RvlService.class);

    private static ObjectMapper objectMapper = getObjectMapper();

    public static void main(String[] args) {

        Connection connection = getConnection();

        AccountsController accountsController = new AccountsController(new AccountsDao(connection));
        TransactionsController transactionsController = new TransactionsController(new TransactionsDao(connection, new AccountsDao(connection)));

        try {

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase("db.changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());

        } catch (Exception e) {
            e.printStackTrace();
        }

        get("/accounts/:id", (req, res) -> {
            res.type("application/json");
            return writeJson(accountsController.getAccountById(req.params(":id")));
        });

        get("/accounts", (req, res) -> {
            res.type("application/json");
            return writeJson(accountsController.getAccounts());
        });

        post("/accounts", (req, res) -> {
            res.type("application/json");
            return writeJson(accountsController.createAccount(req));
        });

        get("/transactions", (req, res) -> {
            res.type("application/json");
            return writeJson(transactionsController.getTransactions());
        });

        post("/transactions", (req, res) -> {
            res.type("application/json");
            return writeJson(transactionsController.createTransaction(req));
        });
    }

    // UTILS
    private static Connection getConnection() {
        String url = "jdbc:h2:mem:test";
        String user = "sa";
        String passwd = "sa";
        try {
            return DriverManager.getConnection(url, user, passwd);
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

    private static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
