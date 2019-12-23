package model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class Account {
    private int accountId;
    private String owner;
    private BigDecimal balance;
    private String currency;
}
