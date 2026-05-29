import { useState, useMemo } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../api/auth'
import styles from './AuthPage.module.css'

const PW_RULES = [
  { label: '8자 이상',          test: (pw) => pw.length >= 8 },
  { label: '영문 포함',          test: (pw) => /[A-Za-z]/.test(pw) },
  { label: '숫자 포함',          test: (pw) => /\d/.test(pw) },
  { label: '특수문자 포함',       test: (pw) => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw) },
]

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', name: '' })
  const [pwTouched, setPwTouched] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const pwChecks = useMemo(() => PW_RULES.map((r) => ({ ...r, ok: r.test(form.password) })), [form.password])
  const pwValid = pwChecks.every((r) => r.ok)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!pwValid) {
      setPwTouched(true)
      return
    }
    setError('')
    setLoading(true)
    try {
      await register(form.email, form.password, form.name)
      navigate('/login')
    } catch (err) {
      const msg = err.response?.data?.message
      setError(msg || '이미 사용 중인 이메일이거나 입력값을 확인해주세요.')
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
        <h1 className={styles.title}>계정 만들기</h1>
        <p className={styles.subtitle}>무료로 시작하세요</p>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>이름</label>
            <input
              type="text"
              placeholder="홍길동"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className={styles.input}
              required
            />
          </div>
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
              placeholder="8자 이상, 영문·숫자·특수문자 포함"
              value={form.password}
              onChange={(e) => { setForm({ ...form, password: e.target.value }); setPwTouched(true) }}
              className={`${styles.input} ${pwTouched && !pwValid ? styles.inputError : ''}`}
              required
            />
            {/* 비밀번호 입력 시작 후 조건 체크리스트 표시 */}
            {pwTouched && (
              <ul className={styles.pwRules}>
                {pwChecks.map((r) => (
                  <li key={r.label} className={r.ok ? styles.pwRuleOk : styles.pwRuleFail}>
                    {r.ok ? '✓' : '✗'} {r.label}
                  </li>
                ))}
              </ul>
            )}
          </div>
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" className={styles.button} disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>
        <p className={styles.link}>
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </div>
  )
}
