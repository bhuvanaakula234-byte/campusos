CampusOS Backend (Spring Boot)

Quick start:

1. Ensure Java 17 and Maven are installed.
2. Configure PostgreSQL and set environment variables (JDBC_DATABASE_URL, JDBC_DATABASE_USERNAME, JDBC_DATABASE_PASSWORD).
3. Build: mvn clean package -DskipTests
4. Run: java -jar target/campusos-backend-0.1.0.jar

Development:
- Run with: mvn spring-boot:run
- Application config in src/main/resources/application.yml

Docker:
- Build jar and use provided Dockerfile for containerization.

Quick API auth check:
- Ensure APP_JWT_SECRET env var is set to a strong secret (HS256 key, at least 32 characters). Example (bash):
  export APP_JWT_SECRET="a-very-long-secret-key-please-change"
- Start the application
- Seeded data: an admin user is created at startup if not present:
  email: admin@campusos.local
  password: adminpass
  (Change immediately in production)

Endpoints:
- GET /api/health  -- public health
- POST /api/auth/register  -- body: { "email": "user@example.com", "password": "pass123", "fullName": "User Name" }
- POST /api/auth/login     -- body: { "email": "user@example.com", "password": "pass123" } returns { token }
- GET /api/admin/ping      -- protected, requires Authorization: Bearer <token> with ADMIN authority

Notes:
- The project seeds roles (STUDENT, FACULTY, HOD, PRINCIPAL, PLACEMENT, ADMIN) on startup.
- The JWT secret must be at least 32 bytes for HS256; replace the default placeholder before production.

OpenAI (StudyBuzz)
- To enable real LLM responses, set the OpenAI API key in environment variable openai.api.key (or OPENAI_API_KEY if you prefer). The service reads property openai.api.key and openai.model (default gpt-3.5-turbo).
- When configured, StudyBuzz will call the OpenAI Chat Completions API for chat and PDF summarization. Example environment variable (bash):
  export openai.api.key="sk-..."

