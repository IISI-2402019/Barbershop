<template>
    <div class="admin-container">
        <h2>{{ $t('home.adminDashboard') }}</h2>
        <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('admin.appointments')" name="appointments">
                <div class="appointment-controls" style="margin-bottom: 20px;">
                    <el-select v-model="filterStylistId" :placeholder="$t('admin.selectStylist')"
                        style="margin-right: 10px; width: 200px;" clearable @change="fetchAppointments"
                        class="filter-item">
                        <el-option :label="$t('admin.allStylists')" :value="null" />
                        <el-option v-for="s in stylists" :key="s.id" :label="s.name" :value="s.id" />
                    </el-select>
                    <el-date-picker v-model="exportStartDate" type="datetime" :placeholder="$t('admin.startDate')"
                        style="margin-right: 5px;" class="date-picker-item" />
                    <span class="date-separator">{{ $t('admin.to') }}</span>
                    <el-date-picker v-model="exportEndDate" type="datetime" :placeholder="$t('admin.endDate')"
                        style="margin-right: 10px;" class="date-picker-item" />
                    <el-button type="success" @click="exportExcel" :loading="loadingExport">{{ $t('admin.exportExcel')
                        }}</el-button>
                </div>
                <FullCalendar ref="appointmentCalendarRef" :options="calendarOptions" />
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.personalSettings')" name="personal"
                v-if="userStore.dbUser?.role === 'STYLIST'">
                <div class="personal-settings">
                    <h3>{{ $t('admin.personalSettings') }}</h3>
                    <div v-if="!currentStylistProfile" style="padding: 20px;">
                        <el-skeleton :rows="3" animated />
                    </div>
                    <el-form v-else :model="currentStylistProfile" label-width="120px" style="max-width: 600px;">
                        <el-form-item :label="$t('admin.name')">
                            <el-input v-model="currentStylistProfile.name" />
                        </el-form-item>
                        <el-form-item :label="$t('admin.image')">
                            <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
                                :on-success="handlePersonalAvatarSuccess" :before-upload="beforeAvatarUpload">
                                <img v-if="currentStylistProfile.avatarUrl"
                                    :src="getFullImageUrl(currentStylistProfile.avatarUrl)" class="avatar" />
                                <el-icon v-else class="avatar-uploader-icon">
                                    <Plus />
                                </el-icon>
                            </el-upload>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="updatePersonalProfile"
                                :loading="loadingPersonalProfile">{{ $t('common.save') }}</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.schedule')" name="schedule">
                <div class="schedule-management">
                    <div class="schedule-controls" style="margin-bottom: 20px;">
                        <el-button type="primary" @click="openAddScheduleDialog">{{ $t('admin.addSchedule')
                        }}</el-button>
                    </div>
                    <FullCalendar ref="scheduleCalendarRef" :options="scheduleCalendarOptions" />
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
                            <el-button type="primary" @click="addService" :loading="loadingService">{{ $t('common.add')
                                }}</el-button>
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
                                <div class="action-buttons">
                                    <el-button size="small" @click="openEditServiceDialog(scope.row)">{{
                                        $t('common.edit')
                                        }}</el-button>
                                    <el-button size="small" type="danger" @click="deleteService(scope.row.id)">{{
                                        $t('common.delete') }}</el-button>
                                </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.users')" name="users">
                <div class="user-management">
                    <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
                        <el-input v-model="userSearchQuery" :placeholder="$t('admin.searchUser')" style="width: 250px;"
                            @keyup.enter="fetchUsers" clearable />
                        <el-select v-model="filterRole" :placeholder="$t('admin.filterByRole')" style="width: 150px;"
                            clearable @change="fetchUsers">
                            <el-option :label="$t('admin.allRoles')" :value="null" />
                            <el-option :label="$t('admin.customer')" value="CUSTOMER" />
                            <el-option :label="$t('admin.stylistRole')" value="STYLIST" />
                            <el-option :label="$t('admin.admin')" value="ADMIN" />
                        </el-select>
                        <el-button type="primary" @click="fetchUsers">{{ $t('common.search') }}</el-button>
                    </div>

                    <el-table :data="userList" style="width: 100%" v-loading="loadingUsers">
                        <el-table-column prop="realName" :label="$t('register.name')" min-width="100" />
                        <el-table-column prop="phone" :label="$t('register.phone')" min-width="120" />
                        <el-table-column :label="$t('admin.role')" min-width="180">
                            <template #default="scope">
                                <el-select v-model="scope.row.role" @change="updateUserRole(scope.row)"
                                    style="width: 100%">
                                    <el-option :label="$t('admin.customer')" value="CUSTOMER" />
                                    <el-option :label="$t('admin.stylistRole')" value="STYLIST" />
                                    <el-option :label="$t('admin.admin')" value="ADMIN" />
                                </el-select>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('admin.actions')" min-width="150">
                            <template #default="scope">
                                <el-button size="small" @click="openCustomerCard(scope.row)"
                                    v-if="scope.row.role === 'CUSTOMER'">
                                    {{ $t('admin.customerCard') }}
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-tab-pane>
            <el-tab-pane :label="$t('admin.settings')" name="settings"
                v-if="userStore.dbUser?.role === 'ADMIN' || userStore.dbUser?.role === 'STYLIST'">
                <div class="settings-management">
                    <h3>{{ $t('admin.businessHours') }}</h3>
                    <el-form :model="settingsForm" label-width="120px">
                        <el-form-item :label="$t('admin.businessHoursStart')">
                            <el-time-select v-model="settingsForm.business_hours_start" start="06:00" step="00:30"
                                end="23:00" :placeholder="$t('admin.businessHoursStart')" />
                        </el-form-item>
                        <el-form-item :label="$t('admin.businessHoursEnd')">
                            <el-time-select v-model="settingsForm.business_hours_end" start="06:00" step="00:30"
                                end="23:00" :placeholder="$t('admin.businessHoursEnd')" />
                        </el-form-item>
                        <el-form-item :label="$t('admin.weeklyOffDay')">
                            <el-select v-model="settingsForm.weekly_off_day" :placeholder="$t('admin.selectOffDay')"
                                clearable>
                                <el-option :label="$t('common.days.sun')" value="0" />
                                <el-option :label="$t('common.days.mon')" value="1" />
                                <el-option :label="$t('common.days.tue')" value="2" />
                                <el-option :label="$t('common.days.wed')" value="3" />
                                <el-option :label="$t('common.days.thu')" value="4" />
                                <el-option :label="$t('common.days.fri')" value="5" />
                                <el-option :label="$t('common.days.sat')" value="6" />
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="saveSettings" :loading="loadingSettings">{{
                                $t('common.save')
                                }}</el-button>
                        </el-form-item>
                    </el-form>

                    <el-divider />

                    <div class="store-closed-management">
                        <h3>{{ $t('admin.storeClosed') }}</h3>
                        <el-button type="danger" @click="openStoreClosedDialog">{{ $t('admin.addStoreClosed')
                            }}</el-button>
                    </div>
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
                    <el-button type="primary" @click="updateService" :loading="loadingService">{{ $t('common.save')
                        }}</el-button>
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
                    <el-button type="primary" @click="addSchedule" :loading="loadingSchedule">{{ isEditingSchedule ?
                        $t('common.save') :
                        $t('common.add') }}</el-button>
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
                    <el-button type="primary" @click="addStoreClosedSchedule" :loading="loadingStoreClosed">{{
                        isEditingSchedule ? $t('common.save') :
                        $t('common.add') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Schedule Detail Dialog -->
        <el-dialog v-model="scheduleDetailVisible" :title="$t('admin.scheduleDetails')" width="400px">
            <div v-if="selectedSchedule">
                <p><strong>{{ selectedSchedule.isStoreClosed ? $t('admin.storeClosed') : $t('admin.stylist')
                        }}:</strong>
                    {{ selectedSchedule.isStoreClosed ? '' : selectedSchedule.stylistName }}
                </p>
                <p><strong>{{ $t('admin.dateRange') }}:</strong><br />
                    {{ formatTime(selectedSchedule.start) }} - {{ formatTime(selectedSchedule.end) }}
                </p>
                <p><strong>{{ $t('admin.reason') }}:</strong> {{ selectedSchedule.reason || $t('admin.leave') }}</p>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button type="primary" @click="openEditSchedule"
                        v-if="userStore.dbUser?.role === 'ADMIN' || (selectedSchedule.stylistUserId && userStore.dbUser?.id === selectedSchedule.stylistUserId)">{{
                        $t('common.edit') }}</el-button>
                    <el-button type="danger" @click="deleteSchedule"
                        v-if="userStore.dbUser?.role === 'ADMIN' || (selectedSchedule.stylistUserId && userStore.dbUser?.id === selectedSchedule.stylistUserId)">{{
                            $t('common.delete') }}</el-button>
                    <el-button @click="scheduleDetailVisible = false">{{ $t('common.close') }}</el-button>
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
                    <el-button type="primary" @click="updateStylist" :loading="loadingStylist">{{ $t('common.save')
                        }}</el-button>
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
                <p><strong>{{ $t('admin.stylist') }}:</strong> {{ selectedAppointment.stylistName }}</p>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button type="danger" @click="cancelAppointment"
                        :disabled="selectedAppointment?.status === 'CANCELED'">
                        {{ $t('common.cancel') }}
                    </el-button>
                    <el-button type="primary" @click="openEditTimeDialog"
                        :disabled="selectedAppointment?.status === 'CANCELED'">
                        {{ $t('admin.editTime') }}
                    </el-button>
                    <el-button @click="appointmentDetailVisible = false">{{ $t('common.close') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Edit Time Dialog -->
        <el-dialog v-model="editTimeDialogVisible" :title="$t('admin.editTime')" width="400px">
            <el-form :model="editTimeForm" label-width="100px" label-position="top">
                <el-form-item :label="$t('booking.date')">
                    <el-date-picker v-model="editTimeForm.date" type="date" format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD" :placeholder="$t('booking.selectDate')" style="width: 100%;" />
                </el-form-item>
                <div style="display: flex; gap: 10px;">
                    <el-form-item :label="$t('admin.newStartTime')" style="flex: 1;">
                        <el-select v-model="editTimeForm.startTimeStr" :placeholder="$t('booking.selectTime')">
                            <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t" />
                        </el-select>
                    </el-form-item>
                    <el-form-item :label="$t('admin.newEndTime')" style="flex: 1;">
                        <el-select v-model="editTimeForm.endTimeStr" :placeholder="$t('booking.selectTime')">
                            <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t" />
                        </el-select>
                    </el-form-item>
                </div>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editTimeDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="updateAppointmentTime" :loading="loadingTimeUpdate">{{
                        $t('common.save') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- Customer Card Dialog -->
        <el-dialog v-model="customerCardDialogVisible" :title="$t('admin.customerCard')" width="600px">
            <el-form :model="customerCardForm" label-width="80px">
                <el-form-item :label="$t('admin.customerName')">
                    <strong>{{ customerCardForm.name }}</strong>
                </el-form-item>
                <el-form-item :label="$t('admin.content')">
                    <el-input v-model="customerCardForm.content" type="textarea" :rows="8"
                        :placeholder="$t('admin.contentPlaceholder')" />
                </el-form-item>
                <el-form-item :label="$t('admin.image')">
                    <el-upload v-model:file-list="customerCardFileList" :action="uploadUrl" list-type="picture-card"
                        :on-success="handleCustomerCardUploadSuccess" :on-remove="handleCustomerCardRemove"
                        :on-preview="handlePictureCardPreview" :before-upload="beforeAvatarUpload">
                        <el-icon>
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="customerCardDialogVisible = false">{{ $t('common.cancel') }}</el-button>
                    <el-button type="primary" @click="saveCustomerCard" :loading="loadingCustomerCard">{{
                        $t('common.save') }}</el-button>
                </span>
            </template>
        </el-dialog>

        <el-dialog v-model="previewVisible">
            <img style="width: 100%" :src="previewImageUrl" alt="Preview Image" />
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'
import { config } from '../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const userStore = useUserStore()
const activeTab = ref('appointments')
const scheduleCalendarRef = ref(null)
const appointmentCalendarRef = ref(null)

watch(activeTab, async (newTab) => {
    if (newTab === 'appointments') {
        if (stylists.value.length === 0) await fetchStylists()
        if (appointments.value.length === 0) await fetchAppointments()
        await nextTick()
        if (appointmentCalendarRef.value) {
            appointmentCalendarRef.value.getApi().updateSize()
        }
    } else if (newTab === 'stylists') {
        if (stylists.value.length === 0) await fetchStylists()
    } else if (newTab === 'schedule') {
        // Always fetch schedules to ensure calendar is up to date
        await fetchSchedules()
        if (stylists.value.length === 0) await fetchStylists() // Schedule needs stylists
        await nextTick()
        if (scheduleCalendarRef.value) {
            scheduleCalendarRef.value.getApi().updateSize()
        }
    } else if (newTab === 'services') {
        if (services.value.length === 0) await fetchServices()
    } else if (newTab === 'users') {
        await fetchUsers()
    } else if (newTab === 'personal') {
        if (stylists.value.length === 0) await fetchStylists()
        initPersonalProfile()
    } else if (newTab === 'settings') {
        await fetchSettings()
    }
})

const appointments = ref([])
const stylists = ref([])
const services = ref([])
const searchQuery = ref('')
const serviceSearchQuery = ref('')
const exportStartDate = ref('')
const exportEndDate = ref('')
const userSearchQuery = ref('')
const filterStylistId = ref(null) // Stylist Filter for Appointments
const filterRole = ref(null) // Role Filter for Users
const userList = ref([])
const loadingUsers = ref(false)

// Customer Card State
const customerCardDialogVisible = ref(false)
const loadingCustomerCard = ref(false)
const customerCardForm = ref({
    id: null,
    name: '',
    content: '',
    images: []
})
const customerCardFileList = ref([])
const previewVisible = ref(false)
const previewImageUrl = ref('')

const handleCustomerCardUploadSuccess = (response, uploadFile) => {
    // response is { url: '/api/uploads/...' }
    // We add it to the list (Element Plus handles display, we track URL)
    // Actually we just need to ensure the fileList is synced for save
}

const handleCustomerCardRemove = (uploadFile, uploadFiles) => {
    // Element Plus updates v-model:file-list automatically
}

const handlePictureCardPreview = (uploadFile) => {
    previewImageUrl.value = uploadFile.url
    previewVisible.value = true
}

// Loading states
const loadingStylist = ref(false)
const loadingService = ref(false)
const loadingSettings = ref(false)
const loadingExport = ref(false)
const loadingSchedule = ref(false)
const loadingStoreClosed = ref(false)

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

// Schedule Detail State
const scheduleDetailVisible = ref(false)
const selectedSchedule = ref(null)
const isEditingSchedule = ref(false)
const editingScheduleId = ref(null)

const handleScheduleEventClick = (info) => {
    const props = info.event.extendedProps
    selectedSchedule.value = {
        id: info.event.id,
        title: info.event.title,
        start: info.event.start,
        end: info.event.end,
        allDay: info.event.allDay,
        stylistId: props.stylistId,
        stylistName: props.stylistName,
        stylistUserId: props.stylistUserId,
        reason: props.reason,
        isStoreClosed: !props.stylistId
    }
    scheduleDetailVisible.value = true
}

const deleteSchedule = async () => {
    if (!selectedSchedule.value) return
    if (confirm(t('admin.deleteScheduleConfirm'))) {
        try {
            await axios.delete(`${config.apiBaseUrl}/api/schedules/${selectedSchedule.value.id}`)
            ElMessage.success(t('admin.scheduleDeleted'))
            scheduleDetailVisible.value = false
            await fetchSchedules()
        } catch (error) {
            console.error('Failed to delete schedule', error)
            ElMessage.error(t('common.error'))
        }
    }
}

const openEditSchedule = () => {
    scheduleDetailVisible.value = false
    isEditingSchedule.value = true
    editingScheduleId.value = selectedSchedule.value.id

    // Fix date range for picker (needs Date objects)
    const start = new Date(selectedSchedule.value.start)
    const end = new Date(selectedSchedule.value.end)

    if (selectedSchedule.value.isStoreClosed) {
        storeClosedSchedule.value = {
            dateRange: [start, end],
            isAllDay: selectedSchedule.value.allDay,
            reason: selectedSchedule.value.reason
        }
        storeClosedDialogVisible.value = true
    } else {
        newSchedule.value = {
            stylistId: selectedSchedule.value.stylistId,
            dateRange: [start, end],
            isAllDay: selectedSchedule.value.allDay,
            reason: selectedSchedule.value.reason
        }
        addScheduleDialogVisible.value = true
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
    eventClick: handleScheduleEventClick,
    eventColor: '#E6A23C', // Default color
    displayEventTime: false, // Hide time prefix like 12a
    slotMinTime: config.businessStartTime,
    slotMaxTime: config.businessEndTime,
    height: 'auto'
})

// Appointment Detail State
const appointmentDetailVisible = ref(false)
const selectedAppointment = ref(null)

const handleEventClick = (info) => {
    const props = info.event.extendedProps
    selectedAppointment.value = {
        id: info.event.id,
        customerName: props.customerName,
        customerPhone: props.customerPhone,
        startTime: info.event.start,
        endTime: info.event.end,
        serviceName: props.serviceName,
        stylistName: props.stylistName,
        status: props.status
    }
    appointmentDetailVisible.value = true
}

const cancelAppointment = async () => {
    if (!selectedAppointment.value) return

    try {
        await ElMessageBox.confirm(
            t('admin.cancelAppointmentConfirm'),
            t('common.warning'),
            {
                confirmButtonText: t('common.confirm'),
                cancelButtonText: t('common.cancel'),
                type: 'warning',
            }
        )

        await axios.delete(`${config.apiBaseUrl}/api/appointments/${selectedAppointment.value.id}`)
        ElMessage.success(t('admin.appointmentCanceled'))
        appointmentDetailVisible.value = false
        await fetchAppointments()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('Failed to cancel appointment', error)
            ElMessage.error(t('common.error'))
        }
    }
}

// Edit Time Logic
const editTimeDialogVisible = ref(false)
const loadingTimeUpdate = ref(false)
const timeOptions = ref([]) // Generated 30-min slots
const editTimeForm = ref({
    date: '',
    startTimeStr: '',
    endTimeStr: ''
})

const generateTimeOptions = () => {
    const slots = []
    const start = parseInt(config.businessStartTime.split(':')[0])
    const end = parseInt(config.businessEndTime.split(':')[0])

    for (let h = start; h <= end; h++) {
        const hStr = h.toString().padStart(2, '0')
        slots.push(`${hStr}:00`)
        // Include half-hours
        slots.push(`${hStr}:30`)
    }
    timeOptions.value = slots
}

const openEditTimeDialog = () => {
    // Hide detail dialog
    appointmentDetailVisible.value = false
    generateTimeOptions()

    // Init form with ISO strings
    const start = selectedAppointment.value.startTime
    const end = selectedAppointment.value.endTime || new Date(start.getTime() + 60 * 60 * 1000)

    const toLocalISO = (date) => {
        const offset = date.getTimezoneOffset() * 60000
        return new Date(date.getTime() - offset).toISOString().slice(0, 19)
    }

    // Split date and time for easier editing
    editTimeForm.value = {
        date: toLocalISO(start).split('T')[0],
        startTimeStr: toLocalISO(start).split('T')[1].slice(0, 5), // HH:mm
        endTimeStr: toLocalISO(end).split('T')[1].slice(0, 5) // HH:mm
    }

    editTimeDialogVisible.value = true
}

const updateAppointmentTime = async () => {
    if (!editTimeForm.value.date || !editTimeForm.value.startTimeStr || !editTimeForm.value.endTimeStr) {
        ElMessage.error(t('common.fillAllFields'))
        return
    }

    // Combine Date and Time
    const startTimeFull = `${editTimeForm.value.date}T${editTimeForm.value.startTimeStr}:00`
    const endTimeFull = `${editTimeForm.value.date}T${editTimeForm.value.endTimeStr}:00`

    try {
        await ElMessageBox.confirm(
            t('admin.updateTimeConfirm'),
            t('common.warning'),
            {
                confirmButtonText: t('common.confirm'),
                cancelButtonText: t('common.cancel'),
                type: 'warning',
            }
        )

        loadingTimeUpdate.value = true
        await axios.put(`${config.apiBaseUrl}/api/appointments/${selectedAppointment.value.id}/time`, {
            startTime: startTimeFull,
            endTime: endTimeFull
        })

        ElMessage.success(t('admin.timeUpdated'))
        editTimeDialogVisible.value = false
        await fetchAppointments()

    } catch (error) {
        if (error !== 'cancel') {
            console.error('Failed to update time', error)
            if (error.response?.data) {
                ElMessage.error(error.response.data)
            } else {
                ElMessage.error(t('common.error'))
            }
        }
    } finally {
        loadingTimeUpdate.value = false
    }
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
    slotMinTime: config.businessStartTime,
    slotMaxTime: config.businessEndTime,
    allDaySlot: false,
    eventClick: handleEventClick,
    height: 'auto'
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


const fetchUsers = async () => {
    loadingUsers.value = true
    try {
        const res = await axios.get(`${config.apiBaseUrl}/api/users`, {
            params: {
                query: userSearchQuery.value,
                role: filterRole.value
            }
        })
        userList.value = res.data
    } catch (e) {
        console.error(e)
        ElMessage.error(t('common.error'))
    } finally {
        loadingUsers.value = false
    }
}

const updateUserRole = async (user) => {
    try {
        await axios.put(`${config.apiBaseUrl}/api/users/${user.id}/role`, {
            role: user.role
        })
        ElMessage.success(t('admin.roleUpdated'))
    } catch (e) {
        console.error(e)
        ElMessage.error(t('common.error'))
        // Refresh list to revert change if failed
        fetchUsers()
    }
}

// Settings Logic
const settingsForm = ref({
    business_hours_start: '10:00',
    business_hours_end: '20:00',
    weekly_off_day: null
})

const fetchSettings = async () => {
    try {
        const res = await axios.get(`${config.apiBaseUrl}/api/settings`)
        if (res.data && Object.keys(res.data).length > 0) {
            settingsForm.value = { ...settingsForm.value, ...res.data }
            // Update calendar options
            const start = settingsForm.value.business_hours_start
            const end = settingsForm.value.business_hours_end

            calendarOptions.value.slotMinTime = start
            calendarOptions.value.slotMaxTime = end
            scheduleCalendarOptions.value.slotMinTime = start
            scheduleCalendarOptions.value.slotMaxTime = end

            // Update hidden days
            const weeklyOff = settingsForm.value.weekly_off_day
            const hiddenDays = (weeklyOff !== null && weeklyOff !== undefined && weeklyOff !== '') ? [parseInt(weeklyOff)] : []
            calendarOptions.value.hiddenDays = hiddenDays
            scheduleCalendarOptions.value.hiddenDays = hiddenDays
        }
    } catch (e) {
        console.error('Failed to fetch settings', e)
    }
}

const saveSettings = async () => {
    loadingSettings.value = true
    try {
        await axios.post(`${config.apiBaseUrl}/api/settings`, settingsForm.value)
        ElMessage.success(t('common.success'))
        const start = settingsForm.value.business_hours_start
        const end = settingsForm.value.business_hours_end

        calendarOptions.value.slotMinTime = start
        calendarOptions.value.slotMaxTime = end
        scheduleCalendarOptions.value.slotMinTime = start
        scheduleCalendarOptions.value.slotMaxTime = end

        // Update hidden days
        const weeklyOff = settingsForm.value.weekly_off_day
        const hiddenDays = (weeklyOff !== null && weeklyOff !== undefined && weeklyOff !== '') ? [parseInt(weeklyOff)] : []
        calendarOptions.value.hiddenDays = hiddenDays
        scheduleCalendarOptions.value.hiddenDays = hiddenDays
    } catch (e) {
        ElMessage.error(t('common.error'))
    } finally {
        loadingSettings.value = false
    }
}


// Personal Settings Logic
const currentStylistProfile = ref(null)
const loadingPersonalProfile = ref(false)

const initPersonalProfile = () => {
    const myStylist = stylists.value.find(s => s.user && s.user.id === userStore.dbUser.id)
    if (myStylist) {
        currentStylistProfile.value = { ...myStylist }
    } else {
        // Fallback: This shouldn't happen if backend created the stylist record on role change,
        // but if it's missing, maybe show a "Create Profile" form or just pre-fill from user
        currentStylistProfile.value = {
            id: null,
            name: userStore.dbUser.realName || userStore.dbUser.displayName,
            avatarUrl: null
        }
    }
}

const handlePersonalAvatarSuccess = (res) => {
    currentStylistProfile.value.avatarUrl = res.url
}

const updatePersonalProfile = async () => {
    loadingPersonalProfile.value = true
    try {
        if (currentStylistProfile.value.id) {
            await axios.put(`${config.apiBaseUrl}/api/stylists/${currentStylistProfile.value.id}`, currentStylistProfile.value)
            ElMessage.success(t('common.success'))
        } else {
            // Should not really be here if logic is consistent, but safeguard:
            // If it's a new stylist record (somehow missing), Create it
            const res = await axios.post(`${config.apiBaseUrl}/api/stylists`, currentStylistProfile.value)
            currentStylistProfile.value = res.data
            ElMessage.success(t('common.success'))
        }
        await fetchStylists() // Refresh global list
    } catch (e) {
        console.error(e)
        ElMessage.error(t('common.error'))
    } finally {
        loadingPersonalProfile.value = false
    }
}

onMounted(async () => {
    // Note: userStore.dbUser might vary depending on whether it's hydrated from pinia-persistedstate or initialized.
    // However, usually on page reload, pinia state hydration happens synchronously or very quickly.
    // If userStore is not ready, we might need to watch it.

    // Force fetch defaults if user is admin
    if (userStore.dbUser?.role === 'ADMIN' || userStore.dbUser?.role === 'STYLIST') {
        fetchSettings()

        if (userStore.dbUser.role === 'STYLIST') {
            await fetchStylists()
            const myStylist = stylists.value.find(s => s.user && s.user.id === userStore.dbUser.id)
            if (myStylist) {
                filterStylistId.value = myStylist.id
            }
        } else {
            fetchStylists()
        }

        // Always fetch appointments for the default tab
        fetchAppointments()
    }
})

// Watch for user login state changes in case it loads late
watch(() => userStore.dbUser, async (newUser) => {
    if (newUser?.role === 'ADMIN' || newUser?.role === 'STYLIST') {
        fetchSettings()
        if (newUser.role === 'STYLIST') {
            await fetchStylists()
            const myStylist = stylists.value.find(s => s.user && s.user.id === newUser.id)
            if (myStylist) {
                filterStylistId.value = myStylist.id
            }
        } else {
            fetchStylists()
        }
        if (activeTab.value === 'appointments') {
            fetchAppointments()
        }
    }
})

const getStylistColor = (stylistName) => {
    if (!stylistName) return '#F56C6C' // Red for store closed or unknown
    const colors = ['#FF8F8F', '#FFF1CB', '#C2E2FA', '#B7A3E3']
    let hash = 0;
    for (let i = 0; i < stylistName.length; i++) {
        hash = stylistName.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}

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
            color: sch.stylist ? getStylistColor(sch.stylist.name) : '#F56C6C', // Dynamic color for stylist, Red for store closed
            extendedProps: {
                stylistId: sch.stylist ? sch.stylist.id : null,
                stylistName: sch.stylist ? sch.stylist.name : null,
                stylistUserId: (sch.stylist && sch.stylist.user) ? sch.stylist.user.id : null,
                reason: sch.reason
            }
        }))
        scheduleCalendarOptions.value.events = events
    } catch (error) {
        console.error('Failed to fetch schedules', error)
    }
}

const openAddScheduleDialog = () => {
    isEditingSchedule.value = false
    editingScheduleId.value = null
    newSchedule.value = {
        stylistId: null,
        dateRange: [],
        isAllDay: false,
        reason: ''
    }
    addScheduleDialogVisible.value = true
}

const openStoreClosedDialog = () => {
    isEditingSchedule.value = false
    editingScheduleId.value = null
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

    loadingStoreClosed.value = true
    try {
        const payload = {
            stylistId: null, // Null means Global/Store Closed
            startTime: toLocalISOString(start),
            endTime: toLocalISOString(end),
            isAllDay: false,
            reason: t('admin.storeClosedDefaultReason')
        }

        if (isEditingSchedule.value) {
            await axios.put(`${config.apiBaseUrl}/api/schedules/${editingScheduleId.value}`, payload)
            ElMessage.success(t('admin.scheduleUpdated'))
        } else {
            await axios.post(`${config.apiBaseUrl}/api/schedules`, payload)
            ElMessage.success(t('admin.storeClosedAdded'))
        }

        storeClosedDialogVisible.value = false
        await fetchSchedules()
    } catch (error) {
        console.error('Failed to save store closed schedule', error)
        ElMessage.error(t('common.error'))
    } finally {
        loadingStoreClosed.value = false
    }
}

const addSchedule = async () => {
    if (!newSchedule.value.stylistId || !newSchedule.value.dateRange || newSchedule.value.dateRange.length !== 2) {
        ElMessage.warning(t('admin.fillAllFields'))
        return
    }

    let start = newSchedule.value.dateRange[0]
    let end = newSchedule.value.dateRange[1]

    loadingSchedule.value = true
    try {
        const payload = {
            stylistId: newSchedule.value.stylistId,
            startTime: toLocalISOString(start),
            endTime: toLocalISOString(end),
            isAllDay: false,
            reason: newSchedule.value.reason
        }

        if (isEditingSchedule.value) {
            await axios.put(`${config.apiBaseUrl}/api/schedules/${editingScheduleId.value}`, payload)
            ElMessage.success(t('admin.scheduleUpdated'))
        } else {
            await axios.post(`${config.apiBaseUrl}/api/schedules`, payload)
            ElMessage.success(t('admin.scheduleAdded'))
        }

        addScheduleDialogVisible.value = false
        await fetchSchedules()
    } catch (error) {
        console.error('Failed to save schedule', error)
        ElMessage.error(t('common.error'))
    } finally {
        loadingSchedule.value = false
    }
}

const fetchAppointments = async () => {
    try {
        const params = {}
        if (filterStylistId.value) {
            params.stylistId = filterStylistId.value
        }
        const response = await axios.get(`${config.apiBaseUrl}/api/appointments`, { params })
        appointments.value = response.data

        const events = response.data.map(appt => {
            const isCancelled = appt.status === 'CANCELED';
            return {
                id: appt.id,
                title: `${isCancelled ? '[已取消] ' : ''}${appt.customer.realName || appt.customer.displayName} - ${appt.service.name} (${appt.stylist.name})`,
                start: appt.startTime,
                end: appt.endTime,
                backgroundColor: isCancelled ? '#909399' : getStylistColor(appt.stylist.name),
                borderColor: isCancelled ? '#909399' : getStylistColor(appt.stylist.name),
                extendedProps: {
                    customerName: appt.customer.realName || appt.customer.displayName,
                    customerPhone: appt.customer.phone,
                    serviceName: appt.service.name,
                    stylistName: appt.stylist.name,
                    status: appt.status
                }
            }
        })
        calendarOptions.value.events = events
    } catch (error) {
        console.error('Failed to fetch appointments', error)
    }
}

const getStatusColor = (status) => {
    switch (status) {
        case 'BOOKED': return '#67C23A'
        case 'CANCELED': return '#F56C6C'
        case 'COMPLETED': return '#909399'
        default: return '#409EFF'
    }
}

const formatTime = (timeStr) => {
    return new Date(timeStr).toLocaleString()
}

const exportExcel = async () => {
    if (!exportStartDate.value || !exportEndDate.value) {
        ElMessage.warning(t('admin.selectDateRange'))
        return
    }
    // Format dates to ISO string but keep local time if possible or handle timezone
    // The backend expects LocalDateTime, so ISO string is usually fine if backend parses it correctly.
    // However, standard ISO string is UTC. Let's send it and see.
    // Actually, Element Plus date picker returns Date objects.

    const start = new Date(exportStartDate.value).toISOString()
    const end = new Date(exportEndDate.value).toISOString()

    loadingExport.value = true
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
    } finally {
        loadingExport.value = false
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
    loadingService.value = true
    try {
        await axios.post(`${config.apiBaseUrl}/api/services`, newService.value)
        ElMessage.success(t('admin.serviceAdded'))
        newService.value = { name: '', price: 0, durationHours: 1.0, isPriceStartingFrom: false }
        await fetchServices()
    } catch (error) {
        console.error('Failed to add service', error)
        ElMessage.error(t('common.error'))
    } finally {
        loadingService.value = false
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
    loadingService.value = true
    try {
        await axios.put(`${config.apiBaseUrl}/api/services/${editingService.value.id}`, editingService.value)
        ElMessage.success(t('admin.serviceUpdated'))
        editServiceDialogVisible.value = false
        await fetchServices()
    } catch (error) {
        console.error('Failed to update service', error)
        ElMessage.error(t('common.error'))
    } finally {
        loadingService.value = false
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
    loadingStylist.value = true
    try {
        await axios.post(`${config.apiBaseUrl}/api/stylists`, {
            name: newStylist.value.name,
            avatarUrl: newStylist.value.avatarUrl // Can be empty
        })
        ElMessage.success(t('admin.stylistAdded'))
        newStylist.value = { name: '', avatarUrl: '' }
        await fetchStylists()
    } catch (error) {
        console.error('Failed to add stylist', error)
        ElMessage.error(t('common.error'))
    } finally {
        loadingStylist.value = false
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
    loadingStylist.value = true
    try {
        await axios.put(`${config.apiBaseUrl}/api/stylists/${editingStylist.value.id}`, {
            name: editingStylist.value.name,
            avatarUrl: editingStylist.value.avatarUrl
        })
        ElMessage.success(t('admin.stylistUpdated'))
        editDialogVisible.value = false
        await fetchStylists()
    } catch (error) {
        console.error('Failed to update stylist', error)
        ElMessage.error(t('common.error'))
    } finally {
        loadingStylist.value = false
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

const openCustomerCard = (user) => {
    let images = []
    try {
        if (user.customerCardImages) {
            images = JSON.parse(user.customerCardImages)
        }
    } catch (e) {
        console.error("Failed to parse images", e)
    }

    customerCardForm.value = {
        id: user.id,
        name: user.realName || user.displayName,
        content: user.customerCardContent || '',
        images: images
    }

    // Convert to Element Plus FileList format
    customerCardFileList.value = images.map(url => ({
        name: url.split('/').pop(),
        url: getFullImageUrl(url)
    }))

    customerCardDialogVisible.value = true
}

const saveCustomerCard = async () => {
    loadingCustomerCard.value = true
    try {
        // Extract URLs from fileList
        // If it's a new upload, response is in .response.url, otherwise .url
        const finalImages = customerCardFileList.value.map(f => {
            if (f.response && f.response.url) return f.response.url
            // If url is full (http...), need to strip base url if backend expects relative? 
            // Our backend stores whatever we send. But getFullImageUrl prepended base.
            // Let's see. If we used getFullImageUrl previously, the url in fileList has http.
            // We should probably strip it if we want relative storage, or just store what we have.
            // For consistency: store relative paths if they are from our server.

            // If getFullImageUrl used apiBaseUrl, let's strip it.
            if (f.url.startsWith(config.apiBaseUrl)) {
                return f.url.substring(config.apiBaseUrl.length)
            }
            return f.url
        })

        await axios.put(`${config.apiBaseUrl}/api/users/${customerCardForm.value.id}/card`, {
            content: customerCardForm.value.content,
            images: finalImages
        })
        ElMessage.success(t('admin.cardUpdated'))
        customerCardDialogVisible.value = false
        // Update local list
        const u = userList.value.find(u => u.id === customerCardForm.value.id)
        if (u) {
            u.customerCardContent = customerCardForm.value.content
            u.customerCardImages = JSON.stringify(finalImages)
        }
    } catch (e) {
        console.error(e)
        ElMessage.error(t('common.error'))
    } finally {
        loadingCustomerCard.value = false
    }
}
</script>

<style scoped>
.admin-container {
    padding: 20px;
}

@media (max-width: 480px) {
    .admin-container {
        padding: 20px 16px;
    }
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

.action-buttons {
    display: flex;
    gap: 5px;
    justify-content: flex-start;
    flex-wrap: wrap;
}

@media (max-width: 768px) {
    .action-buttons {
        flex-direction: column;
        align-items: flex-start;
        gap: 5px;
    }

    .action-buttons .el-button {
        margin-left: 0 !important;
        /* Override Element Plus margin */
        width: 100%;
        margin-bottom: 5px;
    }
}

.appointment-controls {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
}

@media (max-width: 480px) {
    .appointment-controls {
        flex-direction: column;
        align-items: stretch;
    }

    .appointment-controls .el-date-editor {
        width: 100% !important;
        margin-right: 0 !important;
        margin-bottom: 5px;
    }

    .appointment-controls .date-separator {
        text-align: center;
        display: block;
        margin-bottom: 5px;
    }

    .appointment-controls button {
        width: 100%;
    }
}
</style>
