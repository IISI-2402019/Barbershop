import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { config } from '../config'

// Setup Axios Interceptor for JWT
axios.interceptors.request.use(
    (axiosConfig) => {
        const token = localStorage.getItem('jwt_token')
        if (token) {
            axiosConfig.headers['Authorization'] = `Bearer ${token}`
        }
        return axiosConfig
    },
    (error) => {
        return Promise.reject(error)
    }
)

export const useUserStore = defineStore('user', () => {
    const profile = ref(null)
    const dbUser = ref(null)
    const isLoggedIn = ref(false)
    const isLoading = ref(false)
    const token = ref(localStorage.getItem('jwt_token') || null)

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

            // Backend now returns LoginResponse { token, user }
            const jwtToken = response.data.token
            const user = response.data.user

            if (jwtToken) {
                localStorage.setItem('jwt_token', jwtToken)
                token.value = jwtToken
            }

            dbUser.value = user
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
        token,
        setProfile,
        setDbUser,
        loginToBackend
    }
})
