import client from './client'

export const login = (email, password) =>
  client.post('/auth/login', { email, password }).then((r) => r.data)

export const register = (email, password, name) =>
  client.post('/auth/register', { email, password, name })

// 서버에서 refreshToken 쿠키를 읽어 삭제하고 accessToken 쿠키도 만료 처리
export const logout = () => {
  client.post('/auth/logout').catch(() => {})
}
