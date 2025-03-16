# InstaRecipeApp 🍽️

A modern recipe-sharing web application built with Spring Boot, Spring Security, Spring MVC and Thymeleaf. It allows
users to register, share recipes, interact with other users' recipes, and manage their recipes and profiles.

## 🚀 Features
🔐 User Management

* User Registration & Authentication: Secure user authentication with Spring Security.
* Profile Management: Users can update their profile picture, username, email, and password.
* Profile Picture Storage: User profile pictures are stored in Cloudinary.
* Account Deactivation: Accounts are automatically deactivated if the user has not logged in for the past 6 months.
* Last Login Tracking: After each successful login, the last login date is updated.

🍽️ Recipe Management

* Add & Delete Recipes: Users can create and delete their own recipes.
* Edit Recipes: Users can modify their recipes.
* Favorites: Users can add or remove recipes from their favorites list.
* Likes: Users can like recipes that are not their own, but only once per recipe.
* Recipe Picture Storage: Recipe images are stored in Cloudinary.
* Categories: Recipes are categorized using a Category entity with predefined categories.

💬 Comments & Interaction

* Commenting: Users can comment on recipes.
* Delete Comments:
  * Users can delete their own comments.
  * Recipe owners can delete comments on their recipes.

📝 Activity Logging

* Logs successful user actions such as:
  * Registration
  * Adding/deleting recipes or comments
  * Updating profile information
  * Adding/removing recipes from favorites

⏳ Background Jobs
* Scheduled Task: Deactivates users who haven't logged in for the past 6 months.

## 📸 Screenshots

(Add images/gifs showcasing UI if possible.)

## 🛠️ Tech Stack

Backend: Java, Spring Boot, Spring MVC

Frontend: Thymeleaf, Bootstrap

ORM: Hibernate

Database: MySQL (HSQLDB for testing)

Security: Spring Security

API Communication: Feign Client (for microservices)

Architecture: Feature-based project structure

Testing: JUnit, Mockito

Cloudinary – Cloud storage for images

## ⚙️ Installation

1️⃣ Clone the repository

```bash
git clone https://github.com/yourusername/InstaRecipeApp.git
cd InstaRecipeApp
```

2️⃣ Set up the database

-Ensure MySQL is running

-Update application.properties with your database credentials

3️⃣ Run the application

``` bash 
mvn spring-boot:run
```

4️⃣ Access the app

Visit: ```http://localhost:8080```

## 📜 License

This project is licensed under the MIT License.
