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

Database: MySQL (H2DB for testing)

Security: Spring Security

API Communication: Feign Client (for microservices)

Architecture: Feature-based project structure

Testing: JUnit, Mockito

Cloudinary – Cloud storage for images

## ⚙️ Installation

1️⃣ Clone the repository

```bash
git clone https://github.com/yourusername/InstaRecipeApp.git
```

2️⃣ Navigate to the project directory:

```
cd InstaRecipe 
```

3️⃣ Set up the database

-Ensure MySQL is running

-Update application.properties with your database credentials

4️⃣ Set Up Cloudinary

1. Create an account at Cloudinary.

2. Retrieve your API credentials (Cloud Name, API Key, and API Secret).

3. Add the following properties to application.properties:

```
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

5️⃣ Run the application

``` bash 
mvn spring-boot:run
```

6️⃣ Open the application in your browser:

Visit: ```http://localhost:8080```

## 📦 Run with Docker

You can run the entire application stack (main app + MySQL + activity log microservice) in Docker containers using docker-compose.

1️⃣ Create a .env file
Create a .env file in the root directory with the following:
```MAIN_DB_NAME=insta_recipe_db
ACTIVITY_DB_NAME=activity_log_db
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
CLOUD_NAME=your_cloudinary_cloud_name
API_KEY=your_cloudinary_api_key
API_SECRET=your_cloudinary_api_secret
```
🛑 Do not commit this file to version control.

2️⃣ Build and Run Containers

```docker-compose up --build```

This will start:

- insta-recipe-app (Spring Boot app)

- main-app-db (MySQL for the main app)

- activity-log (activity logging microservice)

- activity-log-db (MySQL for the activity log service)

3️⃣ Access the Application

Open your browser at:
http://localhost:8080

## 📜 License

This project is licensed under the MIT License.
