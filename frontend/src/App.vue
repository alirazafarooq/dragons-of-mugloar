
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

const STORAGE_KEY = 'mugloar-game'

const game = ref<GameState | null>(null)
const loading = ref(false)
const error = ref('')

const missions = ref<Mission[]>([])
const missionsLoading = ref(false)
const missionsError = ref('')

// Restore the saved game after a browser refresh.
onMounted(() => {
  const savedGame = localStorage.getItem(STORAGE_KEY)

  if (!savedGame) {
    return
  }

  try {
    const parsed: unknown = JSON.parse(savedGame)

    if (
        typeof parsed === 'object' &&
        parsed !== null &&
        'gameId' in parsed &&
        typeof parsed.gameId === 'string' &&
        parsed.gameId.length > 0 &&
        'lives' in parsed &&
        typeof parsed.lives === 'number' &&
        'gold' in parsed &&
        typeof parsed.gold === 'number'
    ) {
      game.value = parsed as GameState
      void loadMissions(game.value.gameId)
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
    const response = await fetch('http://localhost:8080/api/games', {
      method: 'POST',
    })

    if (!response.ok) {
      throw new Error(`Server returned ${response.status}`)
    }

    const newGame = await response.json() as GameState

    game.value = newGame

    localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(newGame)
    )

    // Load missions for the newly started game.
    await loadMissions(newGame.gameId)
  } catch (e) {
    error.value = e instanceof Error
        ? `${e.name}: ${e.message}`
        : String(e)

    console.error('Start game failed:', e)
  } finally {
    loading.value = false
  }
}

// Retrieve missions through our Spring Boot backend.
async function loadMissions(gameId: string) {
  missionsLoading.value = true
  missionsError.value = ''
  missions.value = []

  try {
    const response = await fetch(
        `http://localhost:8080/api/games/${encodeURIComponent(gameId)}/messages`
    )

    if (!response.ok) {
      throw new Error(`Server returned ${response.status}`)
    }

    const data: unknown = await response.json()

    if (!Array.isArray(data)) {
      throw new Error('The missions API returned an unexpected response.')
    }

    missions.value = data as Mission[]
  } catch (e) {
    missionsError.value = e instanceof Error
        ? e.message
        : String(e)

    console.error('Loading missions failed:', e)
  } finally {
    missionsLoading.value = false
  }
}

function refreshMissions() {
  if (game.value) {
    void loadMissions(game.value.gameId)
  }
}

// End the local session by removing the saved game.
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

    <section v-if="error" class="error-message">
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

      <!-- Game statistics -->
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

      <!-- Missions -->
      <section class="missions-section">
        <div class="missions-heading">
          <div>
            <p class="eyebrow">CHOOSE YOUR CHALLENGE</p>
            <h2>Available Missions</h2>
          </div>
        </div>

        <p v-if="missionsLoading" class="mission-message-state">
          Looking for available missions...
        </p>

        <div v-else-if="missionsError" class="error-message">
          <p>Could not load missions: {{ missionsError }}</p>

          <button
              class="start-button"
              @click="refreshMissions"
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
              <span v-if="mission.encrypted" class="mission-encrypted">
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
