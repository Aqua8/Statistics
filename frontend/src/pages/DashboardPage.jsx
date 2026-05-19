import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar,
} from 'recharts'
import { getDailyStats, getPageStats, getReferrerStats } from '../api/stats'
import styles from './DashboardPage.module.css'

function toDateStr(date) {
  return date.toISOString().slice(0, 10)
}

export default function DashboardPage() {
  const { projectId } = useParams()
  const navigate = useNavigate()

  const today = new Date()
  const defaultFrom = new Date(today)
  defaultFrom.setDate(today.getDate() - 13)

  const [range, setRange] = useState({
    from: toDateStr(defaultFrom),
    to: toDateStr(today),
  })
  const [daily, setDaily] = useState([])
  const [pages, setPages] = useState([])
  const [referrers, setReferrers] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      getDailyStats(projectId, range.from, range.to),
      getPageStats(projectId, range.from, range.to),
      getReferrerStats(projectId, range.from, range.to),
    ])
      .then(([d, p, r]) => {
        setDaily(d)
        setPages(p.slice(0, 10))
        setReferrers(r.slice(0, 10))
      })
      .finally(() => setLoading(false))
  }, [projectId, range])

  const totalViews = daily.reduce((s, d) => s + (d.totalViews ?? 0), 0)
  const totalVisitors = daily.reduce((s, d) => s + (d.uniqueVisitors ?? 0), 0)
  const avgDuration = daily.length
    ? Math.round(daily.reduce((s, d) => s + (d.avgDuration ?? 0), 0) / daily.length / 1000)
    : 0

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button onClick={() => navigate('/projects')} className={styles.backBtn}>← 프로젝트 목록</button>
        <div className={styles.dateRange}>
          <input
            type="date"
            value={range.from}
            onChange={(e) => setRange({ ...range, from: e.target.value })}
            className={styles.dateInput}
          />
          <span>~</span>
          <input
            type="date"
            value={range.to}
            onChange={(e) => setRange({ ...range, to: e.target.value })}
            className={styles.dateInput}
          />
        </div>
      </header>

      <div className={styles.summary}>
        <div className={styles.card}>
          <p className={styles.cardLabel}>총 페이지뷰</p>
          <p className={styles.cardValue}>{totalViews.toLocaleString()}</p>
        </div>
        <div className={styles.card}>
          <p className={styles.cardLabel}>순방문자</p>
          <p className={styles.cardValue}>{totalVisitors.toLocaleString()}</p>
        </div>
        <div className={styles.card}>
          <p className={styles.cardLabel}>평균 체류시간</p>
          <p className={styles.cardValue}>{avgDuration}초</p>
        </div>
      </div>

      {loading ? (
        <p className={styles.loading}>불러오는 중...</p>
      ) : (
        <>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>일별 트래픽</h2>
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={daily}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                <XAxis dataKey="statDate" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Line type="monotone" dataKey="totalViews" name="페이지뷰" stroke="#4f6ef7" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="uniqueVisitors" name="순방문자" stroke="#48bb78" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </section>

          <div className={styles.row}>
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>인기 페이지 TOP 10</h2>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={pages} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                  <XAxis type="number" tick={{ fontSize: 11 }} />
                  <YAxis dataKey="pageUrl" type="category" width={160} tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="views" name="조회수" fill="#4f6ef7" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </section>

            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>유입 경로 TOP 10</h2>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={referrers} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                  <XAxis type="number" tick={{ fontSize: 11 }} />
                  <YAxis dataKey="referrer" type="category" width={160} tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="visits" name="방문수" fill="#48bb78" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </section>
          </div>
        </>
      )}
    </div>
  )
}
