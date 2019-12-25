package com.revolut.hometask.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;

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

    public BaseResponse getTransactionsByAccountId(String id) {
        Collection<Transaction> transactions;

        try {
            transactions = transactionsDao.getTransactionsByAccountId(Integer.parseInt(id));
        } catch (SQLException e) {
            log.error("SQLException", e);
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        } catch (NumberFormatException e) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Account id must be a number!", null);
        }

        if (transactions.isEmpty()) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Transactions from/to account with id=" + id + " not found.", null);
        }

        return new BaseResponse(ResponseStatusEnum.SUCCESS, null, transactions);
    }

    public BaseResponse createTransaction(String debitAccountId, String creditAccountId, String amount) {

        if (StringUtils.isBlank(debitAccountId)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter debitAccountId is empty.", null);
        } else if (StringUtils.isBlank(creditAccountId)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter creditAccountId is empty.", null);
        } else if (StringUtils.isBlank(amount)) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Query parameter amount is empty.", null);
        }

        try {
            transactionsDao.addTransaction(Integer.parseInt(debitAccountId), Integer.parseInt(creditAccountId), new BigDecimal(amount));
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