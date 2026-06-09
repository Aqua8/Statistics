import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { login, guestLogin } from '../api/auth'
import styles from './AuthPage.module.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const successMessage = location.state?.message

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(form.email, form.password)
      sessionStorage.removeItem('guestMode')
      navigate('/projects')
    } catch (err) {
      const msg = err.response?.data?.message
      setError(msg || '이메일 또는 비밀번호가 올바르지 않습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleGuestLogin = async () => {
    setError('')
    setLoading(true)
    try {
      await guestLogin()
      sessionStorage.setItem('guestMode', 'true')
      navigate('/projects')
    } catch {
      setError('게스트 입장에 실패했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.brand}>
          <div className={styles.brandIcon}>S</div>
          <span className={styles.brandName}>StatDash</span>
        </div>
        <h1 className={styles.title}>다시 오셨군요!</h1>
        <p className={styles.subtitle}>계정에 로그인하세요</p>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>이메일</label>
            <input
              type="email"
              placeholder="email@example.com"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              className={styles.input}
              required
            />
          </div>
          <div className={styles.field}>
            <label className={styles.label}>비밀번호</label>
            <input
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              className={styles.input}
              required
            />
          </div>
          {successMessage && <p className={styles.success}>{successMessage}</p>}
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" className={styles.button} disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>
        <div className={styles.divider}>또는</div>
        <button
          type="button"
          onClick={handleGuestLogin}
          className={styles.guestButton}
          disabled={loading}
        >
          게스트로 입장
        </button>
        <p className={styles.link}>
          <Link to="/forgot-password">비밀번호를 잊으셨나요?</Link>
        </p>
        <p className={styles.link}>
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </p>
      </div>
    </div>
  )
}
