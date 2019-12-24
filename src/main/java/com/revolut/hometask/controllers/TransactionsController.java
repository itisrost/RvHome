package com.revolut.hometask.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.revolut.hometask.dao.TransactionsDao;
import lombok.RequiredArgsConstructor;
import com.revolut.hometask.model.BaseResponse;
import com.revolut.hometask.model.ResponseStatusEnum;
import org.apache.commons.lang3.StringUtils;
import spark.Request;

@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionsDao transactionsDao;

    public BaseResponse createTransaction(Request req){

        List<String> nullParams = req.queryMap().toMap().values().stream()
                .map(s -> s[0])
                .filter(StringUtils::isBlank)
                .collect(Collectors.toList());

        if (req.queryMap().toMap().keySet().size() != 3 || !nullParams.isEmpty()) {
            return new BaseResponse(ResponseStatusEnum.ERROR, "Not enough parameters.", null);
        }

        try {
            transactionsDao.makeTransaction(Integer.parseInt(req.queryParams("accountFrom")), Integer.parseInt(req.queryParams("accountTo")), new BigDecimal(req.queryParams("amount")));
            return new BaseResponse(ResponseStatusEnum.SUCCESS, "Transaction saved", null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, "SQLException", null);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, "All parameters should be numbers!", null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        }
    }

    public BaseResponse getTransactions() {
        try {
            return new BaseResponse(ResponseStatusEnum.SUCCESS, null, transactionsDao.getTransactions());
        } catch (SQLException e) {
            e.printStackTrace();
            return new BaseResponse(ResponseStatusEnum.ERROR, e.getMessage(), null);
        }
    }
}
