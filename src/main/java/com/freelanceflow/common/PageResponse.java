package com.freelanceflow.common;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public PageResponse() {}

    public static <T> PageResponse<T> of(Page<T> pageData) {
        PageResponse<T> r = new PageResponse<>();
        r.content = pageData.getContent();
        r.page = pageData.getNumber();
        r.size = pageData.getSize();
        r.totalElements = pageData.getTotalElements();
        r.totalPages = pageData.getTotalPages();
        r.last = pageData.isLast();
        return r;
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}
