package com.b2b.ordermanagement.application.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public class PagedResponse<T> {
    private List<T> records;
    private int page;
    private int pages;
    private long total;

    public PagedResponse() {}

    public PagedResponse(List<T> records, int page, int pages, long total) {
        this.records = records;
        this.page = page;
        this.pages = pages;
        this.total = total;
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
