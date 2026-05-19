import client from './client'

export const getProjects = () =>
  client.get('/projects').then((r) => r.data)

export const createProject = (name, domain) =>
  client.post('/projects', { name, domain }).then((r) => r.data)

export const deleteProject = (id) =>
  client.delete(`/projects/${id}`)
