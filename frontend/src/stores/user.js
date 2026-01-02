import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { config } from '../config'

export const useUserStore = defineStore('user', () => {
    const profile = ref(null)
    const dbUser = ref(null)
    const isLoggedIn = ref(false)
    const isLoading = ref(false)

    const setProfile = (p) => {
        profile.value = p
        isLoggedIn.value = !!p
    }

    const setDbUser = (u) => {
        dbUser.value = u
    }

    const loginToBackend = async () => {
        if (!profile.value) return

        try {
            isLoading.value = true
            const response = await axios.post(`${config.apiBaseUrl}/api/users/login`, {
                lineUserId: profile.value.userId,
                displayName: profile.value.displayName,
                pictureUrl: profile.value.pictureUrl
            })
            dbUser.value = response.data
            console.log('Backend login success:', dbUser.value)
        } catch (error) {
            console.error('Backend login failed:', error)
        } finally {
            isLoading.value = false
        }
    }

    return {
        profile,
        dbUser,
        isLoggedIn,
        isLoading,
        setProfile,
        setDbUser,
        loginToBackend
    }
})
