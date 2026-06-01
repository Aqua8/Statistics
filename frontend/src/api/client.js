import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true, // httpOnly 쿠키 자동 전송
})

// 동시 401 응답이 여러 개 와도 refresh는 한 번만 호출되도록 공유 Promise 사용
let refreshPromise = null

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
    if (err.response?.status === 401 && !original._retry && !original.url?.includes('/auth/refresh')) {
      original._retry = true
      if (!refreshPromise) {
        refreshPromise = client.post('/auth/refresh').finally(() => {
          refreshPromise = null
        })
      }
      try {
        await refreshPromise
        return client(original)
      } catch {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export default client
