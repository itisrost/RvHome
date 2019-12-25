package com.revolut.hometask.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.revolut.hometask.dao.TransactionsDao;
import com.revolut.hometask.model.Transaction;
import lombok.RequiredArgsConstructor;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionsDao transactionsDao;

    public BaseResponse getTransactions() {
        try {
            return new BaseResponse(ResponseStatusEnum.SUCCESS, null, transactionsDao.getTransactions());
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        }
    }

    public BaseResponse getTransactionById(String id) {
        Transaction transaction;

        try {
            transaction = transactionsDao.getTransactionById(Integer.parseInt(id));
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        } catch (NumberFormatException e) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Transaction id must be a number!", null);
        }

        if (transaction == null) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Transaction with id=" + id + " not found.", null);
        }

        return new BaseResponse(ResponseStatusEnum.SUCCESS, null, transaction);
    }

    public BaseResponse createTransaction(String debitAccount, String creditAccount, String amount) {

        if (StringUtils.isBlank(debitAccount)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter debitAccount is empty.", null);
        } else if (StringUtils.isBlank(creditAccount)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter creditAccount is empty.", null);
        } else if (StringUtils.isBlank(amount)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter amount is empty.", null);
        }

        try {
            transactionsDao.makeTransaction(Integer.parseInt(debitAccount), Integer.parseInt(creditAccount), new BigDecimal(amount));
            log.info("Transaction created");
            return new BaseResponse(ResponseStatusEnum.SUCCESS, "Transaction saved.", null);
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, "SQLException " + e.getMessage(), null);
        } catch (NumberFormatException e) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "All parameters should be numbers!", null);
        } catch (IllegalArgumentException e) {
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        }
    }
}