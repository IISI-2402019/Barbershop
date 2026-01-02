<template>
    <div class="admin-container">
        <h2>{{ $t('home.adminDashboard') }}</h2>
        <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('admin.appointments')" name="appointments">
                <div class="appointment-controls" style="margin-bottom: 20px;">
                    <el-date-picker v-model="exportDateRange" type="datetimerange" :range-separator="$t('admin.to')"
                        :start-placeholder="$t('admin.startDate')" :end-placeholder="$t('admin.endDate')"
                        style="margin-right: 10px;" />
                    <el-button type="success" @click="exportExcel">{{ $t('admin.exportExcel') }}</el-button>
                </div>
                <FullCalendar :options="calendarOptions" />
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.stylists')" name="stylists">
                <div class="stylist-management">
                    <h3>{{ $t('admin.addStylist') }}</h3>
                    <el-form :inline="true" :model="newStylist" class="demo-form-inline">
                        <el-form-item :label="$t('admin.name')">
                            <el-input v-model="newStylist.name" :placeholder="$t('admin.stylistName')" />
                        </el-form-item>
                        <el-form-item :label="$t('admin.image')">
                            <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
                                :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
                                <img v-if="newStylist.avatarUrl" :src="getFullImageUrl(newStylist.avatarUrl)"
                                    class="avatar" />
                                <el-icon v-else class="avatar-uploader-icon">
                                    <Plus />
                                </el-icon>
                            </el-upload>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="addStylist">{{ $t('common.add') }}</el-button>
                        </el-form-item>
                    </el-form>

                    <h3>{{ $t('admin.currentStylists') }}</h3>
                    <el-input v-model="searchQuery" :placeholder="$t('admin.searchStylist')"
                        style="width: 200px; margin-bottom: 10px;" />
                    <el-table :data="filteredStylists" style="width: 100%">
                        <el-table-column prop="name" :label="$t('admin.name')" />
                        <el-table-column :label="$t('admin.image')">
                            <template #default="scope">
                                <img v-if="scope.row.avatarUrl" :src="getFullImageUrl(scope.row.avatarUrl)"
                                    alt="Stylist"
                                    style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover;" />
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('admin.actions')">
                            <template #default="scope">
                                <el-button size="small" @click="openEditDialog(scope.row)">{{ $t('common.edit')
                                    }}</el-button>
                                <el-button size="small" type="danger" @click="deleteStylist(scope.row.id)">{{
                                    $t('common.delete') }}</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.schedule')" name="schedule">
                <div class="schedule-management">
                    <div class="schedule-controls" style="margin-bottom: 20px;">
                        <el-button type="primary" @click="openAddScheduleDialog">{{ $t('admin.addSchedule')
                            }}</el-button>
                        <el-button type="danger" @click="openStoreClosedDialog">{{ $t('admin.storeClosed')
                            }}</el-button>
                    </div>
                    <FullCalendar :options="scheduleCalendarOptions" />
                </div>
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.services')" name="services">
                <div class="service-management">
                    <h3>{{ $t('admin.addService') }}</h3>
                    <el-form :inline="true" :model="newService" class="demo-form-inline">
                        <el-form-item :label="$t('admin.serviceNameLabel')">
                            <el-input v-model="newService.name" :placeholder="$t('admin.serviceNameLabel')" />
                        </el-form-item>
                        <el-form-item :label="$t('admin.servicePrice')">
                            <el-input-number v-model="newService.price" :min="0" />
                        </el-form-item>
                        <el-form-item>
                            <el-checkbox v-model="newService.isPriceStartingFrom">{{ $t('admin.priceStartingFrom')
                            }}</el-checkbox>
                        </el-form-item>
                        <el-form-item :label="$t('admin.serviceDuration')">
                            <el-input-number v-model="newService.durationHours" :step="0.5" :min="0.5" />
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="addService">{{ $t('common.add') }}</el-button>
                        </el-form-item>
                    </el-form>

                    <h3>{{ $t('admin.currentServices') }}</h3>
                    <el-input v-model="serviceSearchQuery" :placeholder="$t('admin.searchService')"
                        style="width: 200px; margin-bottom: 10px;" />
                    <el-table :data="filteredServices" style="width: 100%">
                        <el-table-column prop="name" :label="$t('admin.serviceNameLabel')" />
                        <el-table-column :label="$t('admin.servicePrice')">
                            <template #default="scope">
                                {{ scope.row.price }} {{ scope.row.isPriceStartingFrom ? $t('admin.priceStartingFrom') :
                                    '' }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="durationHours" :label="$t('admin.serviceDuration')" />
                        <el-table-column :label="$t('admin.actions')">
                            <template #default="scope">
                                <el-button size="small" @click="openEditServiceDialog(scope.row)">{{ $t('common.edit')
                                }}</el-button>
                                <el-button size="small" type="danger" @click="deleteService(scope.row.id)">{{
                                    $t('common.delete') }}</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-tab-pane>
        </el-tabs>

        <!-- Edit Service Dialog -->
        <el-dialog v-model="editServiceDialogVisible" :title="$t('admin.editService')">
            <el-form :model="editingService">
                <el-form-item :label="$t('admin.serviceNameLabel')">
                    <el-input v-model="editingService.name" />
                </el-form-item>
                <el-form-item :label="$t('admin.servicePrice')">
                    <el-input-number v-model="editingService.price" :min="0" />
                </el-form-item>
                <el-form-item>
                    <el-checkbox v-model="editingService.isPriceStartingFrom">{{ $t('admin.priceStartingFrom')
                    }}</el-checkbox>
                </el-form-item>
                <el-form-item :label="$t('admin.serviceDuration')">
                    <el-input-number v-model="editingService.durationHours" :step="0.5" :min="0.5" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editServiceDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="updateService">{{ $t('common.save') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Add Schedule Dialog -->
        <el-dialog v-model="addScheduleDialogVisible" :title="$t('admin.addSchedule')">
            <el-form :model="newSchedule">
                <el-form-item :label="$t('admin.stylist')">
                    <el-select v-model="newSchedule.stylistId" :placeholder="$t('admin.selectStylist')">
                        <el-option v-for="stylist in stylists" :key="stylist.id" :label="stylist.name"
                            :value="stylist.id" />
                    </el-select>
                </el-form-item>
                <el-form-item :label="$t('admin.dateRange')">
                    <el-date-picker v-model="newSchedule.dateRange" type="datetimerange"
                        :range-separator="$t('admin.to')" :start-placeholder="$t('admin.startDate')"
                        :end-placeholder="$t('admin.endDate')" format="YYYY-MM-DD HH:mm" />
                </el-form-item>
                <el-form-item :label="$t('admin.reason')">
                    <el-input v-model="newSchedule.reason" :placeholder="$t('admin.reasonPlaceholder')" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="addScheduleDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="addSchedule">{{ $t('common.add') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Store Closed Dialog -->
        <el-dialog v-model="storeClosedDialogVisible" :title="$t('admin.storeClosed')">
            <el-form :model="storeClosedSchedule">
                <el-form-item :label="$t('admin.dateRange')">
                    <el-date-picker v-model="storeClosedSchedule.dateRange" type="datetimerange"
                        :range-separator="$t('admin.to')" :start-placeholder="$t('admin.startDate')"
                        :end-placeholder="$t('admin.endDate')" format="YYYY-MM-DD HH:mm" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="storeClosedDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="addStoreClosedSchedule">{{ $t('common.add') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Edit Stylist Dialog -->
        <el-dialog v-model="editDialogVisible" :title="$t('admin.editStylist')">
            <el-form :model="editingStylist">
                <el-form-item :label="$t('admin.name')">
                    <el-input v-model="editingStylist.name" />
                </el-form-item>
                <el-form-item :label="$t('admin.image')">
                    <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
                        :on-success="handleEditAvatarSuccess" :before-upload="beforeAvatarUpload">
                        <img v-if="editingStylist.avatarUrl" :src="getFullImageUrl(editingStylist.avatarUrl)"
                            class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="updateStylist">{{ $t('common.save') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Appointment Detail Dialog -->
        <el-dialog v-model="appointmentDetailVisible" :title="$t('admin.appointmentDetails')" width="400px">
            <div v-if="selectedAppointment">
                <p><strong>{{ $t('admin.customerName') }}:</strong> {{ selectedAppointment.customerName }}</p>
                <p><strong>{{ $t('admin.customerPhone') }}:</strong> {{ selectedAppointment.customerPhone }}</p>
                <p><strong>{{ $t('admin.appointmentTime') }}:</strong> {{ formatTime(selectedAppointment.startTime) }}
                </p>
                <p><strong>{{ $t('admin.serviceName') }}:</strong> {{ selectedAppointment.serviceName }}</p>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="appointmentDetailVisible = false">{{ $t('common.close') }}</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'
import { config } from '../config'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const userStore = useUserStore()
const activeTab = ref('appointments')
const appointments = ref([])
const stylists = ref([])
const services = ref([])
const searchQuery = ref('')
const serviceSearchQuery = ref('')
const exportDateRange = ref([])

// Schedule State
const addScheduleDialogVisible = ref(false)
const storeClosedDialogVisible = ref(false)
const newSchedule = ref({
    stylistId: null,
    dateRange: [],
    isAllDay: false,
    reason: ''
})
const storeClosedSchedule = ref({
    dateRange: [],
    isAllDay: true,
    reason: ''
})
const schedules = ref([])

const handleScheduleEventClick = async (info) => {
    if (confirm(t('admin.deleteScheduleConfirm'))) {
        try {
            await axios.delete(`${config.apiBaseUrl}/api/schedules/${info.event.id}`)
            ElMessage.success(t('admin.scheduleDeleted'))
            await fetchSchedules()
        } catch (error) {
            console.error('Failed to delete schedule', error)
            ElMessage.error(t('common.error'))
        }
    }
}

const scheduleCalendarOptions = ref({
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek'
    },
    events: [],
    height: 'auto',
    eventClick: handleScheduleEventClick,
    eventColor: '#E6A23C' // Default color
})

// Appointment Detail State
const appointmentDetailVisible = ref(false)
const selectedAppointment = ref(null)

const handleEventClick = (info) => {
    const props = info.event.extendedProps
    selectedAppointment.value = {
        customerName: props.customerName,
        customerPhone: props.customerPhone,
        startTime: info.event.start,
        serviceName: props.serviceName
    }
    appointmentDetailVisible.value = true
}

const calendarOptions = ref({
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek'
    },
    events: [],
    height: 'auto',
    slotMinTime: '09:00:00',
    slotMaxTime: '22:00:00',
    allDaySlot: false,
    eventClick: handleEventClick
})

const newStylist = ref({
    name: '',
    avatarUrl: ''
})

const newService = ref({
    name: '',
    price: 0,
    durationHours: 1.0,
    isPriceStartingFrom: false
})

// Edit Dialog State
const editDialogVisible = ref(false)
const editingStylist = ref({
    id: null,
    name: '',
    avatarUrl: ''
})

const editServiceDialogVisible = ref(false)
const editingService = ref({
    id: null,
    name: '',
    price: 0,
    durationHours: 1.0,
    isPriceStartingFrom: false
})

const uploadUrl = `${config.apiBaseUrl}/api/upload`

const filteredStylists = computed(() => {
    if (!searchQuery.value) return stylists.value
    return stylists.value.filter(stylist =>
        stylist.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
})

const filteredServices = computed(() => {
    if (!serviceSearchQuery.value) return services.value
    return services.value.filter(service =>
        service.name.toLowerCase().includes(serviceSearchQuery.value.toLowerCase())
    )
})

onMounted(async () => {
    if (userStore.dbUser?.role === 'ADMIN') {
        await fetchAppointments()
        await fetchStylists()
        await fetchServices()
        await fetchSchedules()
    }
})

const fetchSchedules = async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/schedules`)
        schedules.value = response.data

        const events = response.data.map(sch => ({
            id: sch.id,
            title: `${sch.stylist ? sch.stylist.name : t('admin.storeClosed')} - ${sch.reason || t('admin.leave')}`,
            start: sch.startTime,
            end: sch.endTime,
            allDay: sch.isAllDay,
            color: sch.stylist ? '#E6A23C' : '#F56C6C' // Orange for stylist, Red for store closed
        }))
        scheduleCalendarOptions.value.events = events
    } catch (error) {
        console.error('Failed to fetch schedules', error)
    }
}

const openAddScheduleDialog = () => {
    newSchedule.value = {
        stylistId: null,
        dateRange: [],
        isAllDay: false,
        reason: ''
    }
    addScheduleDialogVisible.value = true
}

const openStoreClosedDialog = () => {
    storeClosedSchedule.value = {
        dateRange: [],
        isAllDay: false,
        reason: t('admin.storeClosedDefaultReason')
    }
    storeClosedDialogVisible.value = true
}

const toLocalISOString = (date) => {
    const tzOffset = date.getTimezoneOffset() * 60000
    return new Date(date.getTime() - tzOffset).toISOString().slice(0, 19)
}

const addStoreClosedSchedule = async () => {
    if (!storeClosedSchedule.value.dateRange || storeClosedSchedule.value.dateRange.length !== 2) {
        ElMessage.warning(t('admin.selectDateRange'))
        return
    }

    let start = storeClosedSchedule.value.dateRange[0]
    let end = storeClosedSchedule.value.dateRange[1]

    try {
        await axios.post(`${config.apiBaseUrl}/api/schedules`, {
            stylistId: null, // Null means Global/Store Closed
            startTime: toLocalISOString(start),
            endTime: toLocalISOString(end),
            isAllDay: false,
            reason: t('admin.storeClosedDefaultReason')
        })
        ElMessage.success(t('admin.storeClosedAdded'))
        storeClosedDialogVisible.value = false
        await fetchSchedules()
    } catch (error) {
        console.error('Failed to add store closed schedule', error)
        ElMessage.error(t('common.error'))
    }
}

const addSchedule = async () => {
    if (!newSchedule.value.stylistId || !newSchedule.value.dateRange || newSchedule.value.dateRange.length !== 2) {
        ElMessage.warning(t('admin.fillAllFields'))
        return
    }

    let start = newSchedule.value.dateRange[0]
    let end = newSchedule.value.dateRange[1]

    try {
        await axios.post(`${config.apiBaseUrl}/api/schedules`, {
            stylistId: newSchedule.value.stylistId,
            startTime: toLocalISOString(start),
            endTime: toLocalISOString(end),
            isAllDay: false,
            reason: newSchedule.value.reason
        })
        ElMessage.success(t('admin.scheduleAdded'))
        addScheduleDialogVisible.value = false
        await fetchSchedules()
    } catch (error) {
        console.error('Failed to add schedule', error)
        ElMessage.error(t('common.error'))
    }
}

const fetchAppointments = async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/appointments`)
        appointments.value = response.data

        const events = response.data.map(appt => ({
            id: appt.id,
            title: `${appt.customer.realName || appt.customer.displayName} - ${appt.service.name}`,
            start: appt.startTime,
            end: appt.endTime,
            backgroundColor: getStatusColor(appt.status),
            extendedProps: {
                customerName: appt.customer.realName || appt.customer.displayName,
                customerPhone: appt.customer.phone,
                serviceName: appt.service.name
            }
        }))
        calendarOptions.value.events = events
    } catch (error) {
        console.error('Failed to fetch appointments', error)
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'BOOKED': return '#67C23A'
        case 'CANCELLED': return '#F56C6C'
        case 'COMPLETED': return '#909399'
        default: return '#409EFF'
    }
}

const formatTime = (timeStr) => {
    return new Date(timeStr).toLocaleString()
}

const exportExcel = async () => {
    if (!exportDateRange.value || exportDateRange.value.length !== 2) {
        ElMessage.warning(t('admin.selectDateRange'))
        return
    }
    // Format dates to ISO string but keep local time if possible or handle timezone
    // The backend expects LocalDateTime, so ISO string is usually fine if backend parses it correctly.
    // However, standard ISO string is UTC. Let's send it and see.
    // Actually, Element Plus date picker returns Date objects.

    const start = exportDateRange.value[0].toISOString()
    const end = exportDateRange.value[1].toISOString()

    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/appointments/export`, {
            params: { start, end },
            responseType: 'blob'
        })

        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'appointments.xlsx')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
    } catch (error) {
        console.error('Export failed', error)
        ElMessage.error(t('common.error'))
    }
}

const fetchStylists = async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/stylists`)
        stylists.value = response.data
    } catch (error) {
        console.error('Failed to fetch stylists', error)
        ElMessage.error(t('common.error'))
    }
}

const fetchServices = async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/services`)
        services.value = response.data
    } catch (error) {
        console.error('Failed to fetch services', error)
        ElMessage.error(t('common.error'))
    }
}

const addService = async () => {
    if (!newService.value.name) {
        ElMessage.warning(t('admin.fillServiceName'))
        return
    }
    if (!newService.value.price) {
        ElMessage.warning(t('admin.fillServicePrice'))
        return
    }
    try {
        await axios.post(`${config.apiBaseUrl}/api/services`, newService.value)
        ElMessage.success(t('admin.serviceAdded'))
        newService.value = { name: '', price: 0, durationHours: 1.0, isPriceStartingFrom: false }
        await fetchServices()
    } catch (error) {
        console.error('Failed to add service', error)
        ElMessage.error(t('common.error'))
    }
}

const openEditServiceDialog = (service) => {
    editingService.value = { ...service }
    editServiceDialogVisible.value = true
}

const updateService = async () => {
    if (!editingService.value.name) {
        ElMessage.warning(t('admin.fillServiceName'))
        return
    }
    try {
        await axios.put(`${config.apiBaseUrl}/api/services/${editingService.value.id}`, editingService.value)
        ElMessage.success(t('admin.serviceUpdated'))
        editServiceDialogVisible.value = false
        await fetchServices()
    } catch (error) {
        console.error('Failed to update service', error)
        ElMessage.error(t('common.error'))
    }
}

const deleteService = async (id) => {
    try {
        await axios.delete(`${config.apiBaseUrl}/api/services/${id}`)
        ElMessage.success(t('admin.serviceDeleted'))
        await fetchServices()
    } catch (error) {
        console.error('Failed to delete service', error)
        ElMessage.error(t('common.error'))
    }
}

const handleAvatarSuccess = (response, uploadFile) => {
    newStylist.value.avatarUrl = response.url
}

const handleEditAvatarSuccess = (response, uploadFile) => {
    editingStylist.value.avatarUrl = response.url
}

const beforeAvatarUpload = (rawFile) => {
    // Allow jpg and png
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error(t('admin.avatarFormatError'))
        return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error(t('admin.avatarSizeError'))
        return false
    }
    return true
}

const getFullImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    return `${config.apiBaseUrl}${url}`
}

const addStylist = async () => {
    if (!newStylist.value.name) {
        ElMessage.warning(t('admin.fillName'))
        return
    }
    try {
        await axios.post(`${config.apiBaseUrl}/api/stylists`, {
            name: newStylist.value.name,
            avatarUrl: newStylist.value.avatarUrl, // Can be empty
            specialty: 'All-Rounder' // Default value
        })
        ElMessage.success(t('admin.stylistAdded'))
        newStylist.value = { name: '', avatarUrl: '' }
        await fetchStylists()
    } catch (error) {
        console.error('Failed to add stylist', error)
        ElMessage.error(t('common.error'))
    }
}

const openEditDialog = (stylist) => {
    editingStylist.value = { ...stylist }
    editDialogVisible.value = true
}

const updateStylist = async () => {
    if (!editingStylist.value.name) {
        ElMessage.warning(t('admin.fillName'))
        return
    }
    try {
        await axios.put(`${config.apiBaseUrl}/api/stylists/${editingStylist.value.id}`, {
            name: editingStylist.value.name,
            avatarUrl: editingStylist.value.avatarUrl,
            specialty: 'All-Rounder'
        })
        ElMessage.success(t('admin.stylistUpdated'))
        editDialogVisible.value = false
        await fetchStylists()
    } catch (error) {
        console.error('Failed to update stylist', error)
        ElMessage.error(t('common.error'))
    }
}

const deleteStylist = async (id) => {
    try {
        await axios.delete(`${config.apiBaseUrl}/api/stylists/${id}`)
        ElMessage.success(t('admin.stylistDeleted'))
        await fetchStylists()
    } catch (error) {
        console.error('Failed to delete stylist', error)
        ElMessage.error(t('common.error'))
    }
}
</script>

<style scoped>
.admin-container {
    padding: 20px;
}

.stylist-management {
    margin-top: 20px;
}

.avatar-uploader .avatar {
    width: 100px;
    height: 100px;
    display: block;
    object-fit: cover;
    border-radius: 6px;
}

.avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
}
</style>
