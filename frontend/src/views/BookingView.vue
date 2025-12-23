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

const route = useRoute()
const router = useRouter()

// Mock Data
const stylists = [
    { id: 1, name: 'Alice' },
    { id: 2, name: 'Bob' },
    { id: 3, name: 'Charlie' }
]

const services = [
    { id: 1, name: 'Cut + Wash', duration: 1 },
    { id: 2, name: 'Scalp Treatment', duration: 1.5 },
    { id: 3, name: 'Hair Dye', duration: 2 },
    { id: 4, name: 'Perm', duration: 3 }
]

const form = ref({
    stylistId: null,
    serviceId: null,
    date: '',
    time: ''
})

onMounted(() => {
    if (route.query.stylistId) {
        form.value.stylistId = parseInt(route.query.stylistId)
    }
})

const disabledDate = (time) => {
    // Example: Disable past dates
    return time.getTime() < Date.now() - 8.64e7
}

const updateDuration = () => {
    // Logic to calculate end time could go here
}

const submitBooking = () => {
    if (!form.value.serviceId || !form.value.date || !form.value.time) {
        ElMessage.error('Please fill in all fields')
        return
    }

    // TODO: Call API to save booking
    console.log('Booking Data:', form.value)

    ElMessage.success('Booking Submitted!')
    setTimeout(() => {
        router.push('/')
    }, 1500)
}
</script>

<style scoped>
.booking-container {
    padding: 20px;
    max-width: 600px;
    margin: 0 auto;
}
</style>
