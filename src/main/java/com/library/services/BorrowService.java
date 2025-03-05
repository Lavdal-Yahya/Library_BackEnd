package com.library.services;


import com.library.models.BorrowRecord;
import com.library.models.Book;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class BorrowService {
    private static final List<BorrowRecord> borrowRecords = new ArrayList<>();
    private final BookService bookService;

    public BorrowService(BookService bookService) {
        this.bookService = bookService;
    }

 // Borrow a book (Ensures user cannot borrow the same book twice without returning it)
    public boolean borrowBook(String userId, String isbn) {
        Book book = bookService.getBookByIsbn(isbn);
        
        if (book == null || book.getAvailableCopies() <= 0) {
            return false; // Book not available
        }

        // Check if user has already borrowed this book and not returned it
        for (BorrowRecord record : borrowRecords) {
            if (record.getUserId().equals(userId) && record.getIsbn().equals(isbn) && record.getReturnDate() == null) {
                return false; // User must return before borrowing again
            }
        }

        // Allow borrowing
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        borrowRecords.add(new BorrowRecord(userId, isbn, LocalDate.now()));
        return true;
    }


    // Return a book
    public boolean returnBook(String userId, String isbn) {
        for (BorrowRecord record : borrowRecords) {
            if (record.getUserId().equals(userId) && record.getIsbn().equals(isbn) && record.getReturnDate() == null) {
                record.setReturnDate(LocalDate.now());
                Book book = bookService.getBookByIsbn(isbn);
                if (book != null) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                }
                return true;
            }
        }
        return false;
    }

    // Get borrow history of a user
    public List<BorrowRecord> getBorrowHistoryByUserId(String userId) {
        List<BorrowRecord> history = new ArrayList<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getUserId().equals(userId)) {
                history.add(record);
            }
        }
        return history;
    }
    
 // Get history of all borrowed books
    public List<BorrowRecord> getAllBorrowHistory() {
        return new ArrayList<>(borrowRecords);
    }

 // Get borrow history for a specific book
    public List<BorrowRecord> getBorrowHistoryByIsbn(String isbn) {
        List<BorrowRecord> history = new ArrayList<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getIsbn().equals(isbn)) {
                history.add(record);
            }
        }
        return history;
    }

}