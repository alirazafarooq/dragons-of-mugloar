
<script setup lang="ts">
import { onMounted } from 'vue'
import GameHeader from './components/GameHeader.vue'
import GameDashboard from './components/GameDashboard.vue'
import MissionList from './components/MissionList.vue'
import MissionOutcome from './components/MissionOutcome.vue'
import { useGame } from './composables/useGame'

const {
  game,
  missions,
  loading,
  missionsLoading,
  error,
  missionsError,
  solvingMissionId,
  missionOutcome,
  missionActionError,
  isGameOver,
  startGame,
  solveMission,
  resumeGame,
  retryMissions,
  clearError,
  endGame,
} = useGame()

onMounted(() => {
  void resumeGame()
})

function handleSolveMission(adId: string) {
  const mission = missions.value.find(
      (item) => item.adId === adId,
  )

  if (mission) {
    return solveMission(mission)
  }
}
</script>

<template>
  <main class="game-page">
    <GameHeader :loading="loading" @start="startGame" />

    <section v-if="error" class="error-message" role="alert">
      <span>{{ error }}</span>
      <button class="dismiss-button" @click="clearError" aria-label="Dismiss error">
        ×
      </button>
    </section>

    <section v-if="game">
      <GameDashboard :game="game" @end="endGame" />

      <section v-if="missionOutcome" class="outcome-section">
        <p class="eyebrow">MISSION RESULT</p>
        <MissionOutcome
            :success="missionOutcome.success"
            :message="missionOutcome.message"
        />
      </section>

      <section v-if="missionActionError" class="error-message" role="alert">
        <p>Mission attempt could not be confirmed: {{ missionActionError }}</p>
        <p>
          Check the game state before attempting another mission, because the
          original request may have reached the game service.
        </p>
      </section>

      <MissionList
          :missions="missions"
          :loading="missionsLoading"
          :error="missionsError"
          :game-over="isGameOver"
          :solving-mission-id="solvingMissionId"
          @solve="handleSolveMission"
          @retry="retryMissions"
      />
    </section>
  </main>
</template>
