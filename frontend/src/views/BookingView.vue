<template>
    <div class="booking-container">
        <h2>{{ $t('booking.title') }}</h2>

        <el-form :model="form" label-width="120px" label-position="top">

            <!-- Stylist Info -->
            <el-form-item :label="$t('booking.stylist')">
                <el-select v-model="form.stylistId" :placeholder="$t('booking.selectStylist')"
                    @change="handleStylistChange" :loading="loadingStylists" v-loading="loadingStylists">
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
                <div v-if="!form.date || availableSlots.length === 0" class="time-slots-placeholder">
                    {{ !form.date ? $t('booking.selectDateFirst') : (loadingSlots ? $t('common.loading') :
                        $t('booking.noSlots')) }}
                </div>
                <div v-else class="time-slots-grid" v-loading="loadingSlots">
                    <el-button v-for="slot in availableSlots" :key="slot.time"
                        :type="form.time === slot.time ? 'primary' : (slot.available ? 'default' : '')"
                        :disabled="!slot.available" @click="form.time = slot.time" class="time-slot-btn"
                        :class="{ 'is-unavailable': !slot.available }">
                        {{ slot.time }}
                    </el-button>
                </div>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitBooking" :loading="submitLoading">{{ $t('common.confirm')
                }}</el-button>
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
const unavailableDates = ref([])
const loadingSlots = ref(false)
const loadingStylists = ref(false)
const submitLoading = ref(false)

const form = ref({
    stylistId: null,
    serviceId: null,
    date: '',
    time: ''
})

const maxBookingDate = ref(null)
const weeklyOffDays = ref([])

const fetchUnavailableDates = async () => {
    if (!form.value.stylistId) return
    try {
        const res = await axios.get(`${config.apiBaseUrl}/api/schedules/unavailable-dates`, {
            params: { stylistId: form.value.stylistId }
        })
        unavailableDates.value = res.data
    } catch (e) {
        console.error('Failed to fetch unavailable dates', e)
    }
}

onMounted(async () => {
    // Fetch Settings
    try {
        const settingsRes = await axios.get(`${config.apiBaseUrl}/api/settings`)
        if (settingsRes.data) {
            if (settingsRes.data.max_booking_date) {
                maxBookingDate.value = new Date(settingsRes.data.max_booking_date)
                // Ensure the max date is inclusive by setting time to end of day
                maxBookingDate.value.setHours(23, 59, 59, 999)
            }
            if (settingsRes.data.weekly_off_day) {
                weeklyOffDays.value = settingsRes.data.weekly_off_day.split(',').map(d => parseInt(d.trim())).filter(n => !isNaN(n))
            }
        }
    } catch (e) {
        console.error('Failed to load settings', e)
    }

    // Fetch Stylists
    loadingStylists.value = true
    try {
        const sRes = await axios.get(`${config.apiBaseUrl}/api/stylists`)
        stylists.value = sRes.data
    } catch (e) {
        console.error('Failed to load stylists', e)
    } finally {
        loadingStylists.value = false
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
        fetchUnavailableDates()
    }
})

const disabledDate = (time) => {
    const isPast = time.getTime() < Date.now() - 8.64e7
    if (isPast) return true

    const year = time.getFullYear()
    const month = String(time.getMonth() + 1).padStart(2, '0')
    const day = String(time.getDate()).padStart(2, '0')
    const dateStr = `${year}-${month}-${day}`

    if (unavailableDates.value.includes(dateStr)) {
        return true
    }

    // Check Weekly Off Day
    if (weeklyOffDays.value.includes(time.getDay())) {
        return true
    }

    if (maxBookingDate.value) {
        return time.getTime() > maxBookingDate.value.getTime()
    }
    return false
}

const handleStylistChange = () => {
    form.value.serviceId = null
    form.value.date = ''
    form.value.time = ''
    availableSlots.value = []
    fetchUnavailableDates()
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
                serviceId: form.value.serviceId,
                _t: Date.now() // Prevent caching
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

    submitLoading.value = true
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
            router.push({ name: 'my-appointments' })
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
    } finally {
        submitLoading.value = false
    }
}
</script>
<style scoped>
.booking-container {
    padding: 20px;
    max-width: 600px;
    margin: 0 auto;
}

.time-slots-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 10px;
    margin-top: 10px;
    width: 100%;
}

.time-slot-btn {
    width: 100%;
    margin: 0 !important;
    /* Override Element Plus default margins */
}

.time-slots-placeholder {
    color: #909399;
    font-size: 14px;
    text-align: center;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 4px;
}

.is-unavailable {
    opacity: 0.6;
    cursor: not-allowed;
    background-color: #f5f7fa !important;
    border-color: #e4e7ed !important;
    color: #c0c4cc !important;
}

@media (max-width: 480px) {
    .booking-container {
        padding: 20px 16px;
    }

    .time-slots-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}
</style>
