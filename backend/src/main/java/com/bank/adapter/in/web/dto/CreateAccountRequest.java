package com.bank.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class CreateAccountRequest {
    // Currency code (e.g. USD, EUR). Validated for shape here; validated for
    // membership in the supported set by Currency.fromString(...) in the
    // controller (throws InvalidCurrencyException on unknown codes).
    @NotBlank(message = "currency is required")
    private String currency;
}
