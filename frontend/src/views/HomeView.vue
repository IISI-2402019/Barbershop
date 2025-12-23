<template>
    <div class="home">
        <h1>Barbershop Appointment System</h1>

        <div v-if="userStore.isLoggedIn" class="user-info">
            <p>Welcome, {{ userStore.profile?.displayName }}</p>
            <div class="actions">
                <el-button type="primary" size="large" @click="$router.push('/stylists')">Book Now</el-button>
                <el-button size="large" @click="$router.push('/my-appointments')">My Appointments</el-button>
                <el-button v-if="userStore.dbUser?.role === 'ADMIN'" type="warning" size="large"
                    @click="$router.push('/admin')">Admin Dashboard</el-button>
            </div>
        </div>

        <div v-else class="login-prompt">
            <p>Please login to book an appointment.</p>
            <el-button type="success" size="large" @click="manualLogin">Login with LINE</el-button>
        </div>
    </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import liff from '@line/liff'

const userStore = useUserStore()

const manualLogin = () => {
    if (!liff.isLoggedIn()) {
        liff.login()
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
