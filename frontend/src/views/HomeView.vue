<template>
    <div class="home">
        <h1>Barbershop Appointment System</h1>
        <div class="actions">
            <el-button type="primary" size="large" @click="$router.push('/stylists')">Book Now</el-button>
            <el-button size="large">My Appointments</el-button>
        </div>
    </div>
</template>

<script setup>
</script>

<style scoped>
.home {
    text-align: center;
    padding: 40px 20px;
}

.actions {
    margin-top: 30px;
    display: flex;
    flex-direction: column;
    gap: 15px;
    align-items: center;
}
</style>
