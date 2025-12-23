export const config = {
    liffId: import.meta.env.VITE_LIFF_ID || '2008756214-RUOCo5l1',
    // 如果環境變數沒設定，預設為空字串，避免產生 "undefined" 字串
    // 請在 Railway 的 Variables 中設定 VITE_API_BASE_URL
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || ''
}
