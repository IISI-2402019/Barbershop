<template>
    <div class="profile-container">
        <h2>{{ $t('profile.title') }}</h2>
        <div v-if="loading" class="loading-state">{{ $t('common.loading') }}</div>
        <el-form v-else :model="form" label-width="120px" label-position="top">

            <!-- Real Name -->
            <el-form-item :label="$t('register.name')" required>
                <el-input v-model="form.realName" :placeholder="$t('register.name')" />
            </el-form-item>

            <!-- Phone -->
            <el-form-item :label="$t('register.phone')" required>
                <el-input v-model="form.phone" :placeholder="$t('register.phone')" maxlength="10" />
            </el-form-item>

            <!-- Reminder Cycle -->
            <el-form-item :label="$t('profile.reminderCycle')">
                <el-input-number v-model="form.reminderCycle" :min="0" />
                <div class="help-text">{{ $t('profile.reminderCycleHelp') }}</div>
            </el-form-item>

            <!-- Actions -->
            <el-form-item>
                <el-button type="primary" @click="saveProfile" :loading="saving">{{ $t('common.save') }}</el-button>
                <el-button @click="$router.push('/')">{{ $t('common.cancel') }}</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { config } from '../config'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()
const router = useRouter()

const loading = ref(true)
const saving = ref(false)

const form = ref({
    realName: '',
    phone: '',
    reminderCycle: null // null or 0 means disabled
})

onMounted(async () => {
    if (!userStore.isLoggedIn) {
        router.push('/')
        return
    }

    // Fetch latest user data
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/users/${userStore.dbUser.id}`)
        const user = response.data
        form.value.realName = user.realName || ''
        form.value.phone = user.phone || ''
        form.value.reminderCycle = user.reminderCycle
    } catch (e) {
        console.error("Failed to fetch profile", e)
        ElMessage.error(t('common.error'))
    } finally {
        loading.value = false
    }
})

const saveProfile = async () => {
    if (!form.value.realName || !form.value.phone) {
        ElMessage.warning(t('register.missingFields'))
        return
    }

    if (!/^09\d{8}$/.test(form.value.phone)) {
        ElMessage.warning(t('register.invalidPhone'))
        return
    }

    try {
        saving.value = true
        const payload = {
            realName: form.value.realName,
            phone: form.value.phone,
            reminderCycle: form.value.reminderCycle || null
        }

        await axios.put(`${config.apiBaseUrl}/api/users/${userStore.dbUser.id}/complete-profile`, payload)

        // Update store
        const updatedUser = { ...userStore.dbUser, ...payload }
        userStore.setProfile(updatedUser) // Warning: this might need a proper store action to update dbUser

        ElMessage.success(t('common.success'))
        router.push('/')
    } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data || t('common.error'))
    } finally {
        saving.value = false
    }
}
</script>

<style scoped>
.profile-container {
    max-width: 500px;
    margin: 40px auto;
    padding: 20px;
}

@media (max-width: 480px) {
    .profile-container {
        padding: 20px 16px;
        margin: 20px auto;
    }
}

.help-text {
    font-size: 12px;
    color: #888;
    line-height: 1.4;
    margin-top: 5px;
}
</style>
