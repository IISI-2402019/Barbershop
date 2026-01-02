import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import StylistListView from '../views/StylistListView.vue'
import BookingView from '../views/BookingView.vue'
import AdminView from '../views/AdminView.vue'
import MyAppointmentsView from '../views/MyAppointmentsView.vue'
import RegisterView from '../views/RegisterView.vue'
import { useUserStore } from '../stores/user'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/register',
            name: 'register',
            component: RegisterView
        },
        {
            path: '/stylists',
            name: 'stylists',
            component: StylistListView
        },
        {
            path: '/booking',
            name: 'booking',
            component: BookingView
        },
        {
            path: '/my-appointments',
            name: 'my-appointments',
            component: MyAppointmentsView
        },
        {
            path: '/admin',
            name: 'admin',
            component: AdminView
        }
    ]
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    // If user is logged in (has dbUser)
    if (userStore.dbUser) {
        const isProfileComplete = userStore.dbUser.realName && userStore.dbUser.phone

        if (!isProfileComplete && to.name !== 'register') {
            next({ name: 'register' })
            return
        }

        if (isProfileComplete && to.name === 'register') {
            next({ name: 'home' })
            return
        }
    }

    next()
})

export default router
