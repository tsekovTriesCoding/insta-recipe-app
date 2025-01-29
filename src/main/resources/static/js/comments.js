document.addEventListener("DOMContentLoaded", function () {
    const commentsList = document.querySelector(".comments-section");

    const recipeId = commentsList.querySelector('#recipeId').value;
    const loggedInUser = document.querySelector("#loggedInUser").value;
    const recipeCreator = document.querySelector("#recipeCreator").value;

    function fetchComments() {
        fetch(`/api/comments/${recipeId}`)
            .then(response => {
                if (response.status === 204) {
                    return []; // Return an empty array if no content
                }

                if (!response.ok) {
                    throw new Error(`Error fetching comments: ${response.status}`);
                }

                return response.json();
            })
            .then(comments => {
                // Clear existing comments
                const existingComments = commentsList.querySelectorAll(".comment-card");
                existingComments.forEach(comment => comment.remove());

                if (comments.length === 0) {
                    // If no comments, show a message
                    const commentCard = `
                        <div class="comment-card card mb-3">
                            <div class="card-body">
                                <p class="no-comments text-center text-muted">No comments yet. Be the first to comment!</p>
                    `;
                    commentsList.insertAdjacentHTML("beforeend", commentCard);
                } else {
                    // Add new comments
                    comments.forEach(comment => {
                        // Show delete button if user is comment creator OR recipe creator
                        const canDelete = (comment.createdBy === loggedInUser || loggedInUser === recipeCreator);
                        const deleteButton = canDelete ?
                            `<button class="btn btn-danger btn-sm delete-comment" data-comment-id="${comment.id}">Delete</button>`
                            : '';

                        const commentCard = `
                        <div class="comment-card card mb-3">
                            <div class="card-body">
                                <p class="comment-author mb-1">${comment.createdBy}</p>
                                <p class="comment-text">${comment.content}</p>
                                <p class="comment-time text-end mb-0">${new Date(comment.createdDate).toLocaleString()}</p>
                                 <div class="text-end mt-2">
                                    ${deleteButton}
                                </div>
                    `;
                        commentsList.insertAdjacentHTML("beforeend", commentCard);
                    });
                }
                document.querySelectorAll(".delete-comment").forEach(button => {
                    button.addEventListener("click", function () {
                        const commentId = this.getAttribute("data-comment-id");
                        confirmDelete(commentId);
                    });
                });
            })
            .catch(error => {
                console.error("Error fetching comments:", error);
            });
    }

    function confirmDelete(commentId) {
        if (confirm("Are you sure you want to delete this comment?")) {
            deleteComment(commentId);
        }
    }

    function deleteComment(commentId) {
        const csrfToken = document.getElementById("csrfToken").value;

        fetch(`/api/comments/delete/${commentId}`, {
            method: "DELETE",
            headers: {
                "X-CSRF-TOKEN": csrfToken // Use the CSRF token from the form
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to delete comment");
                }
                fetchComments(); // Refresh comments after deletion
            })
            .catch(error => console.error("Error deleting comment:", error));
    }

    fetchComments();
});