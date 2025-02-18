document.addEventListener("DOMContentLoaded", function () {
    const commentsTableBody = document.getElementById("commentTableBody");
    const commentsTable = document.getElementById("commentsTable");
    const noCommentsMessage = document.getElementById("noCommentsMessage");

    function fetchComments() {
        fetch("/api/admin/comments")
            .then(response => {
                if (response.status === 204) {  // If No Content is received
                    commentsTable.style.display = "none"; // Hide table
                    noCommentsMessage.style.display = "block"; // Show message
                    return [];
                }
                return response.json();
            })
            .then(comments => {
                commentsTableBody.innerHTML = "";

                if (comments.length === 0) {
                    commentsTable.style.display = "none";
                    noCommentsMessage.style.display = "block";
                    return;
                }

                commentsTable.style.display = "table";
                noCommentsMessage.style.display = "none";

                comments.forEach(comment => {
                    const row = `<tr>
                        <td>${comment.author}</td>
                        <td>${comment.content}</td>
                        <td>${new Date(comment.createdDate).toLocaleDateString()}</td>
                        <td>
                            <button class="btn btn-danger btn-sm delete-comment" data-id="${comment.id}">Delete</button>
                        </td>
                    </tr>`;
                    commentsTableBody.insertAdjacentHTML("beforeend", row);
                });
            })
            .catch(error => console.error("Error fetching comments:", error));
    }

    commentsTableBody.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-comment")) {
            const commentId = event.target.dataset.id;
            if (confirm("Are you sure you want to delete this comment?")) {
                const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
                const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

                fetch(`/api/admin/comments/${commentId}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        [csrfHeader]: csrfToken
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            fetchComments();
                        } else {
                            console.error("Failed to delete comment");
                        }
                    })
                    .catch(error => console.error("Error deleting comment:", error));
            }
        }
    });

    fetchComments();
});