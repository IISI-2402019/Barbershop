import liff from '@line/liff'

export const initLiff = async (liffId) => {
    try {
        await liff.init({ liffId })

        // 如果是在 LINE App 內開啟，通常會自動登入
        // 如果是在外部瀏覽器，且之前登入過，也會是 true
        if (liff.isLoggedIn()) {
            return liff.getProfile()
        } else {
            // 如果是在外部瀏覽器且未登入，這裡保留自動登入邏輯
            // 注意：這會導致頁面重導向至 LINE 登入頁
            if (!liff.isInClient()) {
                liff.login()
                return null // 登入重導向中，回傳 null
            }
            return null
        }
    } catch (error) {
        console.error('LIFF initialization failed', error)
        throw error
    }
}
