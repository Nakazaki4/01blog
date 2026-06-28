# 01Blog

A full-stack social blogging platform where users can publish posts (text + media), follow other users, like and comment, receive notifications, and report inappropriate content. Admins can moderate users, posts, and reports.

## Tech Stack

**Backend**
- Java 17, Spring Boot 4.0
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL
- JWT auth (jjwt 0.12)
- Thumbnailator + webp-imageio (image processing)
- Maven

**Frontend**
- Angular 21 (with SSR)
- Angular Material + Tailwind CSS 4
- RxJS, marked, DOMPurify
- Vitest

**Infrastructure**
- Docker / Docker Compose
- Supabase Storage (media bucket)

## Prerequisites

- Docker and Docker Compose — for the one-command setup
- For local (non-Docker) development:
  - Java 17+ and Maven 3.9+
  - Node.js 20+ and npm 11+
  - PostgreSQL 14+

## Setup

### 1. Clone

```bash
git clone <repo-url>
cd 01blog
```

### 2. Configure environment

Create `backend/.env` with the following variables:

```env
# Database (when running backend locally; Docker Compose overrides DB_URL)
DB_URL=jdbc:postgresql://localhost:5433/mainDB
DB_USERNAME=abdnour
DB_PASSWORD=abdnour

# Seeded admin account
ADMIN_EMAIL=admin@example.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=changeme

# JWT
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRATION_MS=3600000

# Supabase media storage
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_SERVICE_KEY=your-service-role-key
SUPABASE_BUCKET=Media
```

### 3. Run with Docker (recommended)

```bash
docker-compose up --build
```

Services:
- Frontend → http://localhost:4200
- Backend API → http://localhost:8080/api
- PostgreSQL → localhost:5433 (user `abdnour` / db `mainDB`)

The schema is auto-created by Hibernate (`ddl-auto: update`) and the admin account is seeded on first boot.

### 4. Run locally (without Docker)

Start PostgreSQL (or just the `db` service: `docker-compose up db`), then:

```bash
# Backend
cd backend
./mvnw spring-boot:run
```

```bash
# Frontend
cd frontend
npm install
npm start
```

## Useful commands

```bash
# Backend tests
cd backend && ./mvnw test

# Frontend tests
cd frontend && npm test

# Production build
cd frontend && npm run build
```

---

# API Documentation

Base URL: `http://localhost:8080/api`

## Conventions

- All requests/responses use `application/json` unless noted (media upload uses `multipart/form-data`).
- Authenticated endpoints require header: `Authorization: Bearer <jwt>`.
- Pagination query params: `?page=0&size=10` (zero-indexed). Responses return Spring `Page<T>` shape:
  ```json
  { "content": [...], "totalElements": 42, "totalPages": 5, "number": 0, "size": 10 }
  ```
- Errors use this shape:
  ```json
  { "timestamp": "2026-05-14T12:00:00Z", "status": 400, "error": "Bad Request", "message": "Email already in use" }
  ```
- Status codes: `200` OK, `201` Created, `204` No Content, `400` validation, `401` no/invalid token, `403` forbidden, `404` not found, `409` conflict.

---

## Auth

### `POST /api/auth/signup`
Create a new user account. Public.

**Request body:**
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "secret123"
}
```

**Response 201:**
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "username": "alice",
  "role": "USER"
}
```

**Errors:** `409` if email/username taken, `400` if validation fails.

---

### `POST /api/auth/login`
Authenticate and receive a JWT. Public.

**Request body:** (the `username` field accepts either a username or an email)
```json
{ "username": "alice", "password": "secret123" }
```

**Response 200:**
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "username": "alice",
  "role": "USER"
}
```

**Errors:** `401` if credentials wrong, `403` if user is banned.

---

### `GET /api/auth/me`
Return the currently authenticated user. Requires auth.

**Response 200:**
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "bio": "Learning Spring Boot",
  "avatarUrl": "/api/media/abc.png",
  "role": "USER",
  "createdAt": "2026-05-01T10:00:00Z"
}
```

---

## Users

### `GET /api/users/{id}`
Get a user's public profile. Public.

**Response 200:**
```json
{
  "id": 2,
  "username": "bob",
  "bio": "Frontend dev",
  "avatarUrl": "/api/media/bob.png",
  "subscriberCount": 12,
  "subscriptionCount": 8,
  "isSubscribed": false,
  "createdAt": "2026-04-15T09:00:00Z"
}
```

`isSubscribed` reflects whether the **caller** is subscribed to this user (false if unauthenticated).

---

