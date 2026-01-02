<template>
    <div class="booking-container">
        <h2>{{ $t('booking.title') }}</h2>

        <el-form :model="form" label-width="120px" label-position="top">

            <!-- Stylist Info (Read-only or Selectable if not passed) -->
            <el-form-item :label="$t('booking.stylist')">
                <el-select v-model="form.stylistId" :placeholder="$t('booking.selectStylist')" disabled>
                    <el-option v-for="s in stylists" :key="s.id" :label="s.name" :value="s.id" />
                </el-select>
            </el-form-item>

            <!-- Service Selection -->
            <el-form-item :label="$t('booking.service')">
                <el-select v-model="form.serviceId" :placeholder="$t('booking.selectService')" @change="updateDuration">
                    <el-option v-for="service in services" :key="service.id"
                        :label="service.name + ' (' + service.duration + 'h)'" :value="service.id" />
                </el-select>
            </el-form-item>

            <!-- Date Selection -->
            <el-form-item :label="$t('booking.date')">
                <el-date-picker v-model="form.date" type="date" :placeholder="$t('booking.selectDate')"
                    :disabled-date="disabledDate" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </el-form-item>

            <!-- Time Selection -->
            <el-form-item :label="$t('booking.time')">
                <el-time-select v-model="form.time" start="11:00" step="00:30" end="21:00"
                    :placeholder="$t('booking.selectTime')" />
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitBooking">{{ $t('common.confirm') }}</el-button>
                <el-button @click="$router.back()">{{ $t('common.cancel') }}</el-button>
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
import { config } from '../config'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
        const sRes = await axios.get(`${config.apiBaseUrl}/api/stylists`)
        stylists.value = sRes.data
    } catch (e) {
        console.error('Failed to load stylists', e)
    }

    // Fetch Services
    try {
        const svRes = await axios.get(`${config.apiBaseUrl}/api/services`)
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
        ElMessage.error(t('booking.fillAllFields'))
        return
    }

    if (!userStore.dbUser) {
        ElMessage.error(t('booking.notLoggedIn'))
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

        await axios.post(`${config.apiBaseUrl}/api/appointments`, payload)

        ElMessage.success(t('booking.success'))
        setTimeout(() => {
            router.push('/')
        }, 1500)
    } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data || t('common.error'))
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
