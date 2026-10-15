
<script setup lang="ts">
import { onMounted, ref } from 'vue'

interface GameState {
  gameId: string
  lives: number
  gold: number
  level: number
  score: number
  highScore: number
  turn: number
}

interface Mission {
  adId: string
  message: string
  reward: number
  expiresIn: number
  encrypted: string | null
  probability: string
}

const API_BASE_URL = 'http://localhost:8080/api'
const STORAGE_KEY = 'mugloar-game'

const game = ref<GameState | null>(null)
const loading = ref(false)
const error = ref('')

const missions = ref<Mission[]>([])
const missionsLoading = ref(false)
const missionsError = ref('')

// Check that a value is a JSON object.
function isRecord(value: unknown): value is Record<string, unknown> {
  return (
      typeof value === 'object' &&
      value !== null &&
      !Array.isArray(value)
  )
}

// Validate game data received from an API or localStorage.
function isGameState(value: unknown): value is GameState {
  if (!isRecord(value)) {
    return false
  }

  return (
      typeof value.gameId === 'string' &&
      value.gameId.length > 0 &&
      typeof value.lives === 'number' &&
      Number.isFinite(value.lives) &&
      typeof value.gold === 'number' &&
      Number.isFinite(value.gold) &&
      typeof value.level === 'number' &&
      Number.isFinite(value.level) &&
      typeof value.score === 'number' &&
      Number.isFinite(value.score) &&
      typeof value.highScore === 'number' &&
      Number.isFinite(value.highScore) &&
      typeof value.turn === 'number' &&
      Number.isFinite(value.turn)
  )
}

// Validate every mission received from the API.
function isMission(value: unknown): value is Mission {
  if (!isRecord(value)) {
    return false
  }

  return (
      typeof value.adId === 'string' &&
      value.adId.length > 0 &&
      typeof value.message === 'string' &&
      typeof value.reward === 'number' &&
      Number.isFinite(value.reward) &&
      typeof value.expiresIn === 'number' &&
      Number.isFinite(value.expiresIn) &&
      (typeof value.encrypted === 'string' ||
          value.encrypted === null) &&
      typeof value.probability === 'string'
  )
}

// Read the structured error message returned by Spring Boot.
async function getApiErrorMessage(response: Response): Promise<string> {
  const fallback =
      `Request failed with HTTP ${response.status}` +
      (response.statusText ? ` (${response.statusText})` : '')

  try {
    const body: unknown = await response.json()

    if (
        isRecord(body) &&
        typeof body.message === 'string' &&
        body.message.trim().length > 0
    ) {
      return `${fallback}: ${body.message}`
    }
  } catch {
    // The response wasn't JSON. Use the fallback message.
  }

  return fallback
}

