package com.library.models;


import java.time.LocalDate;

public class BorrowRecord {
    private String userId;
    private String isbn;
    private LocalDate borrowDate;
    private LocalDate returnDate;

    public BorrowRecord(String userId, String isbn, LocalDate borrowDate) {
        this.userId = userId;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.returnDate = null; // Not returned yet
    }

    // Getters
    public String getUserId() { return userId; }
    public String getIsbn() { return isbn; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getReturnDate() { return returnDate; }

    // Set return date when the book is returned
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}