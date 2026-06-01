import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProjects, createProject, deleteProject } from '../api/projects'
import { logout } from '../api/auth'
import styles from './ProjectsPage.module.css'

const getSnippet = (trackingKey) =>
  `<script src="${location.origin}/tracker.js" data-key="${trackingKey}" async></script>`

// 로그인 후 프로젝트 목록에서 현재 사이트 도메인과 일치하는 프로젝트를 찾아 트래커 초기화
// 일치하는 도메인이 없으면 첫 번째 프로젝트로 폴백
function injectSelfTracker(projects) {
  if (!projects.length) return
  if (document.querySelector('script[data-self-tracker]')) return // 중복 주입 방지
  const matched = projects.find((p) => location.hostname.includes(p.domain) || p.domain.includes(location.hostname))
  const project = matched || projects[0]
  const script = document.createElement('script')
  script.src = '/tracker.js'
  script.dataset.key = project.trackingKey
  script.dataset.selfTracker = 'true'
  script.async = true
  document.head.appendChild(script)
}

export default function ProjectsPage() {
  const navigate = useNavigate()
  const [projects, setProjects] = useState([])
  const [form, setForm] = useState({ name: '', domain: '' })
  const [loading, setLoading] = useState(true)
  const [createError, setCreateError] = useState('')
  const [actionError, setActionError] = useState('')
  const [snippetId, setSnippetId] = useState(null)
  const [copiedId, setCopiedId] = useState(null)

  const handleCopy = useCallback((trackingKey, id) => {
    navigator.clipboard.writeText(getSnippet(trackingKey))
    setCopiedId(id)
    setTimeout(() => setCopiedId((cur) => (cur === id ? null : cur)), 2000)
  }, [])

  useEffect(() => {
    getProjects()
      .then((data) => {
        setProjects(data)
        injectSelfTracker(data)
      })
      .finally(() => setLoading(false))
  }, [])

  const isDomainValid = (domain) =>
    /^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}$/.test(domain)

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreateError('')
    if (!isDomainValid(form.domain)) {
      setCreateError('올바른 도메인 형식이 아닙니다. (예: example.com)')
      return
    }
    try {
      const project = await createProject(form.name, form.domain)
      setProjects((prev) => [...prev, project])
      setForm({ name: '', domain: '' })
    } catch (err) {
      setCreateError(err.response?.data?.message || '프로젝트 생성에 실패했습니다. 다시 시도해주세요.')
    }
  }

  const handleDelete = useCallback(async (id) => {
    if (!confirm('프로젝트를 삭제하시겠습니까?')) return
    setActionError('')
    try {
      await deleteProject(id)
      setProjects((prev) => prev.filter((p) => p.id !== id))
    } catch {
      setActionError('프로젝트 삭제에 실패했습니다. 다시 시도해주세요.')
    }
  }, [])

  const handleLogout = useCallback(() => {
    logout()
    navigate('/login')
  }, [navigate])

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <div className={styles.headerIcon}>S</div>
          <h1 className={styles.headerTitle}>내 프로젝트</h1>
        </div>
        <div className={styles.headerActions}>
          <button onClick={() => navigate('/mypage')} className={styles.mypageBtn}>마이페이지</button>
          <button onClick={handleLogout} className={styles.logoutBtn}>로그아웃</button>
        </div>
      </header>

      <div className={styles.createSection}>
        <p className={styles.createTitle}>새 프로젝트 추가</p>
        <form onSubmit={handleCreate} className={styles.createForm}>
          <input
            placeholder="프로젝트 이름"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className={styles.input}
            required
          />
          <input
            placeholder="도메인 (예: example.com)"
            value={form.domain}
            onChange={(e) => setForm({ ...form, domain: e.target.value })}
            className={styles.input}
            required
          />
          <button type="submit" className={styles.createBtn}>+ 추가</button>
        </form>
        {createError && <p className={styles.createError}>{createError}</p>}
      </div>

      {actionError && <p className={styles.createError}>{actionError}</p>}

      {loading ? (
        <p className={styles.empty}>불러오는 중...</p>
      ) : projects.length === 0 ? (
        <p className={styles.empty}>
          <span className={styles.emptyIcon}>📊</span>
          아직 등록된 프로젝트가 없습니다.<br />
          위에서 첫 프로젝트를 추가해보세요.
        </p>
      ) : (
        <>
          <div className={styles.listHeader}>
            <span className={styles.listTitle}>프로젝트</span>
            <span className={styles.count}>{projects.length}개</span>
          </div>
          <ul className={styles.list}>
            {projects.map((p) => (
              <li key={p.id} className={styles.item}>
                <div className={styles.itemMain}>
                  <div className={styles.info}>
                    <span className={styles.name}>{p.name}</span>
                    <span className={styles.domain}>{p.domain}</span>
                    <code className={styles.key}>{p.trackingKey}</code>
                  </div>
                  <div className={styles.actions}>
                    <button
                      onClick={() => setSnippetId(snippetId === p.id ? null : p.id)}
                      className={styles.snippetBtn}
                    >
                      {snippetId === p.id ? '닫기' : '삽입 코드'}
                    </button>
                    <button
                      onClick={() => { sessionStorage.setItem('currentProjectId', p.id); navigate('/dashboard') }}
                      className={styles.dashBtn}
                    >
                      대시보드 →
                    </button>
                    <button
                      onClick={() => handleDelete(p.id)}
                      className={styles.deleteBtn}
                    >
                      삭제
                    </button>
                  </div>
                </div>
                {snippetId === p.id && (
                  <div className={styles.snippetBox}>
                    <p className={styles.snippetLabel}>아래 코드를 웹사이트의 <code>&lt;head&gt;</code> 또는 <code>&lt;body&gt;</code> 끝에 추가하세요.</p>
                    <div className={styles.snippetRow}>
                      <code className={styles.snippet}>{getSnippet(p.trackingKey)}</code>
                      <button onClick={() => handleCopy(p.trackingKey, p.id)} className={styles.copyBtn}>
                        {copiedId === p.id ? '복사됨 ✓' : '복사'}
                      </button>
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  )
}
