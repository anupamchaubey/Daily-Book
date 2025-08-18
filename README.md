<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/8f4208a9-6e6d-4787-ae83-e37c9ed90132" />

API endpoints:

| Method | Endpoint    | Description                      | Access |
| ------ | ----------- | -------------------------------- | ------ |
| POST   | `/register` | Register a new user              | Public |
| POST   | `/login`    | Authenticate user and return JWT | Public |

| Method | Endpoint       | Description                     | Access        |
| ------ | -------------- | ------------------------------- | ------------- |
| GET    | `/api/profile` | Get logged-in user’s profile    | Authenticated |
| PUT    | `/api/profile` | Update logged-in user’s profile | Authenticated |
Response includes: id, username, bio, profilePicture, joinedAt.


| Method | Endpoint            | Description                                        |
| ------ | ------------------- | -------------------------------------------------- |
| POST   | `/api/entries`      | Create a new entry                                 |
| GET    | `/api/entries`      | Get all entries of logged-in user                  |
| GET    | `/api/entries/{id}` | Get a specific entry by ID (only if owned by user) |
| PUT    | `/api/entries/{id}` | Update an entry (only if owned by user)            |
| DELETE | `/api/entries/{id}` | Delete an entry (only if owned by user)            |

| Method | Endpoint                              | Description                                         |
| ------ | ------------------------------------- | --------------------------------------------------- |
| GET    | `/api/entries/public`                 | List all public entries (optional `tag` filter)     |
| GET    | `/api/entries/public/user/{username}` | List all public entries by a specific user          |
| GET    | `/api/entries/public/search?q=...`    | Search public entries by query                      |
| GET    | `/api/entries/feed`                   | List public feed entries (simplified, no followers) |
Entry response includes: id, title, content, tags, visibility, createdAt, updatedAt, authorId, authorUsername, authorProfilePicture

JWT authentication is required for all private endpoints.

Passwords are hashed using BCrypt.

CSRF disabled; session is stateless.