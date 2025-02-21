# Library Management System (Back-End)

## Project Overview

This project is a Java EE/Spring Boot web application for managing a university library. It allows administrators, librarians, and students to interact with the system based on their roles.

## Features

### 1️⃣ Authentication & Role Management
- Secure authentication using Spring Security & JWT.
- Three user roles: **Admin, Librarian, Student**.

### 2️⃣ User Management
- Admin can add, modify, and delete librarians.
- Users can register with required details.

### 3️⃣ Book Management
- Admins can add, edit, and delete books.
- Users can view book details.

### 4️⃣ Borrowing & Returning Books
- Students can borrow books.
- Librarians can validate book loans and returns.
- Book availability updates in real-time.

### 5️⃣ Loan History
- Students can view their loan history.
- Admins & Librarians can view all users' loan records.

### 6️⃣ Access Control
- Spring Security ensures role-based access.
- Only authorized users can access specific endpoints.

## Tech Stack

- **Back-end**: Spring Boot, Spring Security, Spring Data JPA
- **Database**: H2 (In-memory) or MySQL
- **Build Tool**: Maven
- **API Testing**: Postman
- **Version Control**: Git

## Setup Instructions

### Prerequisites

- Java 17
- Maven
- Eclipse IDE
- Apache Tomcat 10 (if needed)
- Postman (for API testing)

### Steps to Run the Project

1. Clone the repository:

   ```bash
   git clone https://github.com/your-repo/library-management.git
   ```

2. Navigate to the project directory:

   ```bash
   cd library-management
   ```

3. Build the project:

   ```bash
   mvn clean install
   ```

4. Run the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

5. Access the API at `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /auth/register` - Register a new user
- `POST /auth/login` - User login

### User Management (Admin Only)
- `GET /users` - View all users
- `DELETE /users/{id}` - Delete a user

### Book Management
- `POST /books` - Add a new book
- `GET /books` - View all books
- `PUT /books/{id}` - Update book details
- `DELETE /books/{id}` - Delete a book

### Borrowing & Returning
- `POST /loans/borrow` - Borrow a book
- `POST /loans/return` - Return a book
- `GET /loans/user/{id}` - View loan history

## Testing the API with Postman

1. Import API endpoints into Postman.
2. Test authentication (register/login).
3. Test CRUD operations for users & books.
4. Test borrowing & returning books.
5. Verify role-based access control.

## Next Steps

- Implement a frontend UI with React/Angular.
- Add email notifications for due dates.
- Integrate with an external book database API.

## Contributing

Contributions are welcome! Fork the repo and submit a pull request.

## License

This project is licensed under the **MIT License**.
