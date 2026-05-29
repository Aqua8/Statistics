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
  (res) => res,
  (err) => {
    if (err.response?.status === 401 && localStorage.getItem('token')) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)

export default client
