<template>
    <div class="booking-container">
        <h2>Book Appointment</h2>

        <el-form :model="form" label-width="120px" label-position="top">

            <!-- Stylist Info (Read-only or Selectable if not passed) -->
            <el-form-item label="Stylist">
                <el-select v-model="form.stylistId" placeholder="Select Stylist" disabled>
                    <el-option v-for="s in stylists" :key="s.id" :label="s.name" :value="s.id" />
                </el-select>
            </el-form-item>

            <!-- Service Selection -->
            <el-form-item label="Service">
                <el-select v-model="form.serviceId" placeholder="Select Service" @change="updateDuration">
                    <el-option v-for="service in services" :key="service.id"
                        :label="service.name + ' (' + service.duration + 'h)'" :value="service.id" />
                </el-select>
            </el-form-item>

            <!-- Date Selection -->
            <el-form-item label="Date">
                <el-date-picker v-model="form.date" type="date" placeholder="Pick a day" :disabled-date="disabledDate"
                    format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </el-form-item>

            <!-- Time Selection -->
            <el-form-item label="Time">
                <el-time-select v-model="form.time" start="11:00" step="00:30" end="21:00" placeholder="Select time" />
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitBooking">Confirm Booking</el-button>
                <el-button @click="$router.back()">Cancel</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const stylists = ref([])
const services = ref([])

const form = ref({
    stylistId: null,
    serviceId: null,
    date: '',
    time: ''
})

onMounted(async () => {
    // Fetch Stylists
    try {
        const sRes = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/stylists`)
        stylists.value = sRes.data
    } catch (e) {
        console.error('Failed to load stylists', e)
    }

    // Fetch Services
    try {
        const svRes = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/services`)
        services.value = svRes.data
    } catch (e) {
        console.error('Failed to load services', e)
    }

    if (route.query.stylistId) {
        form.value.stylistId = parseInt(route.query.stylistId)
    }
})

const disabledDate = (time) => {
    return time.getTime() < Date.now() - 8.64e7
}

const updateDuration = () => {
    // Logic to calculate end time could go here
}

const submitBooking = async () => {
    if (!form.value.serviceId || !form.value.date || !form.value.time) {
        ElMessage.error('Please fill in all fields')
        return
    }

    if (!userStore.dbUser) {
        ElMessage.error('User not logged in. Please refresh.')
        return
    }

    try {
        const startTime = `${form.value.date}T${form.value.time}:00`

        const payload = {
            userId: userStore.dbUser.id,
            stylistId: form.value.stylistId,
            serviceId: form.value.serviceId,
            startTime: startTime
        }

        await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/appointments`, payload)

        ElMessage.success('Booking Submitted!')
        setTimeout(() => {
            router.push('/')
        }, 1500)
    } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data || 'Booking failed')
    }
}
</script>
<style scoped>
.booking-container {
    padding: 20px;
    max-width: 600px;
    margin: 0 auto;
}
</style>