// Restore the saved game after a browser refresh.
onMounted(() => {
  const savedGame = localStorage.getItem(STORAGE_KEY)

  if (!savedGame) {
    return
  }

  try {
    const parsed: unknown = JSON.parse(savedGame)

    if (isGameState(parsed)) {
      game.value = parsed
      void loadMissions(parsed.gameId)
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
})

// Start a new game and save its state.
async function startGame() {
  loading.value = true
  error.value = ''

  try {
    const response = await fetch(`${API_BASE_URL}/games`, {
      method: 'POST',
    })

    if (!response.ok) {
      throw new Error(await getApiErrorMessage(response))
    }

    const data: unknown = await response.json()

    if (!isGameState(data)) {
      throw new Error(
          'The backend returned an invalid game state.'
      )
    }

    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    game.value = data

    // Automatically load missions for this game.
    await loadMissions(data.gameId)
  } catch (e) {
    error.value = e instanceof Error
        ? e.message
        : 'An unexpected error occurred while starting the game.'

    console.error('Start game failed:', e)
  } finally {
    loading.value = false
  }
}

// Retrieve and validate missions through our backend.
async function loadMissions(gameId: string) {
  missionsLoading.value = true
  missionsError.value = ''
  missions.value = []

  try {
    const response = await fetch(
        `${API_BASE_URL}/games/${encodeURIComponent(gameId)}/messages`
    )

    if (!response.ok) {
      throw new Error(await getApiErrorMessage(response))
    }

    const data: unknown = await response.json()

    if (!Array.isArray(data) || !data.every(isMission)) {
      throw new Error(
          'The backend returned invalid mission data.'
      )
    }

    missions.value = data
  } catch (e) {
    missionsError.value = e instanceof Error
        ? e.message
        : 'An unexpected error occurred while loading missions.'

    console.error('Loading missions failed:', e)
  } finally {
    missionsLoading.value = false
  }
}

// Retry only when loading missions fails.
function retryMissions() {
  if (game.value && !missionsLoading.value) {
    void loadMissions(game.value.gameId)
  }
}

// End the local session.
function endGame() {
  localStorage.removeItem(STORAGE_KEY)

  game.value = null
  missions.value = []
  error.value = ''
  missionsError.value = ''
}
</script>

<template>
  <main class="game-page">
    <header class="hero">
      <p class="eyebrow">A DRAGON-SIZED CHALLENGE</p>

      <h1>Dragons of <span>Mugloar</span></h1>

      <p class="subtitle">
        Brave the kingdom, complete missions, and build your legend.
      </p>

      <button
          class="start-button"
          :disabled="loading"
          @click="startGame"
      >
        {{ loading ? 'Starting adventure...' : 'Start New Game' }}
      </button>
    </header>

    <section v-if="error" class="error-message" role="alert">
      {{ error }}
    </section>

    <section v-if="game" class="dashboard">
      <div class="game-heading">
        <div>
          <p class="eyebrow">YOUR ADVENTURE</p>
          <h2>Game Dashboard</h2>
        </div>

        <span class="status">In progress</span>
      </div>

      <div class="stats-grid">
        <article class="stat-card">
          <span>❤️ Lives</span>
          <strong>{{ game.lives }}</strong>
        </article>

        <article class="stat-card">
          <span>🪙 Gold</span>
          <strong>{{ game.gold }}</strong>
        </article>

        <article class="stat-card">
          <span>🏆 Score</span>
          <strong>{{ game.score }}</strong>
        </article>

        <article class="stat-card">
          <span>📈 High Score</span>
          <strong>{{ game.highScore }}</strong>
        </article>

        <article class="stat-card">
          <span>🐉 Level</span>
          <strong>{{ game.level }}</strong>
        </article>

        <article class="stat-card">
          <span>⚔️ Turn</span>
          <strong>{{ game.turn }}</strong>
        </article>
      </div>

      <p class="game-id">Game ID: {{ game.gameId }}</p>

      <section class="missions-section">
        <div class="missions-heading">
          <div>
            <p class="eyebrow">CHOOSE YOUR CHALLENGE</p>
            <h2>Available Missions</h2>
          </div>
        </div>

        <p
            v-if="missionsLoading"
            class="mission-message-state"
            role="status"
        >
          Looking for available missions...
        </p>

        <div
            v-else-if="missionsError"
            class="error-message"
            role="alert"
        >
          <p>Could not load missions: {{ missionsError }}</p>

          <button
              class="start-button"
              :disabled="missionsLoading"
              @click="retryMissions"
          >
            Try Again
          </button>
        </div>

        <p
            v-else-if="missions.length === 0"
            class="mission-message-state"
        >
          No missions are currently available.
        </p>

        <div v-else class="mission-grid">
          <article
              v-for="mission in missions"
              :key="mission.adId"
              class="mission-card"
          >
            <div class="mission-card-heading">
              <span class="mission-label">MISSION</span>

              <span
                  v-if="mission.encrypted"
                  class="mission-encrypted"
              >
                Encrypted
              </span>
            </div>

            <p class="mission-description">
              {{ mission.message }}
            </p>

            <div class="mission-details">
              <div>
                <span>Gold reward</span>
                <strong class="mission-reward">
                  🪙 {{ mission.reward }}
                </strong>
              </div>

              <div>
                <span>Expires in</span>
                <strong>
                  {{ mission.expiresIn }}
                  {{ mission.expiresIn === 1 ? 'turn' : 'turns' }}
                </strong>
              </div>
            </div>

            <div class="mission-probability">
              <span>Estimated probability</span>
              <strong>{{ mission.probability }}</strong>
            </div>

            <p class="mission-id">
              Mission ID: {{ mission.adId }}
            </p>
          </article>
        </div>
      </section>

      <button
          class="start-button end-game-button"
          :disabled="loading"
          @click="endGame"
      >
        End Game
      </button>
    </section>
  </main>
</template>
