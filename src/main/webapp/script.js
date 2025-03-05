// Add these functions at the beginning of your script.js file
function toggleList(containerId, buttonElement, fetchFunction) {
  const container = document.getElementById(containerId)
  const isVisible = !container.classList.contains("hidden")

  if (isVisible) {
    // Hide the list
    container.classList.add("hidden")
    buttonElement.textContent = buttonElement.textContent.replace("Hide", "View")
  } else {
    // Show and fetch the list
    container.classList.remove("hidden")
    buttonElement.textContent = buttonElement.textContent.replace("View", "Hide")
    fetchFunction()
  }
}

// API Base URL
const API_BASE_URL = "http://localhost:8080/LibraryManagementSystem"

document.addEventListener("DOMContentLoaded", () => {
  // Check authentication on every page
  checkAuthentication()

  // Display user role on pages that have the userRole element
  displayUserRole()

  // Setup page-specific event listeners
  setupEventListeners()
})

// Check if user is authenticated
function checkAuthentication() {
  const role = sessionStorage.getItem("role");
  const isLoginPage = window.location.pathname.includes("login.html");
  const isRegisterPage = window.location.pathname.includes("register.html"); // Allow register page
  const isIndexPage = window.location.pathname.endsWith("/") || window.location.pathname.endsWith("index.html");

  // If not logged in and not on login, register, or index page, redirect to login
  if (!role && !isLoginPage && !isRegisterPage && !isIndexPage) {
    window.location.href = "login.html";
    return;
  }

  // If logged in and on login page, redirect to dashboard
  if (role && isLoginPage) {
    window.location.href = "dashboard.html";
    return;
  }

  // Check access permissions for specific pages
  if (role) {
    const isUserManagementPage = window.location.pathname.includes("user-management.html");

    // Only ADMIN can access user management
    if (isUserManagementPage && role !== "ADMIN") {
      alert("Access denied. Only administrators can access user management.");
      window.location.href = "dashboard.html";
      return;
    }
  }
}


// Display user role on pages that have the userRole element
function displayUserRole() {
  const userRoleElement = document.getElementById("userRole")
  if (userRoleElement) {
    const role = sessionStorage.getItem("role")
    userRoleElement.innerText = role || "Not logged in"
  }
}

// Setup page-specific event listeners
function setupEventListeners() {
  // Login form
  const loginForm = document.getElementById("loginForm")
  if (loginForm) {
    loginForm.addEventListener("submit", handleLogin)
  }

  // User management forms
  const addUserForm = document.getElementById("addUserForm")
  if (addUserForm) {
    addUserForm.addEventListener("submit", handleAddUser)
  }

  const editUserForm = document.getElementById("editUserForm")
  if (editUserForm) {
    editUserForm.addEventListener("submit", handleEditUser)
  }

  const deleteUserForm = document.getElementById("deleteUserForm")
  if (deleteUserForm) {
    deleteUserForm.addEventListener("submit", handleDeleteUser)
  }

  // Book management forms
  const addBookForm = document.getElementById("addBookForm")
  if (addBookForm) {
    addBookForm.addEventListener("submit", handleAddBook)
  }

  const editBookForm = document.getElementById("editBookForm")
  if (editBookForm) {
    editBookForm.addEventListener("submit", handleEditBook)
  }

  const deleteBookForm = document.getElementById("deleteBookForm")
  if (deleteBookForm) {
    deleteBookForm.addEventListener("submit", handleDeleteBook)
  }

  // If on dashboard, check which cards to show based on role
  if (window.location.pathname.includes("dashboard.html")) {
    setupDashboard()
  }

 
}

// Setup dashboard based on user role
function setupDashboard() {
  const role = sessionStorage.getItem("role");

  const userManagementCard = document.getElementById("userManagementCard");
  const bookManagementCard = document.getElementById("bookManagementCard"); // Select Book Management card

  // Only show User Management to Admins
  if (userManagementCard && role !== "ADMIN") {
    userManagementCard.classList.add("hidden");
  }

  // Only show Book Management to Admins
  if (bookManagementCard && role !== "ADMIN") {
    bookManagementCard.classList.add("hidden");
  }
}


