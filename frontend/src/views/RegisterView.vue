<template>
    <div class="register-container">
        <h2>{{ $t('register.title') }}</h2>
        <div v-if="userStore.isLoading">{{ $t('common.loading') }}</div>
        <el-form v-else-if="userStore.dbUser" :model="form" label-width="120px" label-position="top">
            <el-form-item :label="$t('register.name')" required>
                <el-input v-model="form.realName" :placeholder="$t('register.name')" />
            </el-form-item>
            <el-form-item :label="$t('register.phone')" required>
                <el-input v-model="form.phone" :placeholder="$t('register.phone')" maxlength="10" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="submit" :loading="loading">{{ $t('register.submit') }}</el-button>
            </el-form-item>
        </el-form>
        <div v-else>
            <p>{{ $t('home.loginPrompt') }}</p>
            <el-button type="primary" @click="$router.push('/')">{{ $t('common.back') }}</el-button>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import axios from 'axios'
import { config } from '../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import liff from '@line/liff'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = ref({
    realName: '',
    phone: ''
})

const submit = async () => {
    if (!form.value.realName || !form.value.phone) {
        ElMessage.warning(t('register.fillAll'))
        return
    }

    // Taiwan phone validation
    const phoneRegex = /^09\d{8}$/
    if (!phoneRegex.test(form.value.phone)) {
        ElMessage.error(t('register.invalidPhone'))
        return
    }

    loading.value = true
    try {
        const res = await axios.put(`${config.apiBaseUrl}/api/users/${userStore.dbUser.id}/complete-profile`, {
            realName: form.value.realName,
            phone: form.value.phone
        })

        // Update store
        userStore.setDbUser(res.data)
        ElMessage.success(t('register.success'))

        // Check for friendship
        try {
            if (liff.isInClient() || liff.isLoggedIn()) {
                const friendship = await liff.getFriendship().catch((err) => {
                    console.error('getFriendship failed', err)
                    return { friendFlag: false }
                })
                
                if (!friendship || !friendship.friendFlag) {
                    // Prompt user to add friend
                    await ElMessageBox.confirm(
                        '請加入官方帳號好友以接收預約通知！',
                        '加入好友',
                        {
                            confirmButtonText: '前往加入',
                            cancelButtonText: '稍後再說',
                            type: 'warning'
                        }
                    ).then(() => {
                        // Open Add Friend Link
                        // Note: Replace @your_line_id with your actual Basic ID or Premium ID
                        window.open(`https://line.me/R/ti/p/${config.lineOaId}`, '_blank')
                    }).catch(() => {
                        // User cancelled
                    })
                }
            }
        } catch (e) {
            console.error('Failed to check friendship workflow', e)
        }

        router.push('/')
    } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data || t('common.error'))
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.register-container {
    max-width: 500px;
    margin: 50px auto;
    padding: 20px;
}

@media (max-width: 480px) {
    .register-container {
        margin: 20px auto;
        padding: 20px 16px;
    }
}
</style>
