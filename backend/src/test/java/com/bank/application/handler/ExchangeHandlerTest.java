package com.bank.application.handler;

import com.bank.application.command.ExchangeCommand;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.ExternalValidationPort;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.exception.SameAccountExchangeException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.model.TransactionType;
import com.bank.domain.service.ExchangeRateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExchangeHandlerTest {

    private AccountRepositoryPort accounts;
    private ExchangeRateProvider rates;
    private ApplicationEventPublisher events;
    private ExternalValidationPort externalValidation;
    private ExchangeHandler handler;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepositoryPort.class);
        rates = mock(ExchangeRateProvider.class);
        events = mock(ApplicationEventPublisher.class);
        externalValidation = mock(ExternalValidationPort.class);
        handler = new ExchangeHandler(accounts, rates, events, externalValidation);
    }

    @Test
    void rejectsSelfExchange_beforeAnyDbCallOrExternalValidation() {
        ExchangeCommand cmd = ExchangeCommand.builder()
                .sourceAccountId(1L).targetAccountId(1L)
                .amount(new BigDecimal("10"))
                .build();

        assertThrows(SameAccountExchangeException.class, () -> handler.handle(cmd));

        // Fail-fast: no repository or external interaction should occur.
        verifyNoInteractions(accounts, rates, externalValidation, events);
    }

    @Test
    void throwsAccountNotFound_whenSourceMissing() {
        when(accounts.findById(1L)).thenReturn(Optional.empty());

        ExchangeCommand cmd = ExchangeCommand.builder()
                .sourceAccountId(1L).targetAccountId(2L)
                .amount(new BigDecimal("10"))
                .build();

        assertThrows(AccountNotFoundException.class, () -> handler.handle(cmd));
        verifyNoInteractions(externalValidation, events);
    }

    @Test
    void happyPath_debitsSource_creditsTarget_andPublishesEvent() {
        Account source = Account.builder().id(1L).userId(10L).currency(Currency.EUR)
                .balance(new BigDecimal("500.00")).build();
        Account target = Account.builder().id(2L).userId(10L).currency(Currency.USD)
                .balance(new BigDecimal("0.00")).build();

        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(target));
        when(rates.convert(new BigDecimal("100.00"), Currency.EUR, Currency.USD))
                .thenReturn(new BigDecimal("108.00"));

        ExchangeCommand cmd = ExchangeCommand.builder()
                .sourceAccountId(1L).targetAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        List<Transaction> result = handler.handle(cmd);

        // External validation invoked exactly once for the source account.
        verify(externalValidation).validate(1L);

        // Aggregates mutated as expected.
        assertEquals(new BigDecimal("400.00"), source.getBalance());
        assertEquals(new BigDecimal("108.00"), target.getBalance());
        verify(accounts).save(source);
        verify(accounts).save(target);

        // Two linked transactions, one OUT, one IN, shared referenceId.
        assertEquals(2, result.size());
        assertEquals(TransactionType.EXCHANGE_OUT, result.get(0).getType());
        assertEquals(TransactionType.EXCHANGE_IN, result.get(1).getType());
        assertEquals(result.get(0).getReferenceId(), result.get(1).getReferenceId());

        // One ExchangeCompletedEvent published.
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(event.capture());
        assertNotNull(event.getValue());
    }

    @Test
    void propagatesInsufficientFunds_fromSourceAggregate() {
        Account source = Account.builder().id(1L).userId(10L).currency(Currency.EUR)
                .balance(new BigDecimal("5.00")).build();
        Account target = Account.builder().id(2L).userId(10L).currency(Currency.USD)
                .balance(new BigDecimal("0.00")).build();

        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(target));
        when(rates.convert(any(), any(), any())).thenReturn(new BigDecimal("108.00"));

        ExchangeCommand cmd = ExchangeCommand.builder()
                .sourceAccountId(1L).targetAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(InsufficientFundsException.class, () -> handler.handle(cmd));

        // Neither aggregate should be persisted on failure.
        verify(accounts, never()).save(any());
        verifyNoInteractions(events);
    }
}
