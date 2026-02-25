<template>
  <header class="app-header">
    <div class="header-content">
      <router-link to="/" class="logo-link">
        <img :src="logo" alt="Barbershop Logo" class="header-logo" />
        <span class="app-title">Barbershop</span>
      </router-link>
      
      <div class="header-actions">
        <el-button size="small" @click="toggleLanguage">
          {{ locale === 'zh-TW' ? 'English' : '繁體中文' }}
        </el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import logo from '../assets/bbs.png'

const { locale } = useI18n()

const toggleLanguage = () => {
  locale.value = locale.value === 'zh-TW' ? 'en' : 'zh-TW'
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-content {
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
  gap: 10px;
}

.header-logo {
  height: 40px;
  width: auto;
}

.app-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
}

@media (max-width: 480px) {
  .app-title {
    display: none; /* Hide title on very small screens if needed */
  }
}
</style>
