import liff from '@line/liff'

export const initLiff = async (liffId) => {
    try {
        await liff.init({ liffId })
        if (!liff.isLoggedIn()) {
            liff.login()
        }
        return liff.getProfile()
    } catch (error) {
        console.error('LIFF initialization failed', error)
        throw error
    }
}
