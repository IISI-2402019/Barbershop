<script setup>
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { initLiff } from './utils/liff'
import { useUserStore } from './stores/user'
import { config } from './config'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()
// Fallback to hardcoded ID if env var is missing (common in some PaaS builds)
const liffId = config.liffId

console.log('Current LIFF ID:', liffId) // Debug log

const toggleLanguage = () => {
  locale.value = locale.value === 'zh-TW' ? 'en' : 'zh-TW'
}

onMounted(async () => {
  // Only init LIFF if ID is present
  if (liffId && liffId !== 'YOUR_LIFF_ID') {
    try {
      const profile = await initLiff(liffId)
      if (profile) {
        userStore.setProfile(profile)
        await userStore.loginToBackend()

        // Check if profile is complete
        if (userStore.dbUser && (!userStore.dbUser.realName || !userStore.dbUser.phone)) {
          router.push('/register')
        }
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
  <div class="language-switcher">
    <el-button size="small" @click="toggleLanguage">
      {{ locale === 'zh-TW' ? 'English' : '繁體中文' }}
    </el-button>
  </div>
  <div v-if="userStore.isLoading" class="loading-overlay">
    {{ $t('common.loading') }}
  </div>
  <RouterView />
</template>

<style>
.language-switcher {
  position: fixed;
  top: 10px;
  right: 10px;
  z-index: 1000;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
</style>
