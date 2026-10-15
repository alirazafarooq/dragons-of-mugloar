
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getHealth, type HealthResponse } from './services/api'

const health = ref<HealthResponse | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    health.value = await getHealth()
  } catch {
    error.value =
        'Could not connect to the backend. Check that Spring Boot is running on port 8080.'
  }
})
</script>

<template>
  <main class="page">
    <h1>Dragons of Mugloar</h1>
    <p>Game dashboard connection test</p>

    <section class="status">
      <h2>Backend status</h2>

      <p v-if="health">
        <span class="dot"></span>
        {{ health.status }} — {{ health.application }}
      </p>

      <p v-else-if="error" class="error">
        {{ error }}
      </p>

      <p v-else>Connecting to backend...</p>
    </section>
  </main>
</template>

<style>
body {
  margin: 0;
  background: #10141c;
  color: #f4f6fa;
  font-family: Inter, system-ui, sans-serif;
}

.page {
  max-width: 900px;
  margin: 70px auto;
  padding: 24px;
}

h1 {
  font-size: 36px;
}

.status {
  margin-top: 32px;
  padding: 24px;
  border: 1px solid #343d4d;
  border-radius: 12px;
  background: #1a202c;
}

.dot {
  display: inline-block;
  width: 9px;
  height: 9px;
  margin-right: 8px;
  border-radius: 50%;
  background: #4ade80;
}

.error {
  color: #f87171;
}
</style>
