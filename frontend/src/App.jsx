import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import ProjectsPage from './pages/ProjectsPage'
import MyPage from './pages/MyPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import PrivateRoute from './components/PrivateRoute'
import WelcomeDialog, { shouldShowWelcome } from './components/WelcomeDialog'

function RouteTracker() {
  const location = useLocation()
  useEffect(() => {
    window.tracker?.pageview()
  }, [location.pathname])
  return null
}

function App() {
  const [showWelcome, setShowWelcome] = useState(() => shouldShowWelcome())

  return (
    <>
      <RouteTracker />
      {showWelcome && <WelcomeDialog onClose={() => setShowWelcome(false)} />}
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route element={<PrivateRoute />}>
          <Route path="/projects" element={<ProjectsPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/mypage" element={<MyPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </>
  )
}

export default App
