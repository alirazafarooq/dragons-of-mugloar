# Dragons of Mugloar

A full-stack application for playing and analyzing the **Dragons of Mugloar** strategy game. The application integrates the external game API with a Spring Boot backend and a Vue 3 frontend.

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Maven
- Spring `RestClient` for external API integration
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
└── frontend/    # Vue 3 frontend, game dashboard, strategy and UI tests
```

## Prerequisites

- JDK 21
- Node.js and npm
- Git
- Internet connection to access the external game API

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

- Start a new game and resume a saved game.
- Retrieve available missions from the game API.
- Decode encoded mission descriptions and probability labels in the backend.
- Solve missions and update the game state.
- Display game statistics, including score, gold, lives, level and turn.
- Retrieve available shop items.
- Purchase shop items and update the game state.
- Handle API errors, purchase failures and insufficient gold.
- Handle game-over states and clear unavailable missions and shop items.
- Persist the game state in browser local storage.
- Display mission outcomes and game information in the frontend.
- Validate relevant game, mission and shop identifiers.
- Test backend API integration, mission decoding, frontend services, components, composables and strategy decisions.

## Mission Decoding

Some mission fields returned by the external API are encoded. The backend decodes the mission description and probability label before sending the mission to the frontend.

Currently supported encodings:

- **Base64 (`encrypted: "1"`):** decodes the mission description and probability label.
- **ROT13 (`encrypted: "2"`):** transforms the mission description and probability label using ROT13.

The frontend displays the decoded fields supplied by the backend. The mission identifier is also decoded by the current implementation to support the mission-solving flow.

## Mission Selection Strategy

The initial strategy implementation is located in:

`frontend/src/services/missionStrategy.ts`

The `chooseAutoplayAction` function evaluates the current game state, available missions and shop items. It returns one of three decisions:

- **Stop:** when no lives remain or no missions are available.
- **Buy:** when one life remains and a healing potion is available and affordable.
- **Solve:** select the best available mission based on the current heuristic risk score.

The strategy prioritizes lower-risk missions and uses the reward as a tie-breaker. It continues selecting a mission even when all available missions are considered risky.

Reaching 1,000 points is not a stopping condition in the strategy. The intended autoplay behavior is to continue beyond that milestone until the user stops autoplay or the game runs out of lives.

**Important:** The strategy decision function is implemented and tested, but the autoplay controller and its Start, Pause and Take Manual Control UI are still pending. Therefore, automated play is not yet fully implemented.

## Risk Assumptions and External Research

The current strategy uses probability labels and reward thresholds as heuristic indicators of mission risk. These are experimental assumptions, not official game rules or guaranteed success probabilities.

The initial mapping treats labels such as `Sure thing` and `Piece of cake` as lower risk than labels such as `Gamble`, `Playing with fire`, `Impossible` and `Suicide mission`.

The strategy also treats unusually high rewards paired with apparently safe labels as suspicious:

- `Sure thing` with a reward of **130 or more** receives a higher risk score.
- `Piece of cake` with a reward of **150 or more** receives a higher risk score.

These thresholds were inspired by an independently developed Dragons of Mugloar project that reports experimental mission outcomes:

- [jekost/dragons — GitHub](https://github.com/jekost/dragons)

That project's reported experiments suggest that some apparently safe probability labels may be less reliable at unusually high reward values. These findings provide a hypothesis for the current strategy; they are **not official API documentation**, and the results have not yet been independently validated in this application.

Consequently:

- Mission probability labels are treated as qualitative indicators rather than exact probabilities.
- Unknown labels receive a default risk score.
- Reward alone does not determine mission selection.
- The strategy does not assume that shop upgrades improve mission success without evidence.
- The healing-potion decision is a provisional rule that must be checked against actual purchase results and game-state changes.

The risk mapping and thresholds should be refined using recorded outcomes from multiple games. No particular score or success rate is guaranteed by the current strategy.

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

The mission strategy has tests for score milestones, game-over handling, healing decisions, suspicious high-reward missions and selection when all available missions are risky.

## Project Status

- [x] Development environment
- [x] Vue-to-Spring Boot connection
- [x] External game API integration
- [x] Game creation and mission retrieval
- [x] Mission decoding
- [x] Mission solving
- [x] Game dashboard and state display
- [x] Shop integration and item purchasing
- [x] Error handling and game-over handling
- [x] Game-state persistence
- [x] Backend and frontend automated tests
- [x] Frontend production build
- [x] Initial mission-selection strategy
- [x] Unit tests for mission-selection decisions
- [ ] Autoplay controller integrated with game actions
- [ ] Start, Pause and Take Manual Control UI
- [ ] Safe handling of failed purchases and unconfirmed actions during autoplay
- [ ] Validate strategy performance over multiple complete games
- [ ] Consistently meet the 1,000-point acceptance goal

## Future Improvements

- Integrate the strategy with the existing game-state composable.
- Add autoplay controls while preserving manual gameplay.
- Stop autoplay safely on zero lives, unrecoverable errors or user intervention.
- Prevent repeated purchases and infinite retry loops when actions fail.
- Record decisions and outcomes to evaluate mission-selection heuristics.
- Improve the strategy using observed results rather than reward alone.
