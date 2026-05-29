import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProjects, createProject, deleteProject } from '../api/projects'
import styles from './ProjectsPage.module.css'

export default function ProjectsPage() {
  const navigate = useNavigate()
  const [projects, setProjects] = useState([])
  const [form, setForm] = useState({ name: '', domain: '' })
  const [loading, setLoading] = useState(true)
  const [snippetId, setSnippetId] = useState(null)
  const [copied, setCopied] = useState(false)

  const getSnippet = (trackingKey) =>
    `<script src="${location.origin}/tracker.js" data-key="${trackingKey}" async></script>`

  const handleCopy = (trackingKey) => {
    navigator.clipboard.writeText(getSnippet(trackingKey))
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  useEffect(() => {
    getProjects()
      .then(setProjects)
      .finally(() => setLoading(false))
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    const project = await createProject(form.name, form.domain)
    setProjects([...projects, project])
    setForm({ name: '', domain: '' })
  }

  const handleDelete = async (id) => {
    if (!confirm('프로젝트를 삭제하시겠습니까?')) return
    await deleteProject(id)
    setProjects(projects.filter((p) => p.id !== id))
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    navigate('/login')
  }

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <div className={styles.headerIcon}>S</div>
          <h1 className={styles.headerTitle}>내 프로젝트</h1>
        </div>
        <button onClick={handleLogout} className={styles.logoutBtn}>로그아웃</button>
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
      </div>

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
                      onClick={() => navigate(`/projects/${p.id}/dashboard`)}
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
                      <button onClick={() => handleCopy(p.trackingKey)} className={styles.copyBtn}>
                        {copied ? '복사됨 ✓' : '복사'}
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
