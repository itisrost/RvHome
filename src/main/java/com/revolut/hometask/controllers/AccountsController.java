package com.revolut.hometask.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.revolut.hometask.dao.AccountsDao;
import lombok.RequiredArgsConstructor;
import com.revolut.hometask.model.Account;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class AccountsController {

    private final AccountsDao accountsDao;

    public BaseResponse getAccounts() {
        try {
            return new BaseResponse(ResponseStatusEnum.SUCCESS, null, accountsDao.getAccounts());
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        }
    }

    public BaseResponse getAccountById(String id) {
        Account account;

        try {
            account = accountsDao.getAccountById(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Account id must be a number!", null);
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, "SQLException " + e.getMessage(), null);
        }

        if (account == null) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Account with id=" + id + " not found.", null);
        }

        return new BaseResponse(ResponseStatusEnum.SUCCESS, null, account);
    }

    public BaseResponse createAccount(String owner, String balance, String currency) {

        if (StringUtils.isBlank(owner)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter owner is empty.", null);
        } else if (StringUtils.isBlank(balance)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter balance is empty.", null);
        } else if (StringUtils.isBlank(currency)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter currency is empty.", null);
        }

        try {
            accountsDao.addAccount(owner, new BigDecimal(balance), currency);
            log.info("Account created");
            return new BaseResponse(ResponseStatusEnum.SUCCESS, "Account saved.", null);
        } catch (NumberFormatException e) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Balance should be a number!", null);
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, "SQL Exception " + e.getMessage(), null);
        }
    }
}