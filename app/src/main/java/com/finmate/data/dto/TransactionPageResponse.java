package com.finmate.data.dto;

import java.util.List;

/**
 * Map với Page<TransactionResponse> từ Spring:
 * {
 *   "content": [ ... ],
 *   "totalPages": ...,
 *   "last": ...,
 *   "number": ...,
 *   ...
 * }
 */
public class TransactionPageResponse {

    private java.util.List<TransactionResponse> content;
    private int totalPages;
    private boolean last;
    private int number;

    public TransactionPageResponse() {
    }

    public List<TransactionResponse> getContent() {
        return content;
    }

    public void setContent(List<TransactionResponse> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