// Handle login form submission
async function handleLogin(event) {
  event.preventDefault()
  const name = document.getElementById("name").value
  const password = document.getElementById("password").value
  const messageEl = document.getElementById("message")

  try {
    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `name=${encodeURIComponent(name)}&password=${encodeURIComponent(password)}`,
    })

    const result = await response.json()
    if (response.ok) {
      sessionStorage.setItem("role", result.role)
      window.location.href = "dashboard.html"
    } else {
      messageEl.innerText = result.error || "Login failed"
      messageEl.style.color = "red"
    }
  } catch (error) {
    console.error("Login error:", error)
    messageEl.innerText = "An error occurred during login. Please try again."
    messageEl.style.color = "red"
  }
}

// Modify the list functions to use the toggle functionality
async function listUsers() {
  const button = document.querySelector('[data-action="list-users"]')
  const container = document.getElementById("userListContainer")

  if (!container.classList.contains("hidden")) {
    toggleList("userListContainer", button)
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/users`, {
    method: "GET",
    credentials: "include" // Ensure session authentication
});


    if (!response.ok) {
      throw new Error(`Failed to fetch users: ${response.status}`)
    }

    const users = await response.json()

    if (container) {
      let usersHTML = `
        <h3>All Users</h3>
        <table class="data-table">
          <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Email</th>
        <th>Role</th>
        <th>Edit</th>
    </tr>
</thead>

          <tbody>
      `

      users.forEach((user) => {
        usersHTML += `
          <tr>
    <td>${user.id}</td>
    <td>${user.name}</td>
    <td>${user.email}</td>
    <td>${user.role}</td>
    <td>
        ${user.role === "STUDENT" ? `<button onclick="editUser('${user.id}', '${user.name}', '${user.email}')">Edit</button>` : ""}
    </td>
</tr>

        `
      })

      usersHTML += `
          </tbody>
        </table>
      `

      container.innerHTML = usersHTML
      toggleList("userListContainer", button)
    }
  } catch (error) {
    console.error("Error listing users:", error)
    alert("Failed to load users. Please try again.")
  }
}

// Handle add user form submission
async function handleAddUser(event) {
  event.preventDefault()
  const name = document.getElementById("newUserName").value
  const email = document.getElementById("newUserEmail").value
  const password = document.getElementById("newUserPassword").value
  const role = document.getElementById("newUserRole").value
  const messageEl = document.getElementById("userMessage")

  try {
    const response = await fetch(`${API_BASE_URL}/users/addUserByAdmin`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${role}`,
    })

    const result = await response.json()
    if (response.ok) {
      messageEl.innerText = "User added successfully!"
      messageEl.className = "message text-success"
      event.target.reset()
      // Refresh the user list
      listUsers()
    } else {
      messageEl.innerText = result.error || "Failed to add user"
      messageEl.className = "message text-danger"
    }
  } catch (error) {
    console.error("Error adding user:", error)
    messageEl.innerText = "An error occurred. Please try again."
    messageEl.className = "message text-danger"
  }
}

// Handle edit user form submission
async function handleEditUser(event) {
  event.preventDefault()
  const userId = document.getElementById("editUserId").value
  const name = document.getElementById("editUserName").value
  const email = document.getElementById("editUserEmail").value
  const role = document.getElementById("editUserRole").value
  const editMessageEl = document.getElementById("editMessage")

  try {
    // Construct query parameters dynamically
    const params = new URLSearchParams()
    params.append("id", userId)
    if (name) params.append("name", name)
    if (email) params.append("email", email)
    if (role) params.append("role", role)

    const response = await fetch(`${API_BASE_URL}/users?${params.toString()}`, {
    method: "PUT",
    credentials: "include"
});


    const result = await response.text()
    if (response.ok) {
      editMessageEl.innerText = "User updated successfully!"
      editMessageEl.className = "message text-success"
      event.target.reset()
      // Refresh the user list
      listUsers()
    } else {
      editMessageEl.innerText = result || "Failed to update user"
      editMessageEl.className = "message text-danger"
    }
  } catch (error) {
    console.error("Error updating user:", error)
    editMessageEl.innerText = "An error occurred. Please try again."
    editMessageEl.className = "message text-danger"
  }
}

// Handle delete user form submission
async function handleDeleteUser(event) {
  event.preventDefault()
  const userId = document.getElementById("deleteUserId").value
  const deleteMessageEl = document.getElementById("deleteMessage")

  // Confirm deletion
  if (!confirm("Are you sure you want to delete this user? This action cannot be undone.")) {
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/users?id=${userId}`, {
      method: "DELETE",
    })

    const result = await response.text()
    if (response.ok) {
      deleteMessageEl.innerText = "User deleted successfully!"
      deleteMessageEl.className = "message text-success"
      event.target.reset()
      // Refresh the user list
      listUsers()
    } else {
      deleteMessageEl.innerText = result || "Failed to delete user"
      deleteMessageEl.className = "message text-danger"
    }
  } catch (error) {
    console.error("Error deleting user:", error)
    deleteMessageEl.innerText = "An error occurred. Please try again."
    deleteMessageEl.className = "message text-danger"
  }
}

// Modify the list functions to use the toggle functionality
async function listBooks() {
  const button = document.querySelector('[data-action="list-books"]')
  const container = document.getElementById("bookListContainer")

  if (!container.classList.contains("hidden")) {
    toggleList("bookListContainer", button)
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/books`)

    if (!response.ok) {
      throw new Error(`Failed to fetch books: ${response.status}`)
    }

    const books = await response.json()

    if (container) {
      let booksHTML = `
        <h3>All Books</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>ISBN</th>
              <th>Title</th>
              <th>Author</th>
              <th>Year</th>
              <th>Copies</th>
            </tr>
          </thead>
          <tbody>
      `

      books.forEach((book) => {
        booksHTML += `
          <tr>
            <td>${book.isbn}</td>
            <td>${book.title}</td>
            <td>${book.author}</td>
            <td>${book.year}</td>
            <td>${book.copies}</td>
          </tr>
        `
      })

      booksHTML += `
          </tbody>
        </table>
      `

      container.innerHTML = booksHTML
      toggleList("bookListContainer", button)
    }
  } catch (error) {
    console.error("Error listing books:", error)
    alert("Failed to load books. Please try again.")
  }
}

// Handle add book form submission
async function handleAddBook(event) {
  event.preventDefault()
  const isbn = document.getElementById("bookIsbn").value
  const title = document.getElementById("bookTitle").value
  const author = document.getElementById("bookAuthor").value
  const year = document.getElementById("bookYear").value
  const copies = document.getElementById("bookCopies").value
  const bookMessageEl = document.getElementById("bookMessage")

  try {
    const response = await fetch(`${API_BASE_URL}/books`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `isbn=${isbn}&title=${encodeURIComponent(title)}&author=${encodeURIComponent(author)}&year=${year}&copies=${copies}`,
    })

    const result = await response.text()
    if (response.ok) {
      bookMessageEl.innerText = "Book added successfully!"
      bookMessageEl.className = "message text-success"
      event.target.reset()
      // Refresh the book list
      listBooks()
    } else {
      bookMessageEl.innerText = result || "Failed to add book"
      bookMessageEl.className = "message text-danger"
    }
  } catch (error) {
    console.error("Error adding book:", error)
    bookMessageEl.innerText = "An error occurred. Please try again."
    bookMessageEl.className = "message text-danger"
  }
}

