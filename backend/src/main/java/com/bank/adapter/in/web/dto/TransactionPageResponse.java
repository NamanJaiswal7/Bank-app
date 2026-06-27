package com.bank.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Paginated envelope for {@link TransactionResponse} results.
 *
 * <p>The {@code last} flag mirrors the Spring Data convention so that
 * frontend infinite-scroll logic can stop requesting once the final page
 * has been served, without having to compare {@code page+1} against
 * {@code totalPages}.</p>
 */
@Getter @Builder
public class TransactionPageResponse {
    private List<TransactionResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    /** {@code true} when this is the last page (no more results to fetch). */
    private boolean last;
}
