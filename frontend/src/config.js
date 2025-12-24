export const config = {
    liffId: import.meta.env.VITE_LIFF_ID || '2008756214-RUOCo5l1',
    // Fallback to the production backend URL if env var is missing
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'https://bbsapi-production.up.railway.app'
}
