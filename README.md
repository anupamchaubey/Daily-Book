# Daily-Book — Backend REST API

Daily-Book is a clean, lightweight social blogging backend built with Java 17 and Spring Boot 3. It provides secure JWT authentication, social following, and strict service-layer authorization for blog posts across three visibility levels: `PUBLIC`, `PRIVATE`, and `FOLLOWERS_ONLY`.

---

## 🚀 Key Features

- **JWT Authentication & Password Hashing**: Stateless authentication using Spring Security and `BCryptPasswordEncoder`.
- **Strict Server-Side Post Visibility**:
  - `PUBLIC`: Accessible by any user (authenticated or anonymous).
  - `PRIVATE`: Accessible only by the author.
  - `FOLLOWERS_ONLY`: Accessible only by the author and users who follow the author.
- **Ownership Verification**: Authorship and user identity are verified strictly against Spring Security's authenticated principal (`Authentication.getName()`), preventing client spoofing.
- **Social Following**: Users can follow/unfollow authors and retrieve their follower/following lists.
- **Timeline Feed**: Personalized chronological feed showing posts from followed authors and own posts.
- **Pagination & Search**: Spring Data `Pageable` on all list endpoints and keyword-based search on public posts.
- **Clean DTOs & Validation**: Input validation via `jakarta.validation` (`@NotBlank`, `@Size`, `@Email`) with zero entity exposure.
- **Global Exception Handling**: Centralized `@RestControllerAdvice` returning standard HTTP error status codes (400, 401, 403, 404, 409).

---

## 🛠️ Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.4.2 (Spring Web, Spring Security, Spring Data MongoDB, Spring Validation)
- **Database**: MongoDB
- **Security**: Spring Security + JSON Web Token (`jjwt` 0.11.5) + BCrypt
- **Build Tool**: Maven

---

## 🏛️ Architecture

The project adheres strictly to standard Spring Boot layered architecture:

```
HTTP Client
    │
    ▼
Controller Layer (/api/auth, /api/posts, /api/users, /api/follow)
    │  (Handles HTTP requests, path variables, DTO validation, HTTP status codes)
    ▼
Service Layer (AuthService, EntryService, UserService, FollowService)
    │  (Enforces business logic, authorization rules, ownership checks)
    ▼
Repository Layer (UserRepository, EntryRepository, FollowRepository)
    │  (Spring Data MongoRepository queries and pagination)
    ▼
MongoDB
```

---

## 🔐 Authorization & Post Visibility Rules

| Action / Resource | Permission Rule |
| :--- | :--- |
| **Register / Login** | Public access |
| **Get Public Posts** (`PUBLIC`) | Public access (anonymous or authenticated) |
| **Get Followers-Only Post** (`FOLLOWERS_ONLY`) | Post author OR authenticated users who follow the author |
| **Get Private Post** (`PRIVATE`) | Post author only |
| **Create Post** | Authenticated users only |
| **Update Post** | Post author only (verified via Security Context) |
| **Delete Post** | Post author only (verified via Security Context) |
| **Follow / Unfollow** | Authenticated users only |
| **Timeline Feed** | Authenticated user only (shows followed users + own posts) |

---

## 📡 API Endpoints

### 1. Authentication (`/api/auth`)

| Method | Endpoint | Auth Required | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | No | Register a new user | `201 Created` |
| `POST` | `/api/auth/login` | No | Authenticate and obtain JWT token | `200 OK` |

### 2. User Management (`/api/users`)

| Method | Endpoint | Auth Required | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Yes | Get authenticated user profile | `200 OK` |
| `GET` | `/api/users/{username}` | No | Get public user details by username | `200 OK` |

### 3. Posts (`/api/posts`)

| Method | Endpoint | Auth Required | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/posts` | Yes | Create a new post (`PUBLIC`, `PRIVATE`, `FOLLOWERS_ONLY`) | `201 Created` |
| `GET` | `/api/posts` | No | List all public posts (paged: `?page=0&size=10`) | `200 OK` |
| `GET` | `/api/posts/{id}` | Optional | Get post by ID (visibility enforced by service) | `200 OK` |
| `PUT` | `/api/posts/{id}` | Yes | Update own post (author only) | `200 OK` |
| `DELETE` | `/api/posts/{id}` | Yes | Delete own post (author only) | `200 OK` |
| `GET` | `/api/posts/me` | Yes | List authenticated user's own posts (paged) | `200 OK` |
| `GET` | `/api/posts/user/{username}` | Optional | List author's visible posts for current viewer | `200 OK` |
| `GET` | `/api/posts/feed` | Yes | Chronological feed of followed users and self | `200 OK` |
| `GET` | `/api/posts/search` | No | Search public posts by keyword (`?q=keyword`) | `200 OK` |

### 4. Following (`/api/follow`)

| Method | Endpoint | Auth Required | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/follow/{username}` | Yes | Follow a user | `200 OK` |
| `DELETE` | `/api/follow/{username}` | Yes | Unfollow a user | `200 OK` |
| `GET` | `/api/follow/followers` | Yes | List usernames following the authenticated user | `200 OK` |
| `GET` | `/api/follow/following` | Yes | List usernames the authenticated user follows | `200 OK` |

---

## ⚙️ Environment Configuration

Configuration properties in `src/main/resources/application.properties`:

| Property | Default Value | Description |
| :--- | :--- | :--- |
| `server.port` | `8080` | Port on which the Spring Boot server runs |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/dailybook` | MongoDB connection URI |
| `jwt.secret` | (Base64-encoded 256-bit secret) | HMAC-SHA256 signing secret key |
| `jwt.expiration` | `604800000` (7 days in ms) | JWT validity duration |

---

## 🏃 How to Run

### Prerequisites
- JDK 17+ installed
- MongoDB running locally on port 27017 (or MongoDB Atlas connection URI)
- Maven 3.8+ installed (or use included `mvnw`)

### 1. Run with Maven
```bash
# Clone the repository
git clone https://github.com/anupamchaubey/Daily-Book.git
cd Daily-Book

# Build the project
mvn clean package -DskipTests

# Run the Spring Boot application
mvn spring-boot:run
```

Or pass custom environment variables:
```bash
export MONGODB_URI="mongodb://localhost:27017/dailybook"
export JWT_SECRET="Y2hhbmdlLXlvdXItanZ0LXNlY3JldC1rZXktd2l0aC1hdC1sZWFzdC0zMi1ieXRlcy1sb25nLWZvb2Jhcg=="
mvn spring-boot:run
```

### 2. Run with Docker
```bash
# Build Docker image
docker build -t daily-book-backend .

# Run container (connecting to host or remote MongoDB)
docker run -p 8080:8080 \
  -e MONGODB_URI="mongodb://host.docker.internal:27017/dailybook" \
  daily-book-backend
```

---

## 📄 License
This project is licensed under the MIT License.
