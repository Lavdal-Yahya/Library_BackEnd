package com.library.services;



import com.library.models.Book;
import java.util.ArrayList;
import java.util.List;

public class BookService {
	private static final List<Book> books = new ArrayList<>(); // In-memory storage

	// Add a new book
	public void addBook(String isbn, String title, String author, int year, int copies) {
		books.add(new Book(isbn, title, author, year, copies));
	}

	// Find book by ISBN
	public Book getBookByIsbn(String isbn) {
		return books.stream().filter(book -> book.getIsbn().equals(isbn)).findFirst().orElse(null);
	}

	// Get all books
	public List<Book> getAllBooks() {
		return books;
	}

	// Delete a book by ISBN
	public boolean deleteBook(String isbn) {
		return books.removeIf(book -> book.getIsbn().equals(isbn));
	}

	// Update book information (Ensures copies are not negative)
	public boolean updateBook(String isbn, String title, String author, Integer year, Integer copies) {
	    for (Book book : books) {
	        if (book.getIsbn().equals(isbn)) {
	            if (title != null) book.setTitle(title);
	            if (author != null) book.setAuthor(author);
	            if (year != null) book.setYearPublished(year);
	            if (copies != null && copies >= 0) { // Prevent negative copies
	                book.setAvailableCopies(copies);
	            } else {
	                return false; // Invalid copy count
	            }
	            return true;
	        }
	    }
	    return false;
	}

}