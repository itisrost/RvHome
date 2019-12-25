package com.revolut.hometask.unit;

import java.math.BigDecimal;

import com.revolut.hometask.controllers.AccountsController;
import com.revolut.hometask.dao.AccountsDao;
import com.revolut.hometask.model.Account;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AccountsControllerTest {

    AccountsController accountsController;

    @Mock
    AccountsDao accountsDaoMock;

    @Before
    public void setUp() {
        initMocks(this);
        accountsController = new AccountsController(accountsDaoMock);
    }

    @Test
    public void getAccountById_WithNotNumericId() {
        BaseResponse response = accountsController.getAccountById("aaa");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Account id must be a number!", response.getMessage());
    }

    @Test
    public void getAccountById_NotExisting() {
        BaseResponse response = accountsController.getAccountById("2");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Account with id=2 not found.", response.getMessage());
    }

    @Test
    public void getAccountById_Success() throws Exception {
        Account account = new Account(1, "Ivan Ivanov", new BigDecimal(10000), "RUB");
        Answer<Account> accountAnswer = invocation -> account;

        Mockito.when(accountsDaoMock.getAccountById(Mockito.intThat(t -> t == 1))).then(accountAnswer);

        BaseResponse response = accountsController.getAccountById("1");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertNull(response.getMessage());
        assertEquals(account, response.getData());
    }

    @Test
    public void createAccount_WithoutOwner() {
        BaseResponse response = accountsController.createAccount("Ivan Ivanov", "10000", null);
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter currency is empty.", response.getMessage());
    }

    @Test
    public void createAccount_WithoutBalance() {
        BaseResponse response = accountsController.createAccount("Ivan Ivanov", null, "RUB");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter balance is empty.", response.getMessage());
    }

    @Test
    public void createAccount_WithoutCurrency() {
        BaseResponse response = accountsController.createAccount(null, "10000", "RUB");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Query parameter owner is empty.", response.getMessage());
    }

    @Test
    public void createAccount_WithNotNumericBalance() {
        BaseResponse response = accountsController.createAccount("Ivan Ivanov", "a lot!", "RUB");
        assertEquals(ResponseStatusEnum.ERROR, response.getStatus());
        assertEquals("Balance should be a number!", response.getMessage());
    }

    @Test
    public void createAccount_Success() {
        BaseResponse response = accountsController.createAccount("Ivan Ivanov", "10000", "RUB");
        assertEquals(ResponseStatusEnum.SUCCESS, response.getStatus());
        assertEquals("Account saved.", response.getMessage());
    }
}