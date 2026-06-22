import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProjects, createProject, deleteProject } from '../api/projects'
import { runBatch } from '../api/admin'
import { logout } from '../api/auth'
import { clearAuthCache } from '../components/PrivateRoute'
import styles from './ProjectsPage.module.css'

const getSnippet = (trackingKey) =>
  `<script src="${location.origin}/tracker.js" data-key="${trackingKey}" async></script>`

// 로그인 후 프로젝트 목록에서 현재 사이트 도메인과 일치하는 프로젝트를 찾아 트래커 초기화
// 일치하는 도메인이 없으면 첫 번째 프로젝트로 폴백
function injectSelfTracker(projects) {
  if (!projects.length) return
  if (document.querySelector('script[data-self-tracker]')) return // 중복 주입 방지
  const project = projects.find((p) => location.hostname === p.domain)
  if (!project) return
  const script = document.createElement('script')
  script.src = '/tracker.js'
  script.dataset.key = project.trackingKey
  script.dataset.selfTracker = 'true'
  script.async = true
  document.head.appendChild(script)
}

export default function ProjectsPage() {
  const navigate = useNavigate()
  const isGuest = sessionStorage.getItem('guestMode') === 'true'
  const [projects, setProjects] = useState([])
  const [form, setForm] = useState({ name: '', domain: '' })
  const [loading, setLoading] = useState(true)
  const [createError, setCreateError] = useState('')
  const [actionError, setActionError] = useState('')
  const [snippetId, setSnippetId] = useState(null)
  const [copiedId, setCopiedId] = useState(null)
  const [batchProjectId, setBatchProjectId] = useState('')
  const [batchFrom, setBatchFrom] = useState('')
  const [batchTo, setBatchTo] = useState('')
  const [batchRunning, setBatchRunning] = useState(false)
  const [batchLog, setBatchLog] = useState([])

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

  const handleBatchRun = useCallback(async () => {
    if (!batchProjectId || !batchFrom || !batchTo) return
    const dates = []
    const d = new Date(batchFrom)
    const end = new Date(batchTo)
    const fmt = (dt) =>
      `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`
    while (d <= end) { dates.push(fmt(new Date(d))); d.setDate(d.getDate() + 1) }
    if (dates.length > 90) { alert('최대 90일까지 재집계할 수 있습니다.'); return }
    setBatchRunning(true)
    setBatchLog([])
    for (const date of dates) {
      try {
        await runBatch(batchProjectId, date)
        setBatchLog((prev) => [...prev, `${date} 완료`])
      } catch {
        setBatchLog((prev) => [...prev, `${date} 실패`])
      }
    }
    setBatchRunning(false)
  }, [batchProjectId, batchFrom, batchTo])

  const handleLogout = useCallback(() => {
    clearAuthCache()
    sessionStorage.removeItem('guestMode')
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
          {isGuest && <span className={styles.guestBadge}>게스트</span>}
          {!isGuest && <button onClick={() => navigate('/mypage')} className={styles.mypageBtn}>마이페이지</button>}
          <button onClick={handleLogout} className={styles.logoutBtn}>로그아웃</button>
        </div>
      </header>

      {!isGuest && (
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
      )}

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
                    {!isGuest && (
                      <button
                        onClick={() => setSnippetId(snippetId === p.id ? null : p.id)}
                        className={styles.snippetBtn}
                      >
                        {snippetId === p.id ? '닫기' : '삽입 코드'}
                      </button>
                    )}
                    <button
                      onClick={() => { sessionStorage.setItem('currentProjectId', p.id); navigate('/dashboard') }}
                      className={styles.dashBtn}
                    >
                      대시보드 →
                    </button>
                    {!isGuest && (
                      <button
                        onClick={() => handleDelete(p.id)}
                        className={styles.deleteBtn}
                      >
                        삭제
                      </button>
                    )}
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

      {!isGuest && projects.length > 0 && (
        <div className={styles.batchSection}>
          <p className={styles.createTitle}>배치 재집계</p>
          <div className={styles.batchRow}>
            <select
              value={batchProjectId}
              onChange={(e) => setBatchProjectId(e.target.value)}
              className={styles.input}
            >
              <option value="">프로젝트 선택</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name} ({p.domain})</option>
              ))}
            </select>
            <input
              type="date"
              value={batchFrom}
              onChange={(e) => setBatchFrom(e.target.value)}
              className={styles.input}
            />
            <span className={styles.batchSep}>~</span>
            <input
              type="date"
              value={batchTo}
              onChange={(e) => setBatchTo(e.target.value)}
              className={styles.input}
            />
            <button
              onClick={handleBatchRun}
              disabled={batchRunning || !batchProjectId || !batchFrom || !batchTo}
              className={styles.createBtn}
            >
              {batchRunning ? '실행 중...' : '재집계'}
            </button>
          </div>
          {batchLog.length > 0 && (
            <div className={styles.batchLog}>
              {batchLog.map((line, i) => <div key={i}>{line}</div>)}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
