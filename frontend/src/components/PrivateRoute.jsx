import { useState, useEffect } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { getProfile } from '../api/user'

const CACHE_TTL = 5 * 60 * 1000 // 5분
let cachedStatus = null
let cacheExpiry = 0

export function clearAuthCache() {
  cachedStatus = null
  cacheExpiry = 0
}

export default function PrivateRoute() {
  const isCacheValid = cachedStatus === 'auth' && Date.now() < cacheExpiry
  const [status, setStatus] = useState(isCacheValid ? 'auth' : 'loading')

  useEffect(() => {
    if (isCacheValid) return
    getProfile()
      .then(() => {
        cachedStatus = 'auth'
        cacheExpiry = Date.now() + CACHE_TTL
        setStatus('auth')
      })
      .catch(() => {
        cachedStatus = null
        cacheExpiry = 0
        setStatus('unauth')
      })
  }, [isCacheValid])

  if (status === 'loading') return null
  if (status === 'unauth') return <Navigate to="/login" replace />
  return <Outlet />
}
