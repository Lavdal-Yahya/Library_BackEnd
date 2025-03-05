package com.library.services;



import com.library.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.List;

public class UserService {
	private static final List<User> users = new ArrayList<>();
	private int userIdCounter = 1; // Starts from 1 (Admin is always 0)

	public UserService() {
		if (users.isEmpty()) {
			String hashedPassword = hashPassword("admin");
			users.add(new User("0", "Admin", "admin@library.com", hashedPassword, User.Role.ADMIN));
		}
	}

	private String hashPassword(String plainTextPassword) {
		return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
	}

	private boolean verifyPassword(String plainTextPassword, String hashedPassword) {
		return BCrypt.checkpw(plainTextPassword, hashedPassword);
	}

	// Register a new user (Students only)
	public User registerStudent(String name, String email, String password) {
		String id = String.valueOf(userIdCounter++);
		String hashedPassword = hashPassword(password);
		User user = new User(id, name, email, hashedPassword, User.Role.STUDENT);
		users.add(user);
		return user;
	}

	// Admin can create Librarians or Admins
	public User addUserByAdmin(String name, String email, String password, User.Role role, User loggedInUser) {
		if (loggedInUser == null || loggedInUser.getRole() != User.Role.ADMIN) {
			throw new SecurityException("Only admins can create librarians or other admins");
		}

		String id = String.valueOf(userIdCounter++);
		String hashedPassword = hashPassword(password);
		User user = new User(id, name, email, hashedPassword, role);
		users.add(user);
		return user;
	}

	public User getUserById(String id) {
		return users.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
	}

	public boolean deleteUser(String id, User loggedInUser) {
		if (loggedInUser == null || loggedInUser.getRole() != User.Role.ADMIN) {
			return false;
		}
		return users.removeIf(user -> user.getId().equals(id) && !id.equals("0")); // Prevent deleting admin
	}

	public User authenticateByName(String name, String password) {
		return users.stream()
				.filter(user -> user.getName().equalsIgnoreCase(name) && verifyPassword(password, user.getPassword()))
				.findFirst().orElse(null);
	}

	// Allow any user to update their own password
	public boolean updatePassword(String id, String oldPassword, String newPassword) {
		for (User user : users) {
			if (user.getId().equals(id) && verifyPassword(oldPassword, user.getPassword())) {
				user.setPassword(hashPassword(newPassword));
				return true;
			}
		}
		return false;
	}

	// Get all users
	public List<User> getAllUsers() {
		return users;
	}
	//Get all students
	public List<User> getAllStudents() {
	    return users.stream().filter(user -> user.getRole() == User.Role.STUDENT).toList();
	}

	
	// Update user information (Ensures new password is hashed)
	public boolean updateUser(String id, String name, String email, String password, User.Role role) {
		for (User user : users) {
			if (user.getId().equals(id)) {
				if (name != null && !user.getName().equals(name))
					user.setName(name);
				if (email != null && !user.getEmail().equals(email))
					user.setEmail(email);
				if (password != null && !user.getPassword().equals(password))
					user.setPassword(hashPassword(password)); // Hash new password
				if (role != null && user.getRole() != role)
					user.setRole(role);
				return true;
			}
		}
		return false;
	}
}