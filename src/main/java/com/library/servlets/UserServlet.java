package com.library.servlets;

import com.library.models.User;
import com.library.services.UserService;
import com.library.utils.JsonUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {
	private final UserService userService = new UserService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();

		if (path == null || path.equals("/register")) {
			// Self-register as a Student
			String name = request.getParameter("name");
			String email = request.getParameter("email");
			String password = request.getParameter("password");

			if (name == null || email == null || password == null) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Missing parameters"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			User user = userService.registerStudent(name, email, password);
			JsonUtils.sendJsonResponse(response, Map.of("message", "User registered successfully", "id", user.getId()),
					HttpServletResponse.SC_CREATED);

		} else if (path.equals("/addUserByAdmin")) {
			// Admin creates Librarians or Admins
			User loggedInUser = (User) request.getSession().getAttribute("user");

			if (loggedInUser == null || loggedInUser.getRole() != User.Role.ADMIN) {
				JsonUtils.sendJsonResponse(response,
						Map.of("error", "Only admins can create librarians or other admins"),
						HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String name = request.getParameter("name");
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			String roleStr = request.getParameter("role");

			if (name == null || email == null || password == null || roleStr == null) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Missing parameters"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			User.Role role;
			try {
				role = User.Role.valueOf(roleStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Invalid role"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			User user = userService.addUserByAdmin(name, email, password, role, loggedInUser);
			JsonUtils.sendJsonResponse(response, Map.of("message", "User added successfully", "id", user.getId()),
					HttpServletResponse.SC_CREATED);

		} else if (path.equals("/updatePassword")) {
			// Allow users to update their own password
			User loggedInUser = (User) request.getSession().getAttribute("user");

			if (loggedInUser == null) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "You must be logged in to update password"),
						HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			String oldPassword = request.getParameter("oldPassword");
			String newPassword = request.getParameter("newPassword");

			if (oldPassword == null || newPassword == null) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Missing parameters"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			if (userService.updatePassword(loggedInUser.getId(), oldPassword, newPassword)) {
				JsonUtils.sendJsonResponse(response, Map.of("message", "Password updated successfully"),
						HttpServletResponse.SC_OK);
			} else {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Incorrect old password"),
						HttpServletResponse.SC_BAD_REQUEST);
			}
		} else if (path.equals("/login")) {
			// Handle user login
			String name = request.getParameter("name");
			String password = request.getParameter("password");

			if (name == null || password == null) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Missing credentials"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			User user = userService.authenticateByName(name, password);
			if (user != null) {
				request.getSession().setAttribute("user", user);
				JsonUtils.sendJsonResponse(response, Map.of("message", "Login successful", "role", user.getRole()),
						HttpServletResponse.SC_OK);
			} else {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Invalid credentials"),
						HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else if (path.equals("/logout")) {
			// Handle user logout
			request.getSession().invalidate();
			JsonUtils.sendJsonResponse(response, Map.of("message", "Logout successful"), HttpServletResponse.SC_OK);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();

		if (path == null || path.equals("/")) {
			User loggedInUser = (User) request.getSession().getAttribute("user");

			if (loggedInUser == null) {
			    JsonUtils.sendJsonResponse(response, Map.of("error", "Unauthorized access"), HttpServletResponse.SC_UNAUTHORIZED);
			    return;
			}

			// Admins can see all users, Librarians can see only Students
			if (loggedInUser.getRole() == User.Role.ADMIN) {
			    List<User> users = userService.getAllUsers();
			    JsonUtils.sendJsonResponse(response, users, HttpServletResponse.SC_OK);
			} else if (loggedInUser.getRole() == User.Role.LIBRARIAN) {
			    List<User> students = userService.getAllStudents(); // New method in UserService
			    JsonUtils.sendJsonResponse(response, students, HttpServletResponse.SC_OK);
			} else {
			    JsonUtils.sendJsonResponse(response, Map.of("error", "Access denied"), HttpServletResponse.SC_FORBIDDEN);
			}

		} else if (path.equals("/getUser")) {
			// Fetch user by ID
			String userId = request.getParameter("id");

			if (userId == null || userId.isEmpty()) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "User ID required"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			User user = userService.getUserById(userId);
			if (user != null) {
				JsonUtils.sendJsonResponse(response, user, HttpServletResponse.SC_OK);
			} else {
				JsonUtils.sendJsonResponse(response, Map.of("error", "User not found"),
						HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User loggedInUser = (User) request.getSession().getAttribute("user");

		if (loggedInUser == null || loggedInUser.getRole() != User.Role.ADMIN) {
			JsonUtils.sendJsonResponse(response, Map.of("error", "Admin access required"),
					HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String userId = request.getParameter("id");

		if (userId == null || userId.isEmpty()) {
			JsonUtils.sendJsonResponse(response, Map.of("error", "User ID is required"),
					HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (userService.deleteUser(userId, loggedInUser)) {
			JsonUtils.sendJsonResponse(response, Map.of("message", "User deleted successfully"),
					HttpServletResponse.SC_OK);
		} else {
			JsonUtils.sendJsonResponse(response, Map.of("error", "User not found or cannot be deleted"),
					HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String id = request.getParameter("id");
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String roleStr = request.getParameter("role");

		User loggedInUser = (User) request.getSession().getAttribute("user");

		if (loggedInUser == null) {
		    JsonUtils.sendJsonResponse(response, Map.of("error", "Unauthorized access"), HttpServletResponse.SC_UNAUTHORIZED);
		    return;
		}

		// Admins can update any user, Librarians can only update Students
		boolean canUpdate = loggedInUser.getRole() == User.Role.ADMIN ||
		        (loggedInUser.getRole() == User.Role.LIBRARIAN && userService.getUserById(id).getRole() == User.Role.STUDENT);

		if (!canUpdate) {
		    JsonUtils.sendJsonResponse(response, Map.of("error", "Permission denied"), HttpServletResponse.SC_FORBIDDEN);
		    return;
		}

		// Proceed with updating user info
		if (id == null) {
			JsonUtils.sendJsonResponse(response, Map.of("error", "User ID is required"),
					HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		User.Role role = null;
		if (roleStr != null) {
			try {
				role = User.Role.valueOf(roleStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				JsonUtils.sendJsonResponse(response, Map.of("error", "Invalid role"),
						HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		if (userService.updateUser(id, name, email, password, role)) {
			JsonUtils.sendJsonResponse(response, Map.of("message", "User updated successfully"),
					HttpServletResponse.SC_OK);
		} else {
			JsonUtils.sendJsonResponse(response, Map.of("error", "User not found"),
					HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
