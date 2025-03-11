# InstaRecipeApp 🍽️
A modern recipe-sharing platform built with Spring Boot & Thymeleaf.

## 🚀 Features

✅ User authentication (register/login/logout)

✅ Create, edit, and delete recipes

✅ Categorize recipes into categories

✅ Like and comment on recipes

✅ Favorite recipes for later access

✅ Activity logging for key user actions (added recipes, comments, etc.)

✅ REST API integration for a separate activity log microservice

## 📸 Screenshots
(Add images/gifs showcasing UI if possible.)

## 🛠️ Tech Stack
Backend: Java, Spring Boot, Spring MVC

Frontend: Thymeleaf, Bootstrap

Database: MySQL (HSQLDB for testing)

Security: Spring Security

API Communication: Feign Client (for microservices)

Architecture: Feature-based project structure

Testing: JUnit, Mockito

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

## 📝 Logging & Activity Tracking
The app logs user activities (like recipe creation, comments, favorites) using events.

A separate microservice stores logs via a Feign Client.

## 📜 License
This project is licensed under the MIT License.
