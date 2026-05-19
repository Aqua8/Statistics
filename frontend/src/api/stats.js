import client from './client'

export const getDailyStats = (projectId, from, to) =>
  client.get(`/projects/${projectId}/stats/daily`, { params: { from, to } }).then((r) => r.data)

export const getPageStats = (projectId, from, to) =>
  client.get(`/projects/${projectId}/stats/pages`, { params: { from, to } }).then((r) => r.data)

export const getReferrerStats = (projectId, from, to) =>
  client.get(`/projects/${projectId}/stats/referrers`, { params: { from, to } }).then((r) => r.data)
