import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
})

// 모든 요청에 저장된 JWT 토큰을 Authorization 헤더로 자동 첨부
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 401 응답 시 토큰 제거 후 로그인 페이지로 리다이렉트
// 단, 로그인/회원가입 요청 자체의 401은 제외 — 인증된 상태에서 만료된 경우만 처리
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
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        original._retry = true
        try {
          const res = await client.post('/auth/refresh', { refreshToken })
          // 성공 인터셉터가 이미 언래핑했으므로 res.data = { token, refreshToken, ... }
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('refreshToken', res.data.refreshToken)
          original.headers.Authorization = `Bearer ${res.data.token}`
          return client(original)
        } catch {
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
        }
      } else {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export default client
