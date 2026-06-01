import client from './client'

export const getProfile = () => client.get('/user/me').then((r) => r.data)
export const updateName = (name) => client.put('/user/me/name', { name })
export const updatePassword = (currentPassword, newPassword) =>
  client.put('/user/me/password', { currentPassword, newPassword })
