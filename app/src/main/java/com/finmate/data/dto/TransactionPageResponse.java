package com.finmate.data.dto;

import java.util.List;

/**
 * Map với Page<TransactionResponse> từ Spring:
 * {
 *   "content": [ ... ],
 *   "totalPages": ...,
 *   ...
 * }
 */
public class TransactionPageResponse {

    private java.util.List<TransactionResponse> content;

    public TransactionPageResponse() {
    }

    public List<TransactionResponse> getContent() {
        return content;
    }

    public void setContent(List<TransactionResponse> content) {
        this.content = content;
    }
}
