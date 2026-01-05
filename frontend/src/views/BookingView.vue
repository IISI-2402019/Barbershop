<template>
    <div class="booking-container">
        <h2>{{ $t('booking.title') }}</h2>

        <el-form :model="form" label-width="120px" label-position="top">

            <!-- Stylist Info -->
            <el-form-item :label="$t('booking.stylist')">
                <el-select v-model="form.stylistId" :placeholder="$t('booking.selectStylist')"
                    @change="handleStylistChange">
                    <el-option v-for="s in stylists" :key="s.id" :label="s.name" :value="s.id" />
                </el-select>
            </el-form-item>

            <!-- Service Selection -->
            <el-form-item :label="$t('booking.service')">
                <el-select v-model="form.serviceId" :placeholder="$t('booking.selectService')"
                    :disabled="!form.stylistId" @change="handleServiceChange">
                    <el-option v-for="service in services" :key="service.id"
                        :label="service.name + ' (' + service.durationHours + 'h)'" :value="service.id" />
                </el-select>
            </el-form-item>

            <!-- Date Selection -->
            <el-form-item :label="$t('booking.date')">
                <el-date-picker v-model="form.date" type="date" :placeholder="$t('booking.selectDate')"
                    :disabled="!form.serviceId" :disabled-date="disabledDate" format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD" @change="handleDateChange" />
            </el-form-item>

            <!-- Time Selection -->
            <el-form-item :label="$t('booking.time')">
                <el-select v-model="form.time" :placeholder="$t('booking.selectTime')"
                    :disabled="!form.date || availableSlots.length === 0" v-loading="loadingSlots">
                    <el-option v-for="slot in availableSlots" :key="slot" :label="slot" :value="slot" />
                </el-select>
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
const availableSlots = ref([])
const loadingSlots = ref(false)

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

const handleStylistChange = () => {
    form.value.serviceId = null
    form.value.date = ''
    form.value.time = ''
    availableSlots.value = []
}

const handleServiceChange = () => {
    form.value.date = ''
    form.value.time = ''
    availableSlots.value = []
}

const handleDateChange = () => {
    form.value.time = ''
    fetchAvailableSlots()
}

const fetchAvailableSlots = async () => {
    if (!form.value.stylistId || !form.value.serviceId || !form.value.date) return

    loadingSlots.value = true
    try {
        const res = await axios.get(`${config.apiBaseUrl}/api/appointments/available-slots`, {
            params: {
                stylistId: form.value.stylistId,
                date: form.value.date,
                serviceId: form.value.serviceId
            }
        })
        availableSlots.value = res.data
    } catch (e) {
        console.error('Failed to fetch slots', e)
        ElMessage.error(t('common.error'))
    } finally {
        loadingSlots.value = false
    }
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
        if (error.response && error.response.status === 400 && error.response.data === "該時段已被預約，請再次選擇") {
            ElMessage.error(error.response.data)
            // Clear selected time
            form.value.time = ''
            // Reload available slots
            fetchAvailableSlots()
        } else {
            ElMessage.error(error.response?.data || t('common.error'))
        }
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
