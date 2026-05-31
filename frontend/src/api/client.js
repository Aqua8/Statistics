import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true, // httpOnly 쿠키 자동 전송
})

client.interceptors.response.use(
  (res) => {
    // ApiResponse 래퍼 자동 언래핑: {success, data, message} → data
    if (res.data && typeof res.data === 'object' && 'success' in res.data) {
      res.data = res.data.data
    }
    return res
  },
  async (err) => {
    const original = err.config
    // /auth/refresh 자체가 실패하면 재시도 없이 로그인 페이지로
    if (err.response?.status === 401 && !original._retry && !original.url.includes('/auth/refresh')) {
      original._retry = true
      try {
        // 쿠키에 refreshToken이 있으면 서버가 자동으로 읽어 새 accessToken 쿠키 발급
        await client.post('/auth/refresh')
        return client(original)
      } catch {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export default client
