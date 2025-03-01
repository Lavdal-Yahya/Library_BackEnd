package com.library.models;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private int yearPublished;
    private int availableCopies;

    public Book(String isbn, String title, String author, int yearPublished, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.availableCopies = availableCopies;
    }

    // Getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYearPublished() { return yearPublished; }
    public int getAvailableCopies() { return availableCopies; }

    // Setters
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setYearPublished(int yearPublished) { this.yearPublished = yearPublished; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
}