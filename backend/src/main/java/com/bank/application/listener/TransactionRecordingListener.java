package com.bank.application.listener;

import com.bank.domain.event.AccountCreditedEvent;
import com.bank.domain.event.DebitExecutedEvent;
import com.bank.domain.event.ExchangeCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer — listens for domain events and performs side effects.
 * Currently logs events. Can be extended for audit trails, notifications, etc.
 */
@Slf4j
@Component
public class TransactionRecordingListener {

    @EventListener
    public void onAccountCredited(AccountCreditedEvent event) {
        log.info("EVENT: Account {} credited with {} {}. New balance: {}",
                event.getAccountId(),
                event.getCurrency().getSymbol(),
                event.getAmount(),
                event.getBalanceAfter());
    }

    @EventListener
    public void onDebitExecuted(DebitExecutedEvent event) {
        log.info("EVENT: Account {} debited with {} {}. New balance: {}",
                event.getAccountId(),
                event.getCurrency().getSymbol(),
                event.getAmount(),
                event.getBalanceAfter());
    }

    @EventListener
    public void onExchangeCompleted(ExchangeCompletedEvent event) {
        log.info("EVENT: Exchange completed. {} {} (account {}) → {} {} (account {}). Ref: {}",
                event.getSourceCurrency().getSymbol(), event.getSourceAmount(),
                event.getSourceAccountId(),
                event.getTargetCurrency().getSymbol(), event.getTargetAmount(),
                event.getTargetAccountId(),
                event.getReferenceId());
    }
}
