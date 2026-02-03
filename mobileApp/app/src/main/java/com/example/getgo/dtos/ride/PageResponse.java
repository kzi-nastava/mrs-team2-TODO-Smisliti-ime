package com.example.getgo.dtos.ride;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int number; // trenutna stranica

    // Getteri i setteri
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
