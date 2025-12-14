# DailyBook — Backend

DailyBook is a backend-first social blogging platform that enables users to write posts, follow authors, manage content visibility, and receive notifications.

The system is inspired by Medium-like writing platforms and focuses on **secure authentication**, **social graph logic**, and **strict server-side authorization** rather than frontend-heavy features.

## ✨ Core Capabilities

- Secure user authentication using JWT
- Blog post creation with multiple visibility levels
- Social following system with request–approval workflow
- Notification system for social interactions
- Strict backend-enforced access control

## 🔐 Authentication & Security

- User registration and login using **JWT-based authentication**
- Token expiration and validation
- Secured REST APIs using **Spring Security 6**
- All protected routes require valid authentication tokens
- Authorization checks enforced at service layer

Sensitive configuration such as JWT secrets and database credentials are managed using environment variables.

## 📝 Blog Posts

Users can create, update, and delete blog posts.

Each post supports:
- Title and content
- Tags for categorization
- Images uploaded via a cloud-based image storage service

Each post has one of the following visibility levels:

- `PUBLIC` — visible to all users
- `PRIVATE` — visible only to the author
- `FOLLOWERS_ONLY` — visible only to approved followers

Post visibility and access control are **strictly enforced on the backend**, independent of frontend behavior.

## 🖼️ Image Uploads

The backend supports cloud-based image uploads for blog posts and user profiles.

- Images are uploaded from the client to a cloud storage service
- The backend stores and serves only secure image URLs
- This approach avoids local file storage and improves scalability

Image upload handling is integrated with authenticated endpoints to prevent unauthorized uploads.


## 👥 Social Features

DailyBook implements a request-based follow system:

- Users can send follow requests
- Requests must be approved by the recipient
- Users can reject follow requests
- Approved followers gain access to followers-only posts
- Users can unfollow at any time

APIs are provided to fetch:
- Followers list
- Following list
- Pending follow requests

## 🔔 Notifications

The notification system informs users about social interactions:

- Follow request received
- Follow request approved

Features include:
- Fetch all notifications
- Fetch unread notification count
- Mark individual notifications as read
- Mark all notifications as read

## 🏗️ Tech Stack

**Backend**
- Java 17+
- Spring Boot
- Spring Security 6
- JWT Authentication
- MongoDB
- Cloud-based Image Storage
- Maven


## 📐 Architecture Overview

The backend follows a layered architecture:

- **Controller Layer** — Handles HTTP requests and responses
- **Service Layer** — Business logic and authorization checks
- **Repository Layer** — MongoDB data access
- **DTOs** — Request and response contracts
- **Security Layer** — JWT filters and access validation

Authorization and visibility rules are enforced at the service layer to ensure data security.

## 🔗 API Highlights

- `POST /auth/register` — Register a new user
- `POST /auth/login` — Authenticate and receive JWT
- `POST /entries` — Create a blog post
- `GET /entries/explore` — Get public posts
- `POST /follow/request/{userId}` — Send follow request
- `POST /follow/approve/{requestId}` — Approve follow request
- `GET /notifications` — Fetch notifications

## ⚙️ Environment Configuration

```properties
spring.application.name=DailyBook
server.port=8080

spring.data.mongodb.uri=${MONGODB_URI}

jwt.secret=${JWT_SECRET}
jwt.expiration=604800000
```

---

## Running the Backend Locally


```md
## 🚀 Running Locally

```bash
git clone https://github.com/anupamchaubey/Daily-Book
cd dailybook-backend
mvn spring-boot:run
```


---

## 🎯 Project Focus

This project was built with a **backend-first approach**, emphasizing:

- Secure authentication and authorization
- Social graph design (followers & requests)
- Backend-enforced content visibility
- Cloud-based image upload handling
- Clean and maintainable REST APIs

The frontend is intentionally lightweight to keep the focus on backend engineering.

## 🧠 Future Improvements

- Redis caching for feeds
- Pagination and infinite scrolling
- Rate limiting
- Advanced search and filtering
- Unit and integration testing

## 👤 Author

**Anupam**  
B.Tech CSE  
Backend / SDE Aspirant

## 📄 License

This project is licensed under the MIT License.
