# Migration Guide: Node.js to Spring Boot

This document outlines the migration from the Node.js/Express backend to Spring Boot.

## Project Structure

The new Spring Boot backend is located in `stickerswebsite-backend/` directory, separate from the frontend project.

## Key Changes

### 1. Technology Stack
- **Before**: Node.js + Express + PostgreSQL (pg driver)
- **After**: Java 21 + Spring Boot 3.3.5 + PostgreSQL (JPA/Hibernate)

### 2. API Endpoints

All API endpoints remain the same for backward compatibility:

#### Authentication
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/auth/verify`

#### Templates
- `GET /api/templates`
- `GET /api/templates/{id}`
- `GET /api/templates/{identifier}/stickers`
- `GET /api/templates/categories`
- `GET /api/templates/search?q=query`

#### Custom Stickers
- `POST /api/custom-stickers`
- `GET /api/custom-stickers/published`
- `GET /api/custom-stickers/my-stickers/{userId}`
- `PATCH /api/custom-stickers/{id}/publish`
- `DELETE /api/custom-stickers/{id}?user_id=userId`

### 3. Response Formats

Response formats have been maintained to match the original Node.js backend:
- Templates include `category_name` and `category_id` fields
- Stickers include `template_title` field
- Error responses follow the same structure

### 4. Database

The same PostgreSQL database is used. The Spring Boot application will:
- Automatically create tables if they don't exist (via `spring.jpa.hibernate.ddl-auto=update`)
- Seed initial data on first run (via `DatabaseInitializer`)

### 5. Configuration

**Environment Variables:**
- `DB_USER` - PostgreSQL username (default: postgres)
- `DB_PASSWORD` - PostgreSQL password
- `JWT_SECRET` - JWT secret key for token generation

**Database:**
- Database name: `stickersdb`
- Port: `5432` (default PostgreSQL port)

**Server:**
- Port: `3001` (same as Node.js backend)
- CORS configured for: `localhost:5173`, `localhost:5174`, `localhost:3000`

## Running the Application

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+ running with `stickersdb` database

### Steps

1. **Navigate to backend directory:**
   ```bash
   cd stickerswebsite-backend
   ```

2. **Configure database** (if needed):
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Set JWT secret** (recommended for production):
   ```bash
   export JWT_SECRET=your-secret-key-change-in-production
   ```

4. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   Or use your IDE to run `StickersApplication.java`

### Frontend Integration

The frontend doesn't need any changes! The Vite proxy configuration already points to `http://localhost:3001`, which is where the Spring Boot backend runs.

## Development Notes

### Hot Reload
Spring Boot DevTools is included for automatic application restart during development.

### Database Initialization
On first run, the application will:
1. Create all necessary tables
2. Seed categories
3. Seed templates
4. Seed stickers with finishes

### Testing
To test the backend independently:
```bash
# Health check
curl http://localhost:3001/health

# Get templates
curl http://localhost:3001/api/templates
```

## Differences from Node.js Backend

1. **Type Safety**: Java provides compile-time type checking
2. **ORM**: Uses JPA/Hibernate instead of raw SQL queries
3. **Dependency Injection**: Spring's IoC container manages dependencies
4. **Validation**: Uses Jakarta Bean Validation annotations
5. **Security**: Spring Security provides password encoding and CORS configuration

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running
- Check database credentials in `application.properties`
- Verify database `stickersdb` exists

### Port Already in Use
- Stop the Node.js backend if it's still running
- Or change the port in `application.properties`: `server.port=3002`

### Build Errors
- Ensure Java 21 is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Clean and rebuild: `mvn clean install`

## Production Deployment

For production:
1. Set a strong `JWT_SECRET` environment variable
2. Update CORS origins in `SecurityConfig.java` and `WebConfig.java`
3. Set `spring.jpa.hibernate.ddl-auto=validate` or `none` in production
4. Use environment variables or externalized configuration for database credentials
5. Consider using Spring Profiles for different environments

