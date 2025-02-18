document.addEventListener("DOMContentLoaded", function () {
    const recipesTableBody = document.getElementById("recipeTableBody");

    function fetchRecipes() {
        fetch("/api/admin/recipes")
            //TODO: check if the response is 204 no content!
            .then(response => response.json())
            .then(recipes => {
                recipesTableBody.innerHTML = "";
                recipes.forEach(recipe => {
                    const row = `<tr>
                        <td>${recipe.title}</td>
                        <td>${recipe.author}</td>
                        <td>${new Date(recipe.createdDate).toLocaleDateString()}</td>
                        <td>
                            <a href="/recipes/${recipe.id}" class="btn btn-info btn-sm">View</a>
                            <button class="btn btn-danger btn-sm delete-recipe" data-id="${recipe.id}">Delete</button>
                        </td>
                    </tr>`;
                    recipesTableBody.insertAdjacentHTML("beforeend", row);
                });
            })
            .catch(error => console.error("Error fetching recipes:", error));
    }

    recipesTableBody.addEventListener("click", function (event) {
        if (event.target.classList.contains("delete-recipe")) {
            const recipeId = event.target.dataset.id;
            if (confirm("Are you sure you want to delete this recipe?")) {
                const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
                const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

                fetch(`/api/admin/recipes/${recipeId}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        [csrfHeader]: csrfToken
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            fetchRecipes();
                        } else {
                            console.error("Failed to delete recipe");
                        }
                    })
                    .catch(error => console.error("Error deleting recipe:", error));
            }
        }
    });

    fetchRecipes();
});
