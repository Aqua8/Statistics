import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProjects, createProject, deleteProject } from '../api/projects'
import styles from './ProjectsPage.module.css'

export default function ProjectsPage() {
  const navigate = useNavigate()
  const [projects, setProjects] = useState([])
  const [form, setForm] = useState({ name: '', domain: '' })
  const [loading, setLoading] = useState(true)

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
        <h1>내 프로젝트</h1>
        <button onClick={handleLogout} className={styles.logoutBtn}>로그아웃</button>
      </header>

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
        <button type="submit" className={styles.createBtn}>+ 프로젝트 추가</button>
      </form>

      {loading ? (
        <p className={styles.empty}>불러오는 중...</p>
      ) : projects.length === 0 ? (
        <p className={styles.empty}>등록된 프로젝트가 없습니다.</p>
      ) : (
        <ul className={styles.list}>
          {projects.map((p) => (
            <li key={p.id} className={styles.item}>
              <div className={styles.info}>
                <span className={styles.name}>{p.name}</span>
                <span className={styles.domain}>{p.domain}</span>
                <code className={styles.key}>{p.trackingKey}</code>
              </div>
              <div className={styles.actions}>
                <button
                  onClick={() => navigate(`/projects/${p.id}/dashboard`)}
                  className={styles.dashBtn}
                >
                  대시보드
                </button>
                <button
                  onClick={() => handleDelete(p.id)}
                  className={styles.deleteBtn}
                >
                  삭제
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
