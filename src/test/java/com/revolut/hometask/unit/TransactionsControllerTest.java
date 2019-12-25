package com.revolut.hometask.unit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import com.revolut.hometask.controllers.TransactionsController;
import com.revolut.hometask.dao.TransactionsDao;
import com.revolut.hometask.model.Transaction;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TransactionsControllerTest {

    TransactionsController transactionsController;

    @Mock
    TransactionsDao transactionsDaoMock;

    @Before
    public void setUp() {
        initMocks(this);
        transactionsController = new TransactionsController(transactionsDaoMock);
    }
    
    @Test
    public void getTransactionById_WithNotNumericId() {
        BaseResponse response = transactionsController.getTransactionById("aaa");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Transaction id must be a number!", response.getMessage());
    }

    @Test
    public void getTransactionById_NotExisting() {
        BaseResponse response = transactionsController.getTransactionById("2");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Transaction with id=2 not found.", response.getMessage());
    }

    @Test
    public void getTransactionById_Success() throws Exception {
        Transaction transaction = new Transaction(1, 2, 3, new BigDecimal(10000), "RUR", LocalDateTime.now());
        Answer<Transaction> transactionAnswer = invocation -> transaction;

        Mockito.when(transactionsDaoMock.getTransactionById(Mockito.intThat(t -> t == 1))).then(transactionAnswer);

        BaseResponse response = transactionsController.getTransactionById("1");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertNull(response.getMessage());
        assertEquals(transaction, response.getData());
    }

    @Test
    public void getTransactionsByAccountId_WithNotNumericId() {
        BaseResponse response = transactionsController.getTransactionsByAccountId("aaa");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Account id must be a number!", response.getMessage());
    }

    @Test
    public void getTransactionsByAccountId_NotExisting() {
        BaseResponse response = transactionsController.getTransactionsByAccountId("5");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Transactions from/to account with id=5 not found.", response.getMessage());
    }

    @Test
    public void getTransactionsByAccountId_Success() throws Exception {
        Collection<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(1, 2, 3, new BigDecimal(10000), "RUR", LocalDateTime.now()));
        transactions.add(new Transaction(2, 1, 3, new BigDecimal(10000), "RUR", LocalDateTime.now()));

        Answer<Collection<Transaction>> transactionAnswer = invocation -> transactions;

        Mockito.when(transactionsDaoMock.getTransactionsByAccountId(Mockito.intThat(t -> t == 3))).then(transactionAnswer);

        BaseResponse response = transactionsController.getTransactionsByAccountId("3");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertNull(response.getMessage());
        assertEquals(transactions, response.getData());
    }

    @Test
    public void createTransaction_WithoutDebitAccount() {
        BaseResponse response = transactionsController.createTransaction(null, "2", "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter debitAccountId is empty.", response.getMessage());
    }

    @Test
    public void createTransaction_WithoutCreditAccount() {
        BaseResponse response = transactionsController.createTransaction("1", null, "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter creditAccountId is empty.", response.getMessage());
    }

    @Test
    public void createTransaction_WithoutAmount() {
        BaseResponse response = transactionsController.createTransaction("1", "2", null);
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter amount is empty.", response.getMessage());
    }

    @Test
    public void createAccount_WithNotNumericDebitAccount() {
        BaseResponse response = transactionsController.createTransaction("aaa", "2", "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("All parameters should be numbers!", response.getMessage());
    }

    @Test
    public void createAccount_WithNotNumericCreditAccount() {
        BaseResponse response = transactionsController.createTransaction("1", "aaa", "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("All parameters should be numbers!", response.getMessage());
    }

    @Test
    public void createAccount_WithNotNumericAmount() {
        BaseResponse response = transactionsController.createTransaction("1", "2", "aaa");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("All parameters should be numbers!", response.getMessage());
    }

    @Test
    public void createAccount_Success() {
        BaseResponse response = transactionsController.createTransaction("1", "2", "1000");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertEquals("Transaction saved.", response.getMessage());
    }
}