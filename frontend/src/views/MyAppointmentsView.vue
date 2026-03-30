<template>
    <div class="my-appointments">
        <h2>{{ $t('home.myAppointments') }}</h2>
        <div v-if="loading">{{ $t('common.loading') }}</div>
        <div v-else-if="appointments.length === 0">{{ $t('appointments.noAppointments') }}</div>
        <el-card v-else v-for="appt in appointments" :key="appt.id" class="appointment-card"
            :class="{ 'is-disabled': isPast(appt.startTime) }">
            <template #header>
                <div class="card-header">
                    <span>{{ formatTime(appt.startTime) }}</span>
                    <el-tag :type="getStatusType(appt.status)">{{ appt.status }}</el-tag>
                </div>
            </template>
            <p><strong>{{ $t('booking.stylist') }}:</strong> {{ appt.stylist.name }}</p>
            <p><strong>{{ $t('booking.service') }}:</strong> {{ appt.service.name }}</p>
            <div class="actions" style="margin-top: 10px; text-align: right;" v-if="appt.status === 'BOOKED'">
                <el-button size="small" @click="handleEdit(appt)" :disabled="isPast(appt.startTime)">{{
                    $t('common.edit')
                    }}</el-button>
                <el-button size="small" type="danger" @click="handleDelete(appt)" :disabled="isPast(appt.startTime)">{{
                    $t('common.cancel') }}</el-button>
            </div>
        </el-card>

        <!-- Edit Dialog -->
        <el-dialog v-model="editDialogVisible" :title="$t('appointments.reschedule')">
            <p>{{ $t('appointments.current') }}: {{ formatTime(editingAppt?.startTime) }}</p>
            <el-form>
                <el-form-item :label="$t('booking.service')">
                    <el-select v-model="newServiceId" :placeholder="$t('booking.selectService')">
                        <el-option v-for="service in services" :key="service.id"
                            :label="service.name + ' (' + service.durationHours + 'h)'" :value="service.id" />
                    </el-select>
                </el-form-item>
                <el-form-item :label="$t('appointments.newDate')">
                    <el-date-picker v-model="newDate" type="date" :placeholder="$t('booking.selectDate')"
                        :disabled-date="disabledDate" @change="fetchSlots" />
                </el-form-item>
                <el-form-item :label="$t('appointments.newTime')" v-if="newDate">
                    <el-select v-model="newTime" :placeholder="$t('booking.selectTime')" v-loading="loadingSlots">
                        <el-option v-for="slot in availableSlots" :key="slot" :label="slot" :value="slot" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="confirmUpdate" :disabled="!newTime || !newServiceId" :loading="loadingUpdate">{{
                        $t('common.confirm')
                    }}</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'
import { config } from '../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()
const appointments = ref([])
const services = ref([])
const loading = ref(true)
const loadingUpdate = ref(false)

// Edit State
const editDialogVisible = ref(false)
const editingAppt = ref(null)
const newDate = ref('')
const newTime = ref('')
const newServiceId = ref(null)
const availableSlots = ref([])
const maxBookingDate = ref(null)
const weeklyOffDays = ref([])
const unavailableDates = ref([])

onMounted(async () => {
    // Fetch Settings
    try {
        const settingsRes = await axios.get(`${config.apiBaseUrl}/api/settings`)
        if (settingsRes.data) {
            if (settingsRes.data.max_booking_date) {
                maxBookingDate.value = new Date(settingsRes.data.max_booking_date)
                maxBookingDate.value.setHours(23, 59, 59, 999)
            }
            if (settingsRes.data.weekly_off_day) {
                weeklyOffDays.value = settingsRes.data.weekly_off_day.split(',').map(d => parseInt(d.trim())).filter(n => !isNaN(n))
            }
        }
    } catch (e) {
        console.error('Failed to load settings', e)
    }

    if (userStore.dbUser) {
        await fetchAppointments()
        await fetchServices()
    }
})

const fetchServices = async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/services`)
        services.value = response.data
    } catch (error) {
        console.error('Failed to fetch services', error)
    }
}

const fetchAppointments = async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/appointments/my`, {
            params: { userId: userStore.dbUser.id }
        })

        // Filter: only show appointments within the last 6 months
        const sixMonthsAgo = new Date()
        sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

        appointments.value = response.data.filter(appt => {
            return new Date(appt.startTime) >= sixMonthsAgo
        })
    } catch (error) {
        console.error('Failed to fetch appointments', error)
        ElMessage.error(t('common.error'))
    } finally {
        loading.value = false
    }
}

const isPast = (dateString) => {
    return new Date(dateString) < new Date()
}

