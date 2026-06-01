import { useState, useEffect } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { getProfile } from '../api/user'

export default function PrivateRoute() {
  const [status, setStatus] = useState('loading')

  useEffect(() => {
    getProfile()
      .then(() => setStatus('auth'))
      .catch(() => setStatus('unauth'))
  }, [])

  if (status === 'loading') return null
  if (status === 'unauth') return <Navigate to="/login" replace />
  return <Outlet />
}
