# Dragons of Mugloar

A full-stack application for playing and analyzing the Dragons of Mugloar strategy game. The application integrates the external game API with a Spring Boot backend and a Vue 3 frontend.

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Maven
- Spring REST client for external API integration
- REST API
- OpenAPI / Swagger UI
- JUnit 5 and Mockito

### Frontend
- Vue 3
- TypeScript
- Vite
- Axios
- Pinia
- Vitest and Vue Test Utils

## Project Structure

```text
dragons-of-mugloar/
├── backend/     # Spring Boot backend, game API integration and tests
└── frontend/    # Vue 3 frontend, game dashboard and UI tests
```

## Prerequisites

- JDK 21
- Node.js and npm
- Git

## Running the Application

### 1. Start the Backend

Open a terminal at the project root and run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend base URL: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

### 2. Start the Frontend

Open a second terminal at the project root and run:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Frontend URL: http://localhost:5173

The frontend communicates with the backend through the `/api` proxy configured in Vite.

## Implemented Features

- Start a new game.
- Retrieve available missions from the game API.
- Solve missions and update the game state.
- Display game statistics, including score, gold, lives, level and turn.
- Retrieve available shop items.
- Purchase shop items and update the game state.
- Handle purchase failures and insufficient gold.
- Handle game-over states and clear unavailable missions and shop items.
- Persist the game state in browser local storage.
- Display mission outcomes and game information in the frontend.

## Running Tests

### Backend Tests

From the project root:

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend Tests

From the project root:

```powershell
cd frontend
npm.cmd run test
```

### Frontend Production Build

From the project root:

```powershell
cd frontend
npm.cmd run build
```

## Project Status

- [x] Development environment
- [x] Vue-to-Spring Boot connection
- [x] External game API integration
- [x] Game creation and mission retrieval
- [x] Mission solving
- [x] Game dashboard and state display
- [x] Shop integration and item purchasing
- [x] Error handling and game-over handling
- [x] Backend and frontend automated tests
- [x] Frontend production build
- [ ] Mission-selection strategy
- [ ] Automated play
- [ ] Reliably achieve the 1,000-point acceptance goal