### `GET /api/users/{id}/posts`
List a user's posts, paginated. Public.

**Query params:** `page`, `size`

**Response 200:** `Page<PostDto>` (see Post shape under Posts section).

---

### `GET /api/users/{id}/subscribers`
List users subscribed to this user. Public.

**Response 200:**
```json
[
  { "id": 3, "username": "charlie", "avatarUrl": "/api/media/c.png" },
  { "id": 4, "username": "dora", "avatarUrl": null }
]
```

---

### `GET /api/users/{id}/subscriptions`
List users this user is subscribed to. Public.

**Response 200:** same shape as `/subscribers`.

---

### `POST /api/users/{id}/subscribe`
Subscribe to a user. Requires auth.

**Request body:** none

**Response 204:** no body.

**Errors:** `400` if trying to subscribe to self, `409` if already subscribed.

---

### `DELETE /api/users/{id}/subscribe`
Unsubscribe. Requires auth.

**Response 204:** no body.

---

### `PATCH /api/users/me`
Update own profile. Requires auth.

**Request body:** (any subset)
```json
{ "bio": "New bio text", "avatarUrl": "/api/media/new.png" }
```

**Response 200:** updated user object (same shape as `GET /auth/me`).

---

## Posts

### `GET /api/posts/feed`
Get the feed. Dual-mode:
- Authenticated: paginated personalized feed (posts from subscribed users).
- Unauthenticated: a public preview of recent posts (no pagination).

**Query params:** `page`, `size` (ignored when unauthenticated)

**Response 200:** `Page<PostDto>` when authenticated, plain `List<PostDto>` when not.

**PostDto shape:**
```json
{
  "id": 10,
  "author": { "id": 2, "username": "bob", "avatarUrl": "/api/media/bob.png" },
  "description": "Learned about JWT today",
  "mediaUrl": "/api/media/post10.png",
  "mediaType": "IMAGE",
  "likeCount": 5,
  "commentCount": 2,
  "isLiked": true,
  "createdAt": "2026-05-14T08:30:00Z",
  "updatedAt": null
}
```

---

### `GET /api/posts/{id}`
Get a single post. Public.

**Response 200:** `PostDto`

---

### `POST /api/posts`
Create a post. Requires auth. **Content-Type: `multipart/form-data`**.

**Form fields:**
- `description` (string, required)
- `media` (file, optional — image/jpeg, image/png, video/mp4, max 10MB)

**Response 201:** `PostDto`

**Errors:** `400` if media format invalid or size exceeded.

---

### `PUT /api/posts/{id}`
Update a post (owner only). Requires auth.

**Request body:**
```json
{ "description": "Updated text" }
```

Media replacement uses multipart on a separate flow (or include media in this multipart request).

**Response 200:** updated `PostDto`

**Errors:** `403` if not owner, `404` if not found.

---

### `DELETE /api/posts/{id}`
Delete a post (owner or admin). Requires auth.

**Response 204:** no body.

---

### `POST /api/posts/{id}/like`
Like a post. Requires auth.

**Response 204:** no body.

**Errors:** `409` if already liked.

---

### `DELETE /api/posts/{id}/like`
Unlike a post. Requires auth.

**Response 204:** no body.

---

### `GET /api/posts/{id}/comments`
List comments for a post. Public.

**Query params:** `page`, `size`

**Response 200:** `Page<CommentDto>`
```json
{
  "id": 100,
  "author": { "id": 2, "username": "bob", "avatarUrl": "..." },
  "content": "Nice post!",
  "createdAt": "2026-05-14T09:00:00Z"
}
```

---

### `POST /api/posts/{id}/comments`
Add a comment. Requires auth.

**Request body:**
```json
{ "content": "Thanks for sharing" }
```

**Response 201:** `CommentDto`

---

### `DELETE /api/posts/{id}/comments/{commentId}`
Delete a comment (owner or admin). Requires auth.

**Response 204:** no body.

---

## Reports

### `POST /api/reports`
Report a user. Requires auth.

**Request body:**
```json
{
  "reportedUserId": 5,
  "reason": "Posting spam content"
}
```

**Response 201:**
```json
{
  "id": 1,
  "reportedUserId": 5,
  "reason": "Posting spam content",
  "status": "PENDING",
  "createdAt": "2026-05-14T10:00:00Z"
}
```

**Errors:** `400` if reporting self.

---

## Notifications

### `GET /api/notifications`
List notifications for the authenticated user. Requires auth.

**Query params:** `page`, `size`, `unreadOnly` (boolean)

