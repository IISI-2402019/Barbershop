<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { initLiff } from './utils/liff'
import { useUserStore } from './stores/user'

const userStore = useUserStore()
const liffId = import.meta.env.VITE_LIFF_ID


onMounted(async () => {
  // Only init LIFF if ID is present (skip in local dev if needed, or use mock)
  if (liffId && liffId !== 'YOUR_LIFF_ID') {
    try {
      const profile = await initLiff(liffId)
      if (profile) {
        userStore.setProfile(profile)
        await userStore.loginToBackend()
      }
    } catch (e) {
      console.error('App LIFF init error:', e)
    }
  } else {
    console.warn('LIFF ID not found. Running in standalone mode.')
    // Mock login for dev
    if (import.meta.env.DEV) {
       userStore.setProfile({ userId: 'mock_user', displayName: 'Dev User' })
       // userStore.loginToBackend() // Uncomment to test backend in dev
    }
  }
})
</script>

<template>
  <div v-if="userStore.isLoading" class="loading-overlay">
    Loading...
  </div>
  <RouterView />
</template>

<style>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255,255,255,0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
</style>
