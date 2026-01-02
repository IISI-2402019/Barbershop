<template>
    <div class="register-container">
        <h2>{{ $t('register.title') }}</h2>
        <div v-if="userStore.isLoading">{{ $t('common.loading') }}</div>
        <el-form v-else-if="userStore.dbUser" :model="form" label-width="120px" label-position="top">
            <el-form-item :label="$t('register.name')">
                <el-input v-model="form.realName" :placeholder="$t('register.name')" />
            </el-form-item>
            <el-form-item :label="$t('register.phone')">
                <el-input v-model="form.phone" :placeholder="$t('register.phone')" maxlength="10" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="submit">{{ $t('register.submit') }}</el-button>
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
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

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

    try {
        const res = await axios.put(`${config.apiBaseUrl}/api/users/${userStore.dbUser.id}/complete-profile`, {
            realName: form.value.realName,
            phone: form.value.phone
        })

        // Update store
        userStore.setDbUser(res.data)
        ElMessage.success(t('register.success'))
        router.push('/')
    } catch (error) {
        console.error(error)
        ElMessage.error(error.response?.data || t('common.error'))
    }
}
</script>

<style scoped>
.register-container {
    max-width: 500px;
    margin: 50px auto;
    padding: 20px;
}
</style>