const isToday = (dateString) => {
    const date = new Date(dateString)
    const today = new Date()
    return date.getDate() === today.getDate() &&
        date.getMonth() === today.getMonth() &&
        date.getFullYear() === today.getFullYear()
}

const fetchUnavailableDates = async (stylistId) => {
    try {
        const res = await axios.get(`${config.apiBaseUrl}/api/schedules/unavailable-dates`, {
            params: { stylistId: stylistId }
        })
        unavailableDates.value = res.data
    } catch (e) {
        console.error('Failed to fetch unavailable dates', e)
    }
}

const handleEdit = (appt) => {
    if (isToday(appt.startTime)) {
        ElMessageBox.alert(t('appointments.callToReschedule'), t('appointments.cannotEdit'), {
            confirmButtonText: t('common.confirm')
        })
        return
    }
    editingAppt.value = appt
    newDate.value = ''
    newTime.value = ''
    newServiceId.value = appt.service.id
    availableSlots.value = []
    
    fetchUnavailableDates(appt.stylist.id)
    
    editDialogVisible.value = true
}

const handleDelete = (appt) => {
    if (isToday(appt.startTime)) {
        ElMessageBox.alert(t('appointments.callToCancel'), t('appointments.cannotCancel'), {
            confirmButtonText: t('common.confirm')
        })
        return
    }
    ElMessageBox.confirm(
        t('appointments.cancelConfirm'),
        t('common.warning'),
        {
            confirmButtonText: t('common.confirm'),
            cancelButtonText: t('common.cancel'),
            type: 'warning',
        }
    ).then(async () => {
        try {
            await axios.delete(`${config.apiBaseUrl}/api/appointments/${appt.id}`)
            ElMessage.success(t('appointments.cancelSuccess'))
            await fetchAppointments()
        } catch (error) {
            console.error('Failed to cancel', error)
            ElMessage.error(t('common.error'))
        }
    })
}

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

const toLocalISOString = (date) => {
    const tzOffset = date.getTimezoneOffset() * 60000
    return new Date(date.getTime() - tzOffset).toISOString().slice(0, 10)
}

const loadingSlots = ref(false)

const fetchSlots = async () => {
    if (!newDate.value || !editingAppt.value) return

    const selectedServiceId = newServiceId.value
    const stylistId = editingAppt.value.stylist.id
    const dateStr = toLocalISOString(newDate.value)

    if (!selectedServiceId || !stylistId || !dateStr) return

    loadingSlots.value = true
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/appointments/available-slots`, {
            params: {
                stylistId: stylistId,
                date: dateStr,
                serviceId: selectedServiceId,
                excludeAppointmentId: editingAppt.value.id,
                _t: Date.now() // Prevent caching
            }
        })
        availableSlots.value = response.data
    } catch (error) {
        console.error('Failed to fetch slots', error)
        ElMessage.error(t('common.error'))
        availableSlots.value = []
    } finally {
        loadingSlots.value = false
    }
}

const confirmUpdate = async () => {
    if (!newDate.value || !newTime.value || !newServiceId.value) return

    const dateStr = toLocalISOString(newDate.value)
    const dateTimeStr = `${dateStr}T${newTime.value}:00`

    loadingUpdate.value = true
    try {
        await axios.put(`${config.apiBaseUrl}/api/appointments/${editingAppt.value.id}`, {
            userId: userStore.dbUser.id,
            stylistId: editingAppt.value.stylist.id,
            serviceId: newServiceId.value,
            startTime: dateTimeStr
        })
        ElMessage.success(t('common.success'))
        editDialogVisible.value = false
        await fetchAppointments()
    } catch (error) {
        console.error('Failed to update', error)
        ElMessage.error(error.response?.data || t('common.error'))
    } finally {
        loadingUpdate.value = false
    }
}

const getStatusType = (status) => {
    switch (status) {
        case 'BOOKED': return 'success'
        case 'CANCELED': return 'danger'
        case 'COMPLETED': return 'info'
        default: return ''
    }
}

const formatTime = (timeStr) => {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    })
}
</script>

<style scoped>
.my-appointments {
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
}

@media (max-width: 480px) {
    .my-appointments {
        padding: 20px 16px;
    }
}

.appointment-card {
    margin-bottom: 15px;
}

.appointment-card.is-disabled {
    opacity: 0.6;
    background-color: #f5f7fa;
    pointer-events: none;
    /* Optional: prevents clicks on the card itself if needed, but we just disabled buttons */
}

/* Re-enable pointer events for buttons so we can see the disabled cursor if we want, 
   but since buttons are disabled attribute, they won't click anyway. 
   If we want the 'disabled cursor' on the whole card: */
.appointment-card.is-disabled {
    cursor: not-allowed;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>