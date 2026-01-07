<template>
    <div class="stylist-list">
        <h2>{{ $t('stylist.selectStylist') }}</h2>
        <el-row :gutter="20">
            <el-col :span="24" :md="12" v-for="stylist in stylists" :key="stylist.id" class="mb-4">
                <el-card shadow="hover">
                    <div class="stylist-card-content">
                        <el-avatar :size="64" :src="getFullImageUrl(stylist.avatarUrl)" :icon="UserFilled" />
                        <div class="info">
                            <h3>{{ stylist.name }}</h3>
                        </div>
                        <el-button type="primary" @click="selectStylist(stylist.id)">{{ $t('common.book') }}</el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { config } from '../config'
import { UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const stylists = ref([])

const getFullImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    return `${config.apiBaseUrl}${url}`
}

onMounted(async () => {
    try {
        const response = await axios.get(`${config.apiBaseUrl}/api/stylists`)
        stylists.value = response.data
    } catch (error) {
        console.error('Failed to fetch stylists', error)
    }
})

const selectStylist = (id) => {
    router.push({ name: 'booking', query: { stylistId: id } })
}
</script>
<style scoped>
.stylist-list {
    padding: 20px;
}

.mb-4 {
    margin-bottom: 16px;
}

.stylist-card-content {
    display: flex;
    align-items: center;
    gap: 20px;
}

.info {
    flex-grow: 1;
}

.info h3 {
    margin: 0 0 5px 0;
}

.info p {
    margin: 0;
    color: #666;
}
</style>
