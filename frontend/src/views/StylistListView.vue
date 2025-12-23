<template>
    <div class="stylist-list">
        <h2>Select a Stylist</h2>
        <el-row :gutter="20">
            <el-col :span="24" :md="12" v-for="stylist in stylists" :key="stylist.id" class="mb-4">
                <el-card shadow="hover">
                    <div class="stylist-card-content">
                        <el-avatar :size="64"
                            :src="stylist.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                        <div class="info">
                            <h3>{{ stylist.name }}</h3>
                            <p>{{ stylist.specialty }}</p>
                        </div>
                        <el-button type="primary" @click="selectStylist(stylist.id)">Book</el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Mock data
const stylists = ref([
    { id: 1, name: 'Alice', specialty: 'Cut & Color', avatar: '' },
    { id: 2, name: 'Bob', specialty: 'Perm & Treatment', avatar: '' },
    { id: 3, name: 'Charlie', specialty: 'Men\'s Cut', avatar: '' }
])

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
