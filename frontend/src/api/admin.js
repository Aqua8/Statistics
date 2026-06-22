import client from './client'

export const runBatch = (projectId, date) =>
  client.post(`/admin/batch/run?projectId=${projectId}&date=${date}`)
