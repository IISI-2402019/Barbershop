import liff from '@line/liff'

export const initLiff = async (liffId) => {
  try {
    await liff.init({ liffId })
    if (!liff.isLoggedIn()) {
      liff.login()
      return null // Login redirect happens here
    }
    const profile = await liff.getProfile()
    return profile
  } catch (error) {
    console.error('LIFF initialization failed', error)
    throw error
  }
}
