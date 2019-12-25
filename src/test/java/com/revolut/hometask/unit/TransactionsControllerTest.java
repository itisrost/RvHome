package com.revolut.hometask.unit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.revolut.hometask.controllers.TransactionsController;
import com.revolut.hometask.dao.TransactionsDao;
import com.revolut.hometask.model.Transaction;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

public class TransactionsControllerTest {

    TransactionsDao transactionsDaoMock = Mockito.mock(TransactionsDao.class);
    TransactionsController transactionsController = new TransactionsController(transactionsDaoMock);
    
    @Test
    public void getTransactionWithNotNumericId() {
        BaseResponse response = transactionsController.getTransactionById("aaa");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Transaction id must be a number!", response.getMessage());
    }

    @Test
    public void getNotExistingTransaction() {
        BaseResponse response = transactionsController.getTransactionById("2");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Transaction with id=2 not found.", response.getMessage());
    }

    @Test
    public void getTransactionSuccess() throws Exception {
        Transaction transaction = new Transaction(1, 2, 3, new BigDecimal(10000), LocalDateTime.now());
        Answer<Transaction> transactionAnswer = invocation -> transaction;

        Mockito.when(transactionsDaoMock.getTransactionById(Mockito.intThat(t -> t == 1))).then(transactionAnswer);

        BaseResponse response = transactionsController.getTransactionById("1");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertNull(response.getMessage());
        assertEquals(transaction, response.getData());
    }

    @Test
    public void createTransactionWithoutDebitAccount() {
        BaseResponse response = transactionsController.createTransaction(null, "2", "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter debitAccount is empty.", response.getMessage());
    }

    @Test
    public void createTransactionWithoutCreditAccount() {
        BaseResponse response = transactionsController.createTransaction("1", null, "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter creditAccount is empty.", response.getMessage());
    }

    @Test
    public void createTransactionWithoutAmount() {
        BaseResponse response = transactionsController.createTransaction("1", "2", null);
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter amount is empty.", response.getMessage());
    }

    @Test
    public void createAccountWithNotNumericDebitAccount() {
        BaseResponse response = transactionsController.createTransaction("aaa", "2", "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("All parameters should be numbers!", response.getMessage());
    }

    @Test
    public void createAccountWithNotNumericCreditAccount() {
        BaseResponse response = transactionsController.createTransaction("1", "aaa", "1000");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("All parameters should be numbers!", response.getMessage());
    }

    @Test
    public void createAccountWithNotNumericAmount() {
        BaseResponse response = transactionsController.createTransaction("1", "2", "aaa");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("All parameters should be numbers!", response.getMessage());
    }

    @Test
    public void createAccountSuccess() {
        BaseResponse response = transactionsController.createTransaction("1", "2", "1000");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertEquals("Transaction saved.", response.getMessage());
    }
}