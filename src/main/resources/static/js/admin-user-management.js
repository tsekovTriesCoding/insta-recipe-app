document.addEventListener("DOMContentLoaded", function () {
    const userTableBody = document.getElementById("userTableBody");

    function fetchUsers() {
        fetch("/api/admin/users")
            .then(response => {
                //TODO: check if the response is 204 no content!
                if (!response.ok) {
                    throw new Error("Failed to fetch users");
                }
                return response.json();
            })
            .then(users => {
                userTableBody.innerHTML = "";
                users.forEach(user => {
                    const userRow = `
                        <tr>
                            <td>${user.username}</td>
                            <td>${user.email}</td>
                            <td>${user.role}</td>
                            <td>${user.active ? 'Active' : 'Inactive'}</td>
                            <td>
                                <button class="btn btn-warning btn-sm update-role" data-id="${user.id}">Update Role</button>
                                <button class="btn btn-danger btn-sm delete-user" data-id="${user.id}">Delete</button>
                            </td>
                        </tr>
                    `;
                    userTableBody.insertAdjacentHTML("beforeend", userRow);
                });
            })
            .catch(error => console.error("Error fetching users:", error));
    }

    userTableBody.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-user")) {
            const userId = event.target.getAttribute("data-id");
            deleteUser(userId);
        }
        if (event.target.classList.contains("update-role")) {
            const userId = event.target.getAttribute("data-id");
            updateUserRole(userId);
        }
    });

    function deleteUser(userId) {
        if (!confirm("Are you sure you want to delete this user?")) return;

        fetch(`/api/admin/users/${userId}`, {method: "DELETE"})
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to delete user");
                }
                fetchUsers();
            })
            .catch(error => console.error("Error deleting user:", error));
    }

    function updateUserRole(userId) {
        const newRole = prompt("Enter new role (e.g., User, Admin):");
        if (!newRole) return;

        const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
        const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

        fetch(`/api/admin/users/${userId}/role`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({role: newRole})
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to update role");
                }
                fetchUsers();
            })
            .catch(error => console.error("Error updating user role:", error));
    }

    fetchUsers();
});
