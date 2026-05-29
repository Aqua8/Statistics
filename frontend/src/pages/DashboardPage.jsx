import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, PieChart, Pie, Cell, Legend,
} from 'recharts'
import * as XLSX from 'xlsx'
import { getDailyStats, getPageStats, getReferrerStats, getDeviceStats, getBrowserStats } from '../api/stats'
import styles from './DashboardPage.module.css'

const PIE_COLORS = ['#4f6ef7', '#48bb78', '#ed8936', '#e53e3e', '#805ad5', '#319795']

function toDateStr(date) {
  return date.toISOString().slice(0, 10)
}

function daysAgo(n) {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return toDateStr(d)
}

const DATE_PRESETS = [
  { label: '오늘', days: 0 },
  { label: '7일', days: 6 },
  { label: '14일', days: 13 },
  { label: '30일', days: 29 },
]

function BreakdownPie({ data, title }) {
  if (!data.length) return <p className={styles.noData}>데이터 없음</p>
  return (
    <section className={styles.section}>
      <h2 className={styles.sectionTitle}>{title}</h2>
      <ResponsiveContainer width="100%" height={220}>
        <PieChart>
          <Pie
            data={data}
            dataKey="count"
            nameKey="name"
            cx="50%"
            cy="50%"
            outerRadius={80}
            label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
          >
            {data.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
          </Pie>
          <Tooltip formatter={(v) => v.toLocaleString()} />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </section>
  )
}

function SkeletonCard() {
  return <div className={styles.skeletonCard} />
}

function SkeletonSection() {
  return <div className={styles.skeletonSection} />
}

export default function DashboardPage() {
  const { projectId } = useParams()
  const navigate = useNavigate()

  const today = new Date()
  const defaultFrom = new Date(today)
  defaultFrom.setDate(today.getDate() - 13)

  const [range, setRange] = useState({ from: toDateStr(defaultFrom), to: toDateStr(today) })
  const [activePreset, setActivePreset] = useState('14일')
  const [daily, setDaily] = useState([])
  const [pages, setPages] = useState([])
  const [referrers, setReferrers] = useState([])
  const [devices, setDevices] = useState([])
  const [browsers, setBrowsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeVisitors, setActiveVisitors] = useState(0)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const sse = new EventSource(`/api/projects/${projectId}/stats/realtime?token=${token}`)
    sse.addEventListener('visitors', (e) => setActiveVisitors(parseInt(e.data, 10)))
    sse.onerror = () => sse.close()
    return () => sse.close()
  }, [projectId])

  useEffect(() => {
    setLoading(true)
    Promise.all([
      getDailyStats(projectId, range.from, range.to),
      getPageStats(projectId, range.from, range.to),
      getReferrerStats(projectId, range.from, range.to),
      getDeviceStats(projectId, range.from, range.to),
      getBrowserStats(projectId, range.from, range.to),
    ])
      .then(([d, p, r, dv, br]) => {
        setDaily(d)
        setPages(p.slice(0, 10))
        setReferrers(r.slice(0, 10))
        setDevices(dv)
        setBrowsers(br)
      })
      .finally(() => setLoading(false))
  }, [projectId, range])

  const handlePreset = (preset) => {
    setActivePreset(preset.label)
    setRange({ from: daysAgo(preset.days), to: toDateStr(new Date()) })
  }

  const handleDateChange = (key, value) => {
    setActivePreset(null)
    setRange((prev) => ({ ...prev, [key]: value }))
  }

  const totalViews = daily.reduce((s, d) => s + (d.totalViews ?? 0), 0)
  const totalVisitors = daily.reduce((s, d) => s + (d.uniqueVisitors ?? 0), 0)
  const avgDuration = daily.length
    ? Math.round(daily.reduce((s, d) => s + (d.avgDuration ?? 0), 0) / daily.length / 1000)
    : 0

  const handleExport = () => {
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(daily), '일별 통계')
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(pages), '페이지별')
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(referrers), '유입경로')
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(devices), '디바이스')
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(browsers), '브라우저')
    XLSX.writeFile(wb, `stats_${range.from}_${range.to}.xlsx`)
  }

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button onClick={() => navigate('/projects')} className={styles.backBtn}>← 프로젝트 목록</button>
        <div className={styles.headerRight}>
          <div className={styles.presets}>
            {DATE_PRESETS.map((p) => (
              <button
                key={p.label}
                onClick={() => handlePreset(p)}
                className={`${styles.presetBtn} ${activePreset === p.label ? styles.presetActive : ''}`}
              >
                {p.label}
              </button>
            ))}
          </div>
          <div className={styles.dateRange}>
            <input
              type="date"
              value={range.from}
              onChange={(e) => handleDateChange('from', e.target.value)}
              className={styles.dateInput}
            />
            <span className={styles.dateSep}>~</span>
            <input
              type="date"
              value={range.to}
              onChange={(e) => handleDateChange('to', e.target.value)}
              className={styles.dateInput}
            />
          </div>
          <button onClick={handleExport} className={styles.exportBtn} disabled={loading}>
            엑셀 다운로드
          </button>
        </div>
      </header>

      <div className={styles.summary}>
        <div className={`${styles.card} ${styles.cardLive}`}>
          <p className={styles.cardLabel}>
            현재 방문자
            <span className={styles.liveDot} />
          </p>
          <p className={styles.cardValue}>{activeVisitors.toLocaleString()}</p>
          <p className={styles.cardSub}>실시간</p>
        </div>
        <div className={`${styles.card} ${styles.cardBlue}`}>
          <p className={styles.cardLabel}>총 페이지뷰</p>
          <p className={styles.cardValue}>{loading ? '—' : totalViews.toLocaleString()}</p>
          <p className={styles.cardSub}>선택 기간 합계</p>
        </div>
        <div className={`${styles.card} ${styles.cardPurple}`}>
          <p className={styles.cardLabel}>순방문자</p>
          <p className={styles.cardValue}>{loading ? '—' : totalVisitors.toLocaleString()}</p>
          <p className={styles.cardSub}>IP 기준 중복 제거</p>
        </div>
        <div className={`${styles.card} ${styles.cardOrange}`}>
          <p className={styles.cardLabel}>평균 체류시간</p>
          <p className={styles.cardValue}>{loading ? '—' : `${avgDuration}초`}</p>
          <p className={styles.cardSub}>일별 평균</p>
        </div>
      </div>

      {loading ? (
        <>
          <div className={styles.skeletonRow}>
            <SkeletonSection />
          </div>
          <div className={`${styles.row} ${styles.skeletonGrid}`}>
            <SkeletonSection />
            <SkeletonSection />
          </div>
          <div className={`${styles.row} ${styles.skeletonGrid}`}>
            <SkeletonSection />
            <SkeletonSection />
          </div>
        </>
      ) : (
        <>
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>일별 트래픽</h2>
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={daily}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" />
                <XAxis dataKey="statDate" tick={{ fontSize: 11, fill: '#8a90aa' }} />
                <YAxis tick={{ fontSize: 11, fill: '#8a90aa' }} />
                <Tooltip contentStyle={{ borderRadius: 8, border: '1px solid #e2e6f0', fontSize: 13 }} />
                <Line type="monotone" dataKey="totalViews" name="페이지뷰" stroke="#4f6ef7" strokeWidth={2.5} dot={false} />
                <Line type="monotone" dataKey="uniqueVisitors" name="순방문자" stroke="#48bb78" strokeWidth={2.5} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </section>

          <div className={styles.row}>
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>인기 페이지 TOP 10</h2>
              {pages.length === 0 ? (
                <p className={styles.noData}>데이터 없음</p>
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={pages} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" />
                    <XAxis type="number" tick={{ fontSize: 11, fill: '#8a90aa' }} />
                    <YAxis dataKey="pageUrl" type="category" width={160} tick={{ fontSize: 11, fill: '#8a90aa' }} />
                    <Tooltip contentStyle={{ borderRadius: 8, border: '1px solid #e2e6f0', fontSize: 13 }} />
                    <Bar dataKey="views" name="조회수" fill="#4f6ef7" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </section>

            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>유입 경로 TOP 10</h2>
              {referrers.length === 0 ? (
                <p className={styles.noData}>데이터 없음</p>
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={referrers} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" />
                    <XAxis type="number" tick={{ fontSize: 11, fill: '#8a90aa' }} />
                    <YAxis dataKey="referrer" type="category" width={160} tick={{ fontSize: 11, fill: '#8a90aa' }} />
                    <Tooltip contentStyle={{ borderRadius: 8, border: '1px solid #e2e6f0', fontSize: 13 }} />
                    <Bar dataKey="visits" name="방문수" fill="#48bb78" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </section>
          </div>

          <div className={styles.row}>
            <BreakdownPie data={devices} title="디바이스 분포" />
            <BreakdownPie data={browsers} title="브라우저 분포" />
          </div>
        </>
      )}
    </div>
  )
}