// Handle edit book form submission
async function handleEditBook(event) {
  event.preventDefault()
  const isbn = document.getElementById("editBookIsbn").value
  const title = document.getElementById("editBookTitle").value
  const author = document.getElementById("editBookAuthor").value
  const year = document.getElementById("editBookYear").value
  const copies = document.getElementById("editBookCopies").value
  const editBookMessageEl = document.getElementById("editBookMessage")

  try {
    const params = new URLSearchParams()
    params.append("isbn", isbn)
    if (title) params.append("title", encodeURIComponent(title))
    if (author) params.append("author", encodeURIComponent(author))
    if (year) params.append("year", year)
    if (copies) params.append("copies", copies)

    const response = await fetch(`${API_BASE_URL}/books?${params.toString()}`, {
      method: "PUT",
    })

    const result = await response.text()
    if (response.ok) {
      editBookMessageEl.innerText = "Book updated successfully!"
      editBookMessageEl.className = "message text-success"
      event.target.reset()
      // Refresh the book list
      listBooks()
    } else {
      editBookMessageEl.innerText = result || "Failed to update book"
      editBookMessageEl.className = "message text-danger"
    }
  } catch (error) {
    console.error("Error updating book:", error)
    editBookMessageEl.innerText = "An error occurred. Please try again."
    editBookMessageEl.className = "message text-danger"
  }
}

// Handle delete book form submission
async function handleDeleteBook(event) {
  event.preventDefault()
  const isbn = document.getElementById("deleteBookIsbn").value
  const deleteBookMessageEl = document.getElementById("deleteBookMessage")

  // Confirm deletion
  if (!confirm("Are you sure you want to delete this book? This action cannot be undone.")) {
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/books?isbn=${isbn}`, {
      method: "DELETE",
    })

    const result = await response.text()
    if (response.ok) {
      deleteBookMessageEl.innerText = "Book deleted successfully!"
      deleteBookMessageEl.className = "message text-success"
      event.target.reset()
      // Refresh the book list
      listBooks()
    } else {
      deleteBookMessageEl.innerText = result || "Failed to delete book"
      deleteBookMessageEl.className = "message text-danger"
    }
  } catch (error) {
    console.error("Error deleting book:", error)
    deleteBookMessageEl.innerText = "An error occurred. Please try again."
    deleteBookMessageEl.className = "message text-danger"
  }
}

// Modify the list functions to use the toggle functionality
async function viewBorrowHistory() {
  const button = document.querySelector('[data-action="list-borrow-history"]')
  const container = document.getElementById("borrowHistoryContainer")
  const role = sessionStorage.getItem("role")

  if (!container.classList.contains("hidden")) {
    toggleList("borrowHistoryContainer", button)
    return
  }

  if (role === "ADMIN" || role === "LIBRARIAN") {
    try {
      const response = await fetch(`${API_BASE_URL}/borrow`)

      if (!response.ok) {
        throw new Error(`Failed to fetch borrow history: ${response.status}`)
      }

      const history = await response.json()

      if (container) {
        let historyHTML = `
          <h3>All Borrow History</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>User</th>
                <th>Book</th>
                <th>Borrow Date</th>
                <th>Return Date</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
        `

        history.forEach((record) => {
          historyHTML += `
            <tr>
              <td>${record.id}</td>
              <td>${record.userName}</td>
              <td>${record.bookTitle}</td>
              <td>${record.borrowDate}</td>
              <td>${record.returnDate || "Not returned"}</td>
              <td>${record.status}</td>
            </tr>
          `
        })

        historyHTML += `
            </tbody>
          </table>
        `

        container.innerHTML = historyHTML
        toggleList("borrowHistoryContainer", button)
      }
    } catch (error) {
      console.error("Error viewing borrow history:", error)
      alert("Failed to load borrow history. Please try again.")
    }
  } else {
    alert("Access Denied!")
  }
}

// Fetch and display personal borrow history
async function viewMyHistory() {
  const button = document.querySelector('[data-action="list-my-history"]')
  const container = document.getElementById("borrowHistoryContainer")

  if (!container.classList.contains("hidden")) {
    toggleList("borrowHistoryContainer", button)
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/borrow?userId=1`) // Replace with logged-in user ID

    if (!response.ok) {
      throw new Error(`Failed to fetch my borrow history: ${response.status}`)
    }

    const history = await response.json()

    if (container) {
      let historyHTML = `
        <h3>My Borrow History</h3>
        <table class="data-table">
          <thead>
            <tr>
              <th>Book</th>
              <th>Borrow Date</th>
              <th>Return Date</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
      `

      history.forEach((record) => {
        historyHTML += `
          <tr>
            <td>${record.bookTitle}</td>
            <td>${record.borrowDate}</td>
            <td>${record.returnDate || "Not returned"}</td>
            <td>${record.status}</td>
          </tr>
        `
      })

      historyHTML += `
          </tbody>
        </table>
      `

      container.innerHTML = historyHTML
      toggleList("borrowHistoryContainer", button)
    }
  } catch (error) {
    console.error("Error viewing my borrow history:", error)
    alert("Failed to load your borrow history. Please try again.")
  }
}

