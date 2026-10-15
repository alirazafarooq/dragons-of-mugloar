
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

const STORAGE_KEY = 'mugloar-game'

const game = ref<GameState | null>(null)
const loading = ref(false)
const error = ref('')

// Restore the previously saved game when the page loads.
onMounted(() => {
  const savedGame = localStorage.getItem(STORAGE_KEY)

  if (!savedGame) {
    return
  }

  try {
    const parsedGame: unknown = JSON.parse(savedGame)

    if (
        typeof parsedGame === 'object' &&
        parsedGame !== null &&
        'gameId' in parsedGame &&
        typeof parsedGame.gameId === 'string' &&
        parsedGame.gameId.length > 0
    ) {
      game.value = parsedGame as GameState
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
})

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

    // Save the new game only after the server responds successfully.
    game.value = newGame
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newGame))
  } catch (e) {
    error.value = e instanceof Error
        ? `${e.name}: ${e.message}`
        : String(e)

    console.error('Start game failed:', e)
  } finally {
    loading.value = false
  }
}

function clearSavedGame() {
  localStorage.removeItem(STORAGE_KEY)
  game.value = null
  error.value = ''
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

      <button
          class="start-button"
          :disabled="loading"
          @click="clearSavedGame"
      >
        End Game
      </button>
    </section>
  </main>
</template>
