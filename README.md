# Stickers Backend API

Spring Boot backend application for the Stickers Website, built with Spring Boot 3.3.5 and Java 21.

## Features

- RESTful API endpoints for authentication, templates, and custom stickers
- PostgreSQL database integration
- JWT-based authentication
- CORS configuration for frontend integration
- Automatic database initialization with seed data

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+ (or use Docker)

## Setup

1. **Clone and navigate to the project:**
   ```bash
   cd stickerswebsite-backend
   ```

2. **Configure PostgreSQL:**
   - Create a database named `stickersdb`
   - Update `src/main/resources/application.properties` with your database credentials:
     ```properties
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. **Set JWT Secret (optional):**
   - Create a `.env` file or set environment variable:
     ```bash
     export JWT_SECRET=your-secret-key-change-in-production
     ```

4. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   Or use your IDE to run `StickersApplication.java`

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/verify` - Verify JWT token

### Templates
- `GET /api/templates` - Get all templates (optional query params: `category`, `trending`)
- `GET /api/templates/{id}` - Get template by ID
- `GET /api/templates/{identifier}/stickers` - Get stickers by template ID or title
- `GET /api/templates/categories` - Get all categories
- `GET /api/templates/search?q=query` - Search templates and stickers

### Custom Stickers
- `POST /api/custom-stickers` - Create a custom sticker
- `GET /api/custom-stickers/published` - Get all published stickers
- `GET /api/custom-stickers/my-stickers/{userId}` - Get user's stickers
- `PATCH /api/custom-stickers/{id}/publish` - Update publish status
- `DELETE /api/custom-stickers/{id}?user_id=userId` - Delete sticker

### Health Check
- `GET /health` - Server health check

## Configuration

The application runs on port **3001** by default (configurable in `application.properties`).

## Database Schema

The application automatically creates the following tables:
- `users` - User accounts
- `categories` - Sticker categories
- `templates` - Sticker templates
- `stickers` - Stickers associated with templates
- `user_created_stickers` - User-created custom stickers

## Development

The application uses Spring Boot DevTools for hot reloading during development.

## Production

For production deployment:
1. Set a strong `JWT_SECRET` environment variable
2. Configure proper database credentials
3. Update CORS origins in `SecurityConfig.java` and `WebConfig.java`
4. Set `spring.jpa.hibernate.ddl-auto=validate` or `none` in production

