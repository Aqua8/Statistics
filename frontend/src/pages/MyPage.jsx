import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProfile, updateName, updatePassword } from '../api/user'
import styles from './MyPage.module.css'

const PW_RULES = [
  { label: '8자 이상',       test: (pw) => pw.length >= 8 },
  { label: '영문 포함',       test: (pw) => /[A-Za-z]/.test(pw) },
  { label: '숫자 포함',       test: (pw) => /\d/.test(pw) },
  { label: '특수문자 포함',    test: (pw) => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw) },
]

export default function MyPage() {
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)

  const [nameForm, setNameForm] = useState({ name: '' })
  const [nameMsg, setNameMsg] = useState({ text: '', error: false })
  const [nameSaving, setNameSaving] = useState(false)

  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [pwTouched, setPwTouched] = useState(false)
  const [pwMsg, setPwMsg] = useState({ text: '', error: false })
  const [pwSaving, setPwSaving] = useState(false)

  const pwChecks = useMemo(() => PW_RULES.map((r) => ({ ...r, ok: r.test(pwForm.newPassword) })), [pwForm.newPassword])
  const pwValid = pwChecks.every((r) => r.ok)

  useEffect(() => {
    getProfile().then((data) => {
      setProfile(data)
      setNameForm({ name: data.name })
    })
  }, [])

  const handleNameSubmit = async (e) => {
    e.preventDefault()
    setNameMsg({ text: '', error: false })
    setNameSaving(true)
    try {
      await updateName(nameForm.name)
      setProfile((prev) => ({ ...prev, name: nameForm.name }))
      setNameMsg({ text: '이름이 변경되었습니다.', error: false })
    } catch (err) {
      setNameMsg({ text: err.response?.data?.message || '변경에 실패했습니다.', error: true })
    } finally {
      setNameSaving(false)
    }
  }

  const handlePwSubmit = async (e) => {
    e.preventDefault()
    setPwMsg({ text: '', error: false })
    if (!pwValid) {
      setPwTouched(true)
      return
    }
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      setPwMsg({ text: '새 비밀번호가 일치하지 않습니다.', error: true })
      return
    }
    setPwSaving(true)
    try {
      await updatePassword(pwForm.currentPassword, pwForm.newPassword)
      setPwMsg({ text: '비밀번호가 변경되었습니다.', error: false })
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (err) {
      setPwMsg({ text: err.response?.data?.message || '변경에 실패했습니다.', error: true })
    } finally {
      setPwSaving(false)
    }
  }

  if (!profile) return <div className={styles.loading}>불러오는 중...</div>

  const joinedDate = new Date(profile.createdAt).toLocaleDateString('ko-KR', {
    year: 'numeric', month: 'long', day: 'numeric',
  })

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <button onClick={() => navigate('/projects')} className={styles.backBtn}>← 프로젝트 목록</button>
        </div>
        <h1 className={styles.headerTitle}>마이페이지</h1>
      </header>

      <div className={styles.infoCard}>
        <div className={styles.avatar}>{profile.name.charAt(0).toUpperCase()}</div>
        <div className={styles.infoText}>
          <p className={styles.infoName}>{profile.name}</p>
          <p className={styles.infoEmail}>{profile.email}</p>
          <p className={styles.infoJoined}>가입일: {joinedDate}</p>
        </div>
      </div>

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>이름 변경</h2>
        <form onSubmit={handleNameSubmit} className={styles.form}>
          <input
            className={styles.input}
            value={nameForm.name}
            onChange={(e) => setNameForm({ name: e.target.value })}
            placeholder="새 이름"
            required
          />
          <button type="submit" className={styles.saveBtn} disabled={nameSaving}>
            {nameSaving ? '저장 중...' : '저장'}
          </button>
        </form>
        {nameMsg.text && (
          <p className={nameMsg.error ? styles.errorMsg : styles.successMsg}>{nameMsg.text}</p>
        )}
      </section>

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>비밀번호 변경</h2>
        <form onSubmit={handlePwSubmit} className={styles.form}>
          <input
            type="password"
            className={styles.input}
            value={pwForm.currentPassword}
            onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })}
            placeholder="현재 비밀번호"
            required
          />
          <input
            type="password"
            className={`${styles.input} ${pwTouched && !pwValid ? styles.inputError : ''}`}
            value={pwForm.newPassword}
            onChange={(e) => { setPwForm({ ...pwForm, newPassword: e.target.value }); setPwTouched(true) }}
            placeholder="새 비밀번호 (8자·영문·숫자·특수문자)"
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
          <input
            type="password"
            className={styles.input}
            value={pwForm.confirmPassword}
            onChange={(e) => setPwForm({ ...pwForm, confirmPassword: e.target.value })}
            placeholder="새 비밀번호 확인"
            required
          />
          <button type="submit" className={styles.saveBtn} disabled={pwSaving}>
            {pwSaving ? '변경 중...' : '변경'}
          </button>
        </form>
        {pwMsg.text && (
          <p className={pwMsg.error ? styles.errorMsg : styles.successMsg}>{pwMsg.text}</p>
        )}
      </section>
    </div>
  )
}
