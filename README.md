# Dragons of Mugloar

A full-stack web application for playing the Dragons of Mugloar strategy game. It integrates the external game API with a Spring Boot backend and a Vue 3 frontend, providing manual gameplay, shop interactions, game-state persistence, and automated mission selection.

The project demonstrates REST API integration, frontend state management, automated testing, error handling, and a heuristic-based autoplay strategy that can be extended as more information about the game becomes available.

**Repository:** [https://github.com/alirazafarooq/dragons-of-mugloar](https://github.com/alirazafarooq/dragons-of-mugloar)

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Mission Decoding](#mission-decoding)
- [Autoplay and Mission Strategy](#autoplay-and-mission-strategy)
- [Risk Assumptions](#risk-assumptions)
- [Testing and Test Coverage](#testing-and-test-coverage)
- [Known Limitations](#known-limitations)
- [Future Improvements](#future-improvements)

## Features

- Start a new game and resume a saved game.
- Retrieve available missions from the external game API.
- Decode encoded mission descriptions and probability labels.
- Solve missions and display their outcomes.
- Display score, high score, gold, lives, level, and turn count.
- Retrieve shop items and purchase products.
- Automatically select missions using a heuristic risk-scoring strategy.
- Automatically purchase healing potions when the strategy's conditions are met.
- Start, pause, and stop autoplay to return to manual control.
- Handle API errors, failed purchases, and insufficient gold.
- Handle game-over states and unavailable missions.
- Persist game state in browser local storage.
- Validate relevant game, mission, and shop identifiers.
- Test backend integration, frontend services, components, state management, and strategy decisions.

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Maven
- Spring `RestClient`
- REST API
- OpenAPI / Swagger UI
- JUnit 5
- Mockito

### Frontend

- Vue 3
- TypeScript
- Vite
- Axios
- Vitest
- Vue Test Utils

## Project Structure

```text
dragons-of-mugloar/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── composables/
│   │   ├── services/
│   │   ├── types/
│   │   ├── App.vue
│   │   ├── main.ts
│   │   └── style.css
│   ├── .env.sample
│   └── package.json
├── .gitignore
└── README.md
```

The backend handles external API communication, mission decoding, and game operations. The frontend provides the game interface, state management, mission selection, shop interactions, and autoplay controls.

## Prerequisites

- JDK 21
- Node.js and npm
- Git
- Internet access to communicate with the external game API

## Running the Application

### 1. Clone the repository

```bash
git clone https://github.com/alirazafarooq/dragons-of-mugloar.git
cd dragons-of-mugloar
```

### 2. Start the backend

Open a terminal in the project root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs at:

`http://localhost:8080`

### 3. Configure and start the frontend

Open a **second terminal** in the project root.

Navigate to the frontend directory:

```powershell
cd frontend
```

Install the frontend dependencies:

```powershell
npm.cmd install
```

#### Configure environment variables

The frontend uses `VITE_API_BASE_URL` to configure the backend API base URL.

A sample environment configuration is provided in `frontend/.env.sample`.

Create a `.env.development` file from the sample file.

**Windows PowerShell:**

```powershell
Copy-Item .env.sample .env.development
```

**Windows Command Prompt (CMD):**

```cmd
copy .env.sample .env.development
```

Open `frontend/.env.development` and ensure it contains:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

This configures the frontend to send API requests to the local Spring Boot backend.

Start the frontend development server:

```powershell
npm.cmd run dev
```

Open the application at:

`http://localhost:5173`

**Environment configuration notes:**

- Run the frontend commands from the `frontend` directory.
- Vite automatically loads `.env.development` when running the development server with `npm run dev`.
- Restart the Vite development server after changing environment variables.
- Keep `.env.sample` in version control so other developers can use it as a template.
- Keep `.env.development` out of version control if it is intended for local configuration. Check `.gitignore` to ensure it is excluded.
- Environment variables prefixed with `VITE_` are exposed to frontend code. Do not store secrets or private credentials in them.

The frontend communicates with the backend through the `/api` proxy configured in Vite. Ensure the Vite proxy configuration and `VITE_API_BASE_URL` are consistent with the frontend API service implementation.

### Backend Configuration

The external API base URL and frontend URL are configured in the backend's `application.properties` file:

```properties
dragons.api.base-url=https://dragonsofmugloar.com/api/v2
app.frontend.url=http://localhost:5173
```

The backend uses the external API base URL to communicate with the Dragons of Mugloar game service.

## API Documentation

When the backend is running, open Swagger UI:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

The backend provides endpoints for creating games, retrieving missions, solving missions, retrieving shop items, and purchasing items.

## Mission Decoding

Some mission fields returned by the external game API are encoded. The backend decodes mission descriptions and probability labels before returning them to the frontend.

Supported encodings include:

- **Base64 (`encrypted: "1"`):** Decodes encoded mission data.
- **ROT13 (`encrypted: "2"`):** Transforms encoded mission text using ROT13.

The current implementation also decodes the mission identifier where required by the mission-solving flow. The frontend displays the decoded data supplied by the backend.

## Autoplay and Mission Strategy

The autoplay controller is integrated with the game-state composable and frontend controls.

The interface provides:

- **Start autoplay:** Begins automated gameplay.
- **Pause:** Stops autoplay from scheduling additional actions.
- **Manual control:** Stops automation so the player can take over.

Autoplay uses the strategy implemented in:

`frontend/src/services/missionStrategy.ts`

The strategy evaluates the current game state, available missions, and shop items. It returns one of three action types:

- **Stop:** Stops when no lives remain or no missions are available.
- **Buy:** Purchases an affordable healing potion when the healing conditions are met.
- **Solve:** Selects the best-ranked available mission.

After an action, the controller refreshes game-related data and checks the result before continuing. It stops when an action fails, the resulting state cannot be verified, or another stopping condition is reached.

### How Mission Selection Works

The `chooseAutoplayAction` function uses a heuristic risk score to rank available missions.

The current probability-label mapping is:

| Probability label | Risk score | Interpretation |
|---|---:|---|
| Sure thing | 1/10 | Very low estimated risk |
| Piece of cake | 2/10 | Low estimated risk |
| Walk in the park | 2/10 | Low estimated risk |
| Quite likely | 3/10 | Relatively low estimated risk |
| Likely | 3/10 | Relatively low estimated risk |
| Gamble | 5/10 | Uncertain outcome |
| Playing with fire | 7/10 | High estimated risk |
| Risky | 7/10 | High estimated risk |
| Impossible | 10/10 | Extremely high estimated risk |
| Suicide mission | 10/10 | Extremely high estimated risk |
| Unknown label | 5/10 | Default risk assumption |

The strategy sorts missions by ascending risk score. If multiple missions have the same risk score, it prefers the mission with the higher reward.

The strategy also applies reward-based adjustments:

- A `Sure thing` mission with a reward of 130 or more receives a minimum risk score of 9/10.
- A `Piece of cake` mission with a reward of 150 or more receives a minimum risk score of 8/10.

These adjustments are intended to account for the possibility that unusually high rewards may indicate greater risk than the probability label suggests.

If all available missions are risky, the strategy still selects the best-ranked mission instead of refusing to play.

### How Healing Purchases Work

The current strategy considers buying a healing potion when:

- The game has exactly one remaining life.
- The player has enough gold to afford the potion.
- A healing potion is present in the available shop items.

The current implementation prioritizes the healing purchase when these conditions are met. It does not yet establish whether purchasing other shop products improves mission success rates.

Autoplay does not stop simply because it reaches 1,000 points. It continues until the user intervenes or a stopping condition is reached. Repeated healing purchases may consume gold without guaranteeing a successful mission, so this behavior remains an area for future improvement.

### Score Objective

The project has reached more than 1,000 points during gameplay testing, with a reported run reaching approximately 3,000 points.

These are observed results, not a guarantee that every game will achieve the same score. The strategy does not use 1,000 points as its stopping condition.

## Risk Assumptions

The risk model is a heuristic rather than a verified probability model. Risk scores rank missions for selection; they do not directly calculate the probability of success.

For example, the strategy may assume that a `Walk in the park` mission is a relatively safe choice. A success rate such as 98% can be used as an illustrative working assumption when discussing the strategy, but it is not a measured probability supplied by the game API or calculated by the current code.

The distinction is important:

- **Risk score:** A value from 1 to 10 used by the current implementation to rank missions.
- **Assumed success rate:** A hypothetical percentage that may help describe the intended interpretation of a probability label.
- **Observed success rate:** A percentage calculated from actual recorded mission outcomes.

The current implementation uses the first of these. Illustrative percentages should not be interpreted as measured or guaranteed success rates.

### Shop-Product Assumptions

Healing potions are currently used as the automatic recovery option under the conditions described above.

The relationship between other shop products and mission outcomes has not yet been established. Products such as claw upgrades, armor, fuel, and other upgrades remain available through the shop integration, but the current strategy does not assume that they improve the success rate of particular mission types.

The existing shop integration is retained so that the strategy can be expanded later without redesigning the application.

## Testing and Test Coverage

The project includes automated tests for backend behavior, frontend components, API services, state management, and mission-selection decisions.

### Backend Tests

Run from the project root:

```powershell
cd backend
.\mvnw.cmd test
```

Backend tests cover areas including:

- External game API client behavior.
- Mission decoding.
- Mission retrieval and solving.
- Shop-item retrieval and purchases.
- Controller endpoints and request validation.
- Error handling and API integration behavior.

JUnit 5 and Mockito are used for backend testing.

### Frontend Tests

Run from the project root:

```powershell
cd frontend
npm.cmd run test
```

Frontend tests cover areas including:

- API service calls.
- Mission and shop components.
- Game dashboard rendering.
- Game-state composable behavior.
- Shop purchasing behavior.
- Mission-selection strategy decisions.
- Game-over handling and healing decisions.

Vitest and Vue Test Utils are used for frontend testing.

## Known Limitations

- Mission risk scores are heuristic estimates, not verified probabilities.
- Reward-based risk thresholds are assumptions that may need adjustment.
- Autoplay can spend significant gold repeatedly on healing potions.
- The effectiveness of non-healing shop products has not yet been established.
- A successful build and passing unit tests do not guarantee a high score in every complete game.
- Numerical code-coverage percentages have not been measured.
- The reported 3,000-point run is an observed result, not a guaranteed outcome.

## Future Improvements

- Refine risk scores and reward thresholds using observed mission outcomes.
- Investigate whether probability labels and reward values reliably indicate mission success.
- Explore relationships between shop products and the outcomes of different mission types.
- Improve healing and gold-management decisions to reduce unnecessary spending.
- Extend the strategy as additional useful relationships between missions and products are identified.
- Add further automated tests as the strategy becomes more sophisticated.
- Introduce numerical code-coverage reporting.

The current autoplay implementation remains the baseline. Future strategy changes should be guided by observed game behavior rather than assuming that unverified probabilities or shop-product relationships are established facts.
