package com.banking.model;

import com.banking.model.Transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionTest {

    @Test
    void transferSetsAllFields() {
        Transaction tx = new Transaction("NL01", "NL02", new BigDecimal("100.00"),
                "user@test.com", "Test payment", TransactionType.TRANSFER);

        assertEquals("NL01", tx.getFromIban());
        assertEquals("NL02", tx.getToIban());
        assertEquals(new BigDecimal("100.00"), tx.getAmount());
        assertEquals("user@test.com", tx.getPerformedBy());
        assertEquals("Test payment", tx.getDescription());
        assertEquals(TransactionType.TRANSFER, tx.getTransactionType());
        assertNotNull(tx.getTimestamp());
    }

    @Test
    void depositHasNullFromIban() {
        Transaction tx = new Transaction(null, "NL02", new BigDecimal("50.00"),
                "user@test.com", "ATM deposit", TransactionType.DEPOSIT);

        assertNull(tx.getFromIban());
        assertEquals("NL02", tx.getToIban());
        assertEquals(TransactionType.DEPOSIT, tx.getTransactionType());
    }

    @Test
    void withdrawalHasNullToIban() {
        Transaction tx = new Transaction("NL01", null, new BigDecimal("50.00"),
                "user@test.com", "ATM withdrawal", TransactionType.WITHDRAWAL);

        assertEquals("NL01", tx.getFromIban());
        assertNull(tx.getToIban());
        assertEquals(TransactionType.WITHDRAWAL, tx.getTransactionType());
    }
}
