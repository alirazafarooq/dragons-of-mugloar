
# Dragons of Mugloar

A full-stack application for playing and analyzing the
Dragons of Mugloar strategy game.

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Maven
- REST API
- OpenAPI / Swagger UI
- JUnit 5 and Mockito

### Frontend
- Vue 3
- TypeScript
- Vite
- Axios
- Pinia

## Project Structure

- `backend/` — game API integration, business logic,
  strategy engine, and backend tests
- `frontend/` — dashboard and user interface

## Prerequisites

- JDK 21
- Node.js and npm
- Git

## Running the Backend

From the project root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend base URL: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

## Running the Frontend

Open a second terminal at the project root:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Frontend URL: http://localhost:5173

## Running Backend Tests

From the project root:

```powershell
cd backend
.\mvnw.cmd test
```

## Project Status

- [x] Development environment
- [x] Vue-to-Spring Boot connection
- [ ] Game API integration
- [ ] Game dashboard
- [ ] Mission strategy
- [ ] Automated play
- [ ] Comprehensive test coverage