// Logout function
async function logout() {
  try {
    await fetch(`${API_BASE_URL}/users/logout`, { method: "POST" })
  } catch (error) {
    console.error("Logout error:", error)
  } finally {
    // Clear session storage and redirect regardless of API response
    sessionStorage.clear()
    window.location.href = "login.html"
  }
}

// And modify viewBorrowHistory() to not automatically call itself:
if (window.location.pathname.includes("borrow-history.html")) {
  const viewAllButton = document.querySelector(".primary-button")
  if (viewAllButton) {
    viewAllButton.addEventListener("click", viewBorrowHistory)
  }
  const viewMyHistoryButton = document.querySelector(".secondary-button")
  if (viewMyHistoryButton) {
    viewMyHistoryButton.addEventListener("click", viewMyHistory)
  }
}
// Register function
document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.getElementById("registerForm");
    if (registerForm) {
        registerForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const name = document.getElementById("name").value;
            const email = document.getElementById("email").value;
            const password = document.getElementById("password").value;
            const messageEl = document.getElementById("message");

            const response = await fetch("http://localhost:8080/LibraryManagementSystem/users", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=STUDENT`
            });

            const result = await response.json();
            if (response.ok) {
                messageEl.innerText = "Registration successful! Redirecting to login...";
                messageEl.style.color = "green";
                setTimeout(() => {
                    window.location.href = "login.html";
                }, 2000);
            } else {
                messageEl.innerText = result.error || "Registration failed";
                messageEl.style.color = "red";
            }
        });
    }
});
// Handle Change Password Form Submission
async function handleChangePassword(event) {
    event.preventDefault();

    const oldPassword = document.getElementById("oldPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const messageEl = document.getElementById("passwordMessage");

    try {
        const response = await fetch("http://localhost:8080/LibraryManagementSystem/users/updatePassword", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`
        });

        const result = await response.json();
        if (response.ok) {
            messageEl.innerText = "Password updated successfully!";
            messageEl.style.color = "green";
            document.getElementById("changePasswordForm").reset();
        } else {
            messageEl.innerText = result.error || "Failed to update password";
            messageEl.style.color = "red";
        }
    } catch (error) {
        console.error("Error updating password:", error);
        messageEl.innerText = "An error occurred. Please try again.";
        messageEl.style.color = "red";
    }
}

// Attach event listener when page loads
document.addEventListener("DOMContentLoaded", function () {
    const changePasswordForm = document.getElementById("changePasswordForm");
    if (changePasswordForm) {
        changePasswordForm.addEventListener("submit", handleChangePassword);
    }
});

