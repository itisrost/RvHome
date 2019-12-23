package model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class Transaction {
    private Integer transactionId;
    private Integer accountFrom;
    private Integer accountTo;
    private BigDecimal amount;
    private Instant date;
}
