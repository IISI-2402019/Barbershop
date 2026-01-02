<template>
    <div class="home">
        <h1>{{ $t('home.title') }}</h1>

        <div v-if="userStore.isLoggedIn" class="user-info">
            <p>{{ $t('home.welcome', { name: userStore.profile?.displayName }) }}</p>
            <div class="actions">
                <el-button type="primary" size="large" @click="$router.push('/stylists')">{{ $t('home.bookNow')
                    }}</el-button>
                <el-button size="large" @click="$router.push('/my-appointments')">{{ $t('home.myAppointments')
                    }}</el-button>
                <el-button v-if="userStore.dbUser?.role === 'ADMIN'" type="warning" size="large"
                    @click="$router.push('/admin')">{{ $t('home.adminDashboard') }}</el-button>
            </div>
        </div>

        <div v-else class="login-prompt">
            <p>{{ $t('home.loginPrompt') }}</p>
            <el-button type="success" size="large" @click="manualLogin">{{ $t('home.loginWithLine') }}</el-button>
        </div>
    </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import liff from '@line/liff'

const userStore = useUserStore()

const manualLogin = () => {
    if (!liff.isLoggedIn()) {
        liff.login({ redirectUri: window.location.href })
    }
}
</script>

<style scoped>
.home {
    text-align: center;
    padding: 40px 20px;
}

.actions {
    margin-top: 30px;
    display: flex;
    flex-direction: column;
    gap: 15px;
    align-items: center;
}
</style>
