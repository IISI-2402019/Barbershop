import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import StylistListView from '../views/StylistListView.vue'
import BookingView from '../views/BookingView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
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
        }
    ]
})

export default router