**Response 200:** `Page<NotificationDto>`
```json
{
  "id": 1,
  "type": "NEW_POST",
  "actor": { "id": 2, "username": "bob", "avatarUrl": "..." },
  "postId": 10,
  "isRead": false,
  "createdAt": "2026-05-14T08:31:00Z"
}
```

`type` values: `NEW_POST`, `NEW_COMMENT`, `NEW_LIKE`, `NEW_SUBSCRIBER`.

---

### `PATCH /api/notifications/{id}/read`
Mark one notification as read. Requires auth.

**Response 204:** no body.

---

### `PATCH /api/notifications/read-all`
Mark all notifications as read. Requires auth.

**Response 204:** no body.

---

## Admin

All admin endpoints require `ROLE_ADMIN`. Unauthorized requests return `403`.

### `GET /api/admin/stats`
Get platform-wide counts. Requires `ROLE_ADMIN`.

**Response 200:**
```json
{
  "totalUsers": 42,
  "totalPosts": 158,
  "totalReports": 10,
  "totalPendingReports": 3
}
```

---

### `GET /api/admin/users`
List all users. Paginated.

**Query params:** `page`, `size`, `search` (matches username/email)

**Response 200:** `Page<AdminUserDto>`
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "role": "USER",
  "banned": false,
  "postCount": 5,
  "reportCount": 0,
  "createdAt": "2026-05-01T10:00:00Z"
}
```

---

### `GET /api/admin/posts`
List all posts. Paginated.

**Response 200:** `Page<PostDto>` with extra fields (`reportCount`).

---

### `GET /api/admin/reports`
List reports. Paginated.

**Query params:** `page`, `size`, `status` (`PENDING` / `REVIEWED` / `DISMISSED`)

**Response 200:** `Page<ReportDto>`
```json
{
  "id": 1,
  "reporter": { "id": 3, "username": "charlie" },
  "reportedUser": { "id": 5, "username": "spammer" },
  "reason": "Posting spam content",
  "status": "PENDING",
  "createdAt": "2026-05-14T10:00:00Z"
}
```

---

### `POST /api/admin/users/{id}/ban`
Ban a user (prevents login).

**Response 204:** no body.

---

### `POST /api/admin/users/{id}/unban`
Unban a user.

**Response 204:** no body.

---

### `DELETE /api/admin/users/{id}`
Permanently delete a user and their content.

**Response 204:** no body.

---

### `DELETE /api/admin/posts/{id}`
Delete any post.

**Response 204:** no body.

---

### `PATCH /api/admin/reports/{id}`
Update report status.

**Request body:**
```json
{ "status": "REVIEWED" }
```

**Response 200:** updated `ReportDto`.

---

## Settings

All settings endpoints require auth.

### `PATCH /api/settings/email`
Change the authenticated user's email.

**Request body:**
```json
{ "email": "newemail@example.com" }
```

**Response 204:** no body.

**Errors:** `409` if email already in use, `400` if invalid format.

---

### `PATCH /api/settings/password`
Change the authenticated user's password.

**Request body:**
```json
{ "currentPassword": "old123", "newPassword": "new456" }
```

**Response 204:** no body.

**Errors:** `401` if current password is wrong.

---

### `PATCH /api/settings/avatar`
Upload a new avatar. **Content-Type: `multipart/form-data`**.

**Form fields:**
- `avatar` (file — image/jpeg, image/png)

**Response 204:** no body.

**Errors:** `400` if format invalid or size exceeded.

---

### `DELETE /api/settings/account`
Permanently delete the authenticated user's account and all their content.

**Response 204:** no body.

---

## Media

### `GET /api/media/{filename}`
Serve an uploaded media file. Public.

**Response 200:** binary content with appropriate `Content-Type` (image/*).

**Errors:** `404` if not found.

---

## Authorization Summary

| Endpoint pattern | Required role |
| --- | --- |
| `/api/auth/register`, `/api/auth/login` | public |
| `/api/users/{id}`, `/api/users/{id}/posts`, `/api/users/{id}/subscribers`, `/api/users/{id}/subscriptions` | public |
| `/api/posts/{id}`, `/api/posts/{id}/comments` (GET) | public |
| `/api/posts/feed` (GET) | public (preview) / USER or ADMIN (personalized) |
| `/api/media/**` | public |
| all writes on posts/comments/likes | USER or ADMIN |
| `/api/users/me`, `/api/users/{id}/subscribe` | USER or ADMIN |
| `/api/reports`, `/api/notifications/**` | USER or ADMIN |
| `/api/settings` | USER or ADMIN |
| `/api/admin/**` | ADMIN |