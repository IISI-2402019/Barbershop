<template>
    <div class="admin-container">
        <h2>Admin Dashboard</h2>
        <el-tabs v-model="activeTab">
            <el-tab-pane label="Appointments" name="appointments">
                <el-table :data="appointments" style="width: 100%">
                    <el-table-column prop="id" label="ID" width="50" />
                    <el-table-column prop="customerName" label="Customer" />
                    <el-table-column prop="stylistName" label="Stylist" />
                    <el-table-column prop="serviceName" label="Service" />
                    <el-table-column prop="startTime" label="Time" />
                    <el-table-column prop="status" label="Status" />
                </el-table>
            </el-tab-pane>
            <el-tab-pane label="Stylists" name="stylists">
                <p>Stylist management coming soon...</p>
            </el-tab-pane>
            <el-tab-pane label="Schedule" name="schedule">
                <p>Schedule management coming soon...</p>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const activeTab = ref('appointments')
const appointments = ref([])

onMounted(async () => {
    if (userStore.dbUser?.role === 'ADMIN') {
        await fetchAppointments()
    }
})

const fetchAppointments = async () => {
    try {
        // Assuming we will have an endpoint for all appointments
        // For now, let's just try to fetch something or mock it if the endpoint doesn't exist yet
        // const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/appointments`)
        // appointments.value = response.data

        // Mock data for visualization until backend is ready
        appointments.value = [
            { id: 1, customerName: 'Test User', stylistName: 'Alice', serviceName: 'Cut', startTime: '2023-10-27 14:00', status: 'BOOKED' }
        ]
    } catch (error) {
        console.error('Failed to fetch appointments', error)
    }
}
</script>

<style scoped>
.admin-container {
    padding: 20px;
}
</style>
