package com.library.servlets;

import com.library.models.Book;
import com.library.models.User;
import com.library.services.BookService;
import com.library.utils.JsonUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/books/*")
public class BookServlet extends HttpServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("user");

        if (loggedInUser == null || loggedInUser.getRole() != User.Role.ADMIN) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Admin access required"), HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Add new book
        String isbn = request.getParameter("isbn");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String yearStr = request.getParameter("year");
        String copiesStr = request.getParameter("copies");

        if (isbn == null || title == null || author == null || yearStr == null || copiesStr == null) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Missing parameters"), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int copies = Integer.parseInt(copiesStr);
            bookService.addBook(isbn, title, author, year, copies);
            JsonUtils.sendJsonResponse(response, Map.of("message", "Book added successfully"), HttpServletResponse.SC_CREATED);
        } catch (NumberFormatException e) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Year and Copies must be valid numbers"), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Book> books = bookService.getAllBooks();
        JsonUtils.sendJsonResponse(response, books, HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("user");

        if (loggedInUser == null || loggedInUser.getRole() != User.Role.ADMIN) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Admin access required"), HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String isbn = request.getParameter("isbn");

        if (isbn == null) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "ISBN is required"), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (bookService.deleteBook(isbn)) {
            JsonUtils.sendJsonResponse(response, Map.of("message", "Book deleted successfully"), HttpServletResponse.SC_OK);
        } else {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Book not found"), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String isbn = request.getParameter("isbn");
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String yearStr = request.getParameter("year");
        String copiesStr = request.getParameter("copies");

        if (isbn == null) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "ISBN is required"), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Integer year = null, copies = null;
        try {
            if (yearStr != null) year = Integer.parseInt(yearStr);
            if (copiesStr != null) copies = Integer.parseInt(copiesStr);
        } catch (NumberFormatException e) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Year and Copies must be valid numbers"), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (bookService.updateBook(isbn, title, author, year, copies)) {
            JsonUtils.sendJsonResponse(response, Map.of("message", "Book updated successfully"), HttpServletResponse.SC_OK);
        } else {
            JsonUtils.sendJsonResponse(response, Map.of("error", "Book not found or invalid data"), HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
