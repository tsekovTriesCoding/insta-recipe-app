

document.addEventListener("DOMContentLoaded", function () {
    const commentsList = document.querySelector(".comments-section");

    // Assuming you have a data attribute or another way to get the recipe ID
    const recipeId = commentsList.querySelector('#recipeId').value;

    // Function to fetch comments
    function fetchComments() {
        fetch(`/api/comments/${recipeId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error fetching comments: ${response.status}`);
                }
                return response.json();
            })
            .then(comments => {
                // Clear existing comments
                const existingComments = commentsList.querySelectorAll(".comment-card");
                existingComments.forEach(comment => comment.remove());

                // Add new comments
                comments.forEach(comment => {
                    const commentCard = `
                        <div class="comment-card card mb-3">
                            <div class="card-body">
                                <p class="comment-author mb-1">${comment.createdBy}</p>
                                <p class="comment-text">${comment.content}</p>
                                <p class="comment-time text-end mb-0">${new Date(comment.createdDate).toLocaleString()}</p>
                            </div>
                        </div>
                    `;
                    commentsList.insertAdjacentHTML("beforeend", commentCard);
                });
            })
            .catch(error => {
                console.error("Error fetching comments:", error);
            });
    }

    fetchComments();
});