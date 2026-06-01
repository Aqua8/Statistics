import { useState, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { findAccount, checkEmail, resetPassword } from '../api/auth'
import styles from './AuthPage.module.css'

const PW_RULES = [
  { label: '8자 이상',       test: (pw) => pw.length >= 8 },
  { label: '영문 포함',       test: (pw) => /[A-Za-z]/.test(pw) },
  { label: '숫자 포함',       test: (pw) => /\d/.test(pw) },
  { label: '특수문자 포함',    test: (pw) => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw) },
]

function FindAccountTab() {
  const [name, setName] = useState('')
  const [results, setResults] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setResults(null)
    setLoading(true)
    try {
      const emails = await findAccount(name)
      setResults(emails)
    } catch (err) {
      setError(err.response?.data?.message || '조회에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <p className={styles.subtitle}>가입 시 입력한 이름을 입력하세요</p>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>이름</label>
          <input
            type="text"
            placeholder="홍길동"
            value={name}
            onChange={(e) => { setName(e.target.value); setResults(null); setError('') }}
            className={styles.input}
            required
          />
        </div>
        {error && <p className={styles.error}>{error}</p>}
        {results && (
          <div className={styles.resultBox}>
            <p className={styles.resultLabel}>
              <strong>{name}</strong> 이름으로 가입된 계정 {results.length}개
            </p>
            <ul className={styles.resultList}>
              {results.map((email, i) => (
                <li key={i} className={styles.resultItem}>{email}</li>
              ))}
            </ul>
          </div>
        )}
        <button type="submit" className={styles.button} disabled={loading}>
          {loading ? '조회 중...' : '아이디 찾기'}
        </button>
      </form>
    </>
  )
}

function ResetPasswordTab() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' })
  const [pwTouched, setPwTouched] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const pwChecks = useMemo(() => PW_RULES.map((r) => ({ ...r, ok: r.test(form.newPassword) })), [form.newPassword])
  const pwValid = pwChecks.every((r) => r.ok)

  const handleCheckEmail = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await checkEmail(email)
      setStep(2)
    } catch (err) {
      setError(err.response?.data?.message || '이메일 확인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async (e) => {
    e.preventDefault()
    setError('')
    if (!pwValid) {
      setPwTouched(true)
      return
    }
    if (form.newPassword !== form.confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }
    setLoading(true)
    try {
      await resetPassword(email, form.newPassword)
      navigate('/login', { state: { message: '비밀번호가 재설정되었습니다. 로그인해주세요.' } })
    } catch (err) {
      setError(err.response?.data?.message || '비밀번호 재설정에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  if (step === 1) return (
    <>
      <p className={styles.subtitle}>가입한 이메일을 입력하세요</p>
      <form onSubmit={handleCheckEmail} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>이메일</label>
          <input
            type="email"
            placeholder="email@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={styles.input}
            required
          />
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button type="submit" className={styles.button} disabled={loading}>
          {loading ? '확인 중...' : '다음'}
        </button>
      </form>
    </>
  )

  return (
    <>
      <p className={styles.subtitle}>{email}</p>
      <form onSubmit={handleResetPassword} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>새 비밀번호</label>
          <input
            type="password"
            placeholder="8자 이상, 영문·숫자·특수문자 포함"
            value={form.newPassword}
            onChange={(e) => { setForm({ ...form, newPassword: e.target.value }); setPwTouched(true) }}
            className={`${styles.input} ${pwTouched && !pwValid ? styles.inputError : ''}`}
            required
          />
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
        <div className={styles.field}>
          <label className={styles.label}>새 비밀번호 확인</label>
          <input
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            value={form.confirmPassword}
            onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
            className={styles.input}
            required
          />
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button type="submit" className={styles.button} disabled={loading}>
          {loading ? '재설정 중...' : '비밀번호 재설정'}
        </button>
      </form>
    </>
  )
}

export default function ForgotPasswordPage() {
  const [tab, setTab] = useState('find')

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.brand}>
          <div className={styles.brandIcon}>S</div>
          <span className={styles.brandName}>StatDash</span>
        </div>

        <div className={styles.tabs}>
          <button
            className={`${styles.tab} ${tab === 'find' ? styles.tabActive : ''}`}
            onClick={() => setTab('find')}
            type="button"
          >
            아이디 찾기
          </button>
          <button
            className={`${styles.tab} ${tab === 'reset' ? styles.tabActive : ''}`}
            onClick={() => setTab('reset')}
            type="button"
          >
            비밀번호 찾기
          </button>
        </div>

        {tab === 'find' ? <FindAccountTab /> : <ResetPasswordTab />}

        <p className={styles.link}>
          <Link to="/login">로그인으로 돌아가기</Link>
        </p>
      </div>
    </div>
  )
}
