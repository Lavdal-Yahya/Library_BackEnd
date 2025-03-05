package com.library.servlets;

import com.library.models.BorrowRecord;
import com.library.models.User;
import com.library.services.BorrowService;
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

@WebServlet("/borrow/*")
public class BorrowServlet extends HttpServlet {
    private final BorrowService borrowService = new BorrowService(new BookService());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("user");

        if (loggedInUser == null) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "You must be logged in to borrow or return books"), HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String path = request.getPathInfo();

        if (path != null && path.equals("/return")) {
            // Return a book
            String isbn = request.getParameter("isbn");

            if (isbn == null) {
                JsonUtils.sendJsonResponse(response, Map.of("error", "ISBN is required"), HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (borrowService.returnBook(loggedInUser.getId(), isbn)) {
                JsonUtils.sendJsonResponse(response, Map.of("message", "Book returned successfully"), HttpServletResponse.SC_OK);
            } else {
                JsonUtils.sendJsonResponse(response, Map.of("error", "Return failed"), HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            // Borrow a book
            String isbn = request.getParameter("isbn");

            if (isbn == null) {
                JsonUtils.sendJsonResponse(response, Map.of("error", "ISBN is required"), HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (borrowService.borrowBook(loggedInUser.getId(), isbn)) {
                JsonUtils.sendJsonResponse(response, Map.of("message", "Book borrowed successfully"), HttpServletResponse.SC_OK);
            } else {
                JsonUtils.sendJsonResponse(response, Map.of("error", "Borrowing failed"), HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("user");

        if (loggedInUser == null) {
            JsonUtils.sendJsonResponse(response, Map.of("error", "You must be logged in to view borrow history"), HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userId = request.getParameter("userId");
        String isbn = request.getParameter("isbn");

        if (userId != null) {
            // Students can only view their own history
            if (loggedInUser.getRole() == User.Role.STUDENT && !loggedInUser.getId().equals(userId)) {
                JsonUtils.sendJsonResponse(response, Map.of("error", "Students can only view their own borrow history"), HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Admins and Librarians can view any user's history
            List<BorrowRecord> history = borrowService.getBorrowHistoryByUserId(userId);
            if (history.isEmpty()) {
                JsonUtils.sendJsonResponse(response, Map.of("message", "No borrow history found for User ID: " + userId), HttpServletResponse.SC_OK);
            } else {
                JsonUtils.sendJsonResponse(response, history, HttpServletResponse.SC_OK);
            }
        } else if (isbn != null) {
            // Everyone can view book borrow history
            List<BorrowRecord> history = borrowService.getBorrowHistoryByIsbn(isbn);
            if (history.isEmpty()) {
                JsonUtils.sendJsonResponse(response, Map.of("message", "No borrow history found for ISBN: " + isbn), HttpServletResponse.SC_OK);
            } else {
                JsonUtils.sendJsonResponse(response, history, HttpServletResponse.SC_OK);
            }
        } else {
            // If no userId or isbn is provided, only Admins and Librarians can view full borrow history
            if (loggedInUser.getRole() == User.Role.ADMIN || loggedInUser.getRole() == User.Role.LIBRARIAN) {
                List<BorrowRecord> allHistory = borrowService.getAllBorrowHistory();
                if (allHistory.isEmpty()) {
                    JsonUtils.sendJsonResponse(response, Map.of("message", "No borrow history found"), HttpServletResponse.SC_OK);
                } else {
                    JsonUtils.sendJsonResponse(response, allHistory, HttpServletResponse.SC_OK);
                }
            } else {
                JsonUtils.sendJsonResponse(response, Map.of("error", "You do not have permission to view all borrow history"), HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }

}
