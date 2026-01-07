<template>
    <div class="home">
        <img :src="logoEntry" class="logo" alt="Barbershop Logo" />

        <div v-if="userStore.isLoggedIn" class="user-info">
            <p>{{ $t('home.welcome', { name: userStore.profile?.displayName }) }}</p>
            <div class="actions">
                <el-button class="home-btn btn-book" @click="$router.push('/stylists')">
                    <div class="btn-content">
                        <el-icon :size="40">
                            <Calendar />
                        </el-icon>
                        <span>{{ $t('home.bookNow') }}</span>
                    </div>
                </el-button>
                <el-button class="home-btn btn-appt" @click="$router.push('/my-appointments')">
                    <div class="btn-content">
                        <el-icon :size="40">
                            <User />
                        </el-icon>
                        <span>{{ $t('home.myAppointments') }}</span>
                    </div>
                </el-button>
                <el-button v-if="userStore.dbUser?.role === 'ADMIN'" class="home-btn btn-admin"
                    @click="$router.push('/admin')">
                    <div class="btn-content">
                        <el-icon :size="40">
                            <Setting />
                        </el-icon>
                        <span>{{ $t('home.adminDashboard') }}</span>
                    </div>
                </el-button>
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
import logoEntry from '../assets/bbs.png'
import { Calendar, User, Setting } from '@element-plus/icons-vue'

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
    min-height: 80vh;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.logo {
    max-width: 300px;
    margin-bottom: 30px;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.actions {
    margin-top: 30px;
    display: flex;
    flex-direction: row;
    gap: 30px;
    justify-content: center;
    flex-wrap: wrap;
}

@media (max-width: 768px) {
    .actions {
        flex-direction: column;
        align-items: center;
    }

    .home-btn {
        margin-left: 0 !important;
    }
}

.home-btn {
    width: 150px;
    height: 150px;
    border-radius: 20px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    border: none;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.home-btn:hover {
    transform: translateY(-10px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.btn-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

.btn-content span {
    font-size: 16px;
    font-weight: 500;
    white-space: normal;
    line-height: 1.4;
    letter-spacing: 0.02em;
}

/* Custom Button Colors for Soft Theme */
.btn-book {
    background-color: var(--color-primary) !important;
    color: white !important;
    border: none !important;
}

.btn-book:hover,
.btn-book:focus {
    background-color: var(--color-primary-dark) !important;
    transform: translateY(-5px);
}

.btn-appt {
    background-color: var(--color-secondary) !important;
    color: var(--color-text-main) !important;
    /* Dark text for contrast on beige */
    border: none !important;
}

.btn-appt:hover,
.btn-appt:focus {
    background-color: var(--color-accent) !important;
    /* Darker Gold on hover */
    color: white !important;
    transform: translateY(-5px);
}

.btn-admin {
    background-color: var(--color-text-light) !important;
    color: white !important;
    border: none !important;
}

.btn-admin:hover,
.btn-admin:focus {
    background-color: var(--color-text-main) !important;
    transform: translateY(-5px);
}

.login-prompt {
    margin-top: 40px;
    padding: 30px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 20px;
    backdrop-filter: blur(10px);
}
</style>
