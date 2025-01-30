document.addEventListener("DOMContentLoaded", function () {
    fetchDashboardData();
});

function fetchDashboardData() {
    Promise.all([
        fetch("/api/admin/total-users").then(res => res.json()),
        fetch("/api/admin/total-recipes").then(res => res.json()),
        fetch("/api/admin/total-comments").then(res => res.json()),
        fetch("/api/admin/total-likes").then(res => res.json())
    ])
        .then(([usersCount, recipesCount, commentsCount, likesCount]) => {
            document.getElementById("totalUsers").textContent = usersCount;
            document.getElementById("totalRecipes").textContent = recipesCount;
            document.getElementById("totalComments").textContent = commentsCount;
            document.getElementById("totalLikes").textContent = likesCount;

            updateChart(usersCount, recipesCount, commentsCount, likesCount);
        })
        .catch(error => console.error("Error fetching dashboard data:", error));
}

function updateChart(users, recipes, comments, likes) {
    const ctx = document.getElementById("adminChart").getContext("2d");
    new Chart(ctx, {
        type: "bar",
        data: {
            labels: ["Users", "Recipes", "Comments", "Likes"],
            datasets: [{
                label: "Admin Statistics",
                data: [users, recipes, comments, likes],
                backgroundColor: ["#007bff", "#28a745", "#ffc107", "#dc3545"],
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}