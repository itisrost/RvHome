package com.revolut.hometask.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Account {
    private int accountId;
    private String owner;
    private BigDecimal balance;
    private String currency;
}
