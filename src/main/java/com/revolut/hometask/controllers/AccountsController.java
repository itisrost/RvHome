package com.revolut.hometask.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.revolut.hometask.dao.AccountsDao;
import lombok.RequiredArgsConstructor;
import com.revolut.hometask.model.Account;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import org.apache.commons.lang3.StringUtils;
import spark.Request;

@RequiredArgsConstructor
public class AccountsController {

    private final AccountsDao accountsDao;

    public BaseResponse getAccountById(String id) {
        Account account;

        if (StringUtils.isNumeric(id)) {
            try {
                account = accountsDao.getAccountById(Integer.parseInt(id));
            } catch (SQLException e) {
                e.printStackTrace();
                return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
            }
        } else {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Account id must be a number!", null);
        }

        if (account == null) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Account with id=" + id + " not found.", null);
        }

        return new BaseResponse(ResponseStatusEnum.SUCCESS, null, account);
    }

    public BaseResponse getAccounts() {
        try {
            return new BaseResponse(ResponseStatusEnum.SUCCESS, null, accountsDao.getAccounts());
        } catch (SQLException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        }
    }

    public BaseResponse createAccount(Request req) {
        List<String> nullParams = req.queryMap().toMap().values().stream()
                .map(s -> s[0])
                .filter(StringUtils::isBlank)
                .collect(Collectors.toList());

        if (req.queryMap().toMap().keySet().size() != 3 || !nullParams.isEmpty()) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Not enough parameters.", null);
        }

        try {
            accountsDao.addAccount(req.queryParams("owner"), new BigDecimal(req.queryParams("balance")), req.queryParams("currency"));
            return new BaseResponse(ResponseStatusEnum.SUCCESS, "Account saved", null);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, "Balance should be a number!", null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, "SQL Exception", null);
        }
    }
}
