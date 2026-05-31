import client from './client'

export const login = (email, password) =>
  client.post('/auth/login', { email, password }).then((r) => {
    localStorage.setItem('token', r.data.token)
    localStorage.setItem('refreshToken', r.data.refreshToken)
    return r.data
  })

export const register = (email, password, name) =>
  client.post('/auth/register', { email, password, name })

export const logout = () => {
  const refreshToken = localStorage.getItem('refreshToken')
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  if (refreshToken) {
    client.post('/auth/logout', { refreshToken }).catch(() => {})
  }
}
