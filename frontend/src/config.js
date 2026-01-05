export const config = {
    liffId: import.meta.env.VITE_LIFF_ID || '2008756214-RUOCo5l1',
    // Fallback to the production backend URL if env var is missing
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'https://bbsapi-production.up.railway.app',
    businessStartTime: import.meta.env.VITE_BUSINESS_START_TIME || '11:00:00',
    businessEndTime: import.meta.env.VITE_BUSINESS_END_TIME || '21:00:00'
}
