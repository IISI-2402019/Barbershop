<template>
    <div class="my-appointments">
        <h2>{{ $t('home.myAppointments') }}</h2>
        <div v-if="loading">{{ $t('common.loading') }}</div>
        <div v-else-if="appointments.length === 0">{{ $t('appointments.noAppointments') }}</div>
        <el-card v-else v-for="appt in appointments" :key="appt.id" class="appointment-card">
            <template #header>
                <div class="card-header">
                    <span>{{ formatTime(appt.startTime) }}</span>
                    <el-tag :type="getStatusType(appt.status)">{{ appt.status }}</el-tag>
                </div>
            </template>
            <p><strong>{{ $t('booking.stylist') }}:</strong> {{ appt.stylist.name }}</p>
            <p><strong>{{ $t('booking.service') }}:</strong> {{ appt.service.name }}</p>
            <div class="actions" style="margin-top: 10px; text-align: right;" v-if="appt.status === 'BOOKED'">
                <el-button size="small" @click="handleEdit(appt)">{{ $t('common.edit') }}</el-button>
                <el-button size="small" type="danger" @click="handleDelete(appt)">{{ $t('common.cancel') }}</el-button>
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
                    <el-select v-model="newTime" :placeholder="$t('booking.selectTime')">
                        <el-option v-for="slot in availableSlots" :key="slot" :label="slot" :value="slot" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="confirmUpdate" :disabled="!newTime || !newServiceId">{{
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

// Edit State
const editDialogVisible = ref(false)
const editingAppt = ref(null)
const newDate = ref('')
const newTime = ref('')
const newServiceId = ref(null)
const availableSlots = ref([])

onMounted(async () => {
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
        appointments.value = response.data
    } catch (error) {
        console.error('Failed to fetch appointments', error)
        ElMessage.error(t('common.error'))
    } finally {
        loading.value = false
    }
}

const isToday = (dateString) => {
    const date = new Date(dateString)
    const today = new Date()
    return date.getDate() === today.getDate() &&
        date.getMonth() === today.getMonth() &&
        date.getFullYear() === today.getFullYear()
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
    return time.getTime() < Date.now()
}

const toLocalISOString = (date) => {
    const tzOffset = date.getTimezoneOffset() * 60000
    return new Date(date.getTime() - tzOffset).toISOString().slice(0, 10)
}

const fetchSlots = async () => {
    if (!newDate.value || !editingAppt.value) return

    // Simple slot generation for now (10:00 to 20:00)
    // In a real app, fetch from backend based on stylist availability
    // We can reuse the logic from BookingView if we extract it, but for now let's mock it or fetch simply
    // Ideally we should call an API to get available slots for the stylist on that day

    // Let's just generate slots and let the backend reject if busy for simplicity in this iteration
    // Or better: Call the same API used in BookingView if available?
    // We don't have a public "get slots" API yet, BookingView calculates it.

    const slots = []
    for (let i = 10; i < 20; i++) {
        slots.push(`${i}:00`)
        slots.push(`${i}:30`)
    }
    availableSlots.value = slots
}

const confirmUpdate = async () => {
    if (!newDate.value || !newTime.value || !newServiceId.value) return

    const dateStr = toLocalISOString(newDate.value)
    const dateTimeStr = `${dateStr}T${newTime.value}:00`

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
    }
}

const getStatusType = (status) => {
    switch (status) {
        case 'BOOKED': return 'success'
        case 'CANCELLED': return 'danger'
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

.appointment-card {
    margin-bottom: 15px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>