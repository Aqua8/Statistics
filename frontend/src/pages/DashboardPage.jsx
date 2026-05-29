import { useState, useEffect, useMemo, memo } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, PieChart, Pie, Cell, Legend,
} from 'recharts'
import * as XLSX from 'xlsx'
import { getDailyStats, getPageStats, getReferrerStats, getDeviceStats, getBrowserStats } from '../api/stats'
import styles from './DashboardPage.module.css'

const PIE_COLORS = ['#4f6ef7', '#48bb78', '#ed8936', '#e53e3e', '#805ad5', '#319795']

// Recharts Tooltip 공통 스타일 — 매 렌더마다 새 객체가 생기지 않도록 상수로 추출
const TOOLTIP_STYLE = { borderRadius: 8, border: '1px solid #e2e6f0', fontSize: 13 }

// 로컬 타임존 기준 yyyy-MM-dd 포맷 (toISOString은 UTC라 KST 오전엔 하루 밀림)
function toDateStr(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
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

function BreakdownPie({ data, title, description }) {
  return (
    <section className={styles.section}>
      <h2 className={styles.sectionTitle}>
        {title}
        <InfoButton description={description} />
      </h2>
      {data.length === 0 ? (
        <p className={styles.noData}>데이터 없음</p>
      ) : (
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
      )}
    </section>
  )
}

function SkeletonSection() {
  return <div className={styles.skeletonSection} />
}

function InfoButton({ description }) {
  const [open, setOpen] = useState(false)
  return (
    <>
      <button className={styles.infoBtn} onClick={() => setOpen(true)}>?</button>
      {open && (
        <div className={styles.modalOverlay} onClick={() => setOpen(false)}>
          <div className={styles.modalBox} onClick={(e) => e.stopPropagation()}>
            <p className={styles.modalDesc}>{description}</p>
            <button className={styles.modalClose} onClick={() => setOpen(false)}>닫기</button>
          </div>
        </div>
      )}
    </>
  )
}

// 차트 블록을 memo로 분리 — SSE 실시간 방문자(activeVisitors)가 5초마다 갱신돼도
// daily/pages/referrers/devices/browsers가 그대로면 차트는 리렌더되지 않음
const ChartsBlock = memo(function ChartsBlock({ daily, pages, referrers, devices, browsers }) {
  return (
    <>
      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>
          일별 트래픽
          <InfoButton description={"Spring Batch가 매일 새벽 1시에 전날 로그를 집계한 결과입니다.\n\n• 페이지뷰: pageview 이벤트 총 횟수\n• 순방문자: 고유 IP 기준 중복 제거\n\n오늘 데이터는 내일 새벽 1시 이후 반영됩니다."} />
        </h2>
        <ResponsiveContainer width="100%" height={240}>
          <LineChart data={daily}>
            <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" />
            <XAxis dataKey="statDate" tick={{ fontSize: 11, fill: '#8a90aa' }} />
            <YAxis tick={{ fontSize: 11, fill: '#8a90aa' }} />
            <Tooltip contentStyle={TOOLTIP_STYLE} />
            <Line type="monotone" dataKey="totalViews" name="페이지뷰" stroke="#4f6ef7" strokeWidth={2.5} dot={false} />
            <Line type="monotone" dataKey="uniqueVisitors" name="순방문자" stroke="#48bb78" strokeWidth={2.5} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </section>

      <div className={styles.row}>
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>
            인기 페이지 TOP 10
            <InfoButton description={"선택 기간 동안 가장 많이 조회된 페이지 상위 10개입니다.\n\n• 조회수: pageview 이벤트 기준\n• 순방문자: 고유 IP 기준 중복 제거"} />
          </h2>
          {pages.length === 0 ? (
            <p className={styles.noData}>데이터 없음</p>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={pages} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" />
                <XAxis type="number" tick={{ fontSize: 11, fill: '#8a90aa' }} />
                <YAxis dataKey="pageUrl" type="category" width={160} tick={{ fontSize: 11, fill: '#8a90aa' }} />
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Bar dataKey="views" name="조회수" fill="#4f6ef7" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </section>

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>
            유입 경로 TOP 10
            <InfoButton description={"방문자가 어떤 외부 사이트를 통해 유입됐는지 보여줍니다.\n\n• 직접 접속(referrer 없음)은 집계에서 제외됩니다.\n• 선택 기간 내 방문 수 기준 TOP 10입니다."} />
          </h2>
          {referrers.length === 0 ? (
            <p className={styles.noData}>데이터 없음</p>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={referrers} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" />
                <XAxis type="number" tick={{ fontSize: 11, fill: '#8a90aa' }} />
                <YAxis dataKey="referrer" type="category" width={160} tick={{ fontSize: 11, fill: '#8a90aa' }} />
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Bar dataKey="visits" name="방문수" fill="#48bb78" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </section>
      </div>

      <div className={styles.row}>
        <BreakdownPie data={devices} title="디바이스 분포" description={"방문자의 접속 디바이스 유형 비율입니다.\n\nUser-Agent를 분석해 아래로 분류합니다.\n• mobile: 스마트폰\n• tablet: 태블릿\n• desktop: PC / 노트북"} />
        <BreakdownPie data={browsers} title="브라우저 분포" description={"방문자가 사용한 브라우저 비율입니다.\n\nUser-Agent를 분석해 Chrome / Firefox / Safari / Edge / IE / Other로 분류합니다."} />
      </div>
    </>
  )
})

// 실시간 방문자 카드 분리 — SSE 갱신 시 이 컴포넌트만 리렌더
const LiveVisitorCard = memo(function LiveVisitorCard({ projectId }) {
  const [activeVisitors, setActiveVisitors] = useState(0)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const sse = new EventSource(`/api/projects/${projectId}/stats/realtime?token=${token}`)
    sse.addEventListener('visitors', (e) => setActiveVisitors(parseInt(e.data, 10)))
    // 인증 오류 등으로 연결이 끊기면 닫고, 일시적 오류는 브라우저 자동 재연결에 맡김
    sse.onerror = () => { if (sse.readyState === EventSource.CLOSED) sse.close() }
    return () => sse.close()
  }, [projectId])

  return (
    <div className={`${styles.card} ${styles.cardLive}`}>
      <p className={styles.cardLabel}>
        현재 방문자
        <span className={styles.liveDot} />
      </p>
      <p className={styles.cardValue}>{activeVisitors.toLocaleString()}</p>
      <p className={styles.cardSub}>실시간</p>
    </div>
  )
})

export default function DashboardPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const projectId = location.state?.projectId

  // state 없이 직접 URL 접근(새로고침 등)하면 프로젝트 목록으로 이동
  useEffect(() => {
    if (!projectId) navigate('/projects', { replace: true })
  }, [projectId, navigate])

  const [range, setRange] = useState(() => ({ from: daysAgo(13), to: daysAgo(0) }))
  const [activePreset, setActivePreset] = useState('14일')
  const [daily, setDaily] = useState([])
  const [pages, setPages] = useState([])
  const [referrers, setReferrers] = useState([])
  const [devices, setDevices] = useState([])
  const [browsers, setBrowsers] = useState([])
  const [loading, setLoading] = useState(true)

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
    setRange((prev) => {
      const next = { ...prev, [key]: value }
      // from > to 역전 방지
      if (next.from > next.to) {
        return key === 'from' ? { from: value, to: value } : { from: value, to: prev.to }
      }
      return next
    })
  }

  // daily가 바뀔 때만 재계산 (불필요한 매 렌더 합산 방지)
  const { totalViews, totalVisitors, avgDuration } = useMemo(() => ({
    totalViews: daily.reduce((s, d) => s + (d.totalViews ?? 0), 0),
    totalVisitors: daily.reduce((s, d) => s + (d.uniqueVisitors ?? 0), 0),
    avgDuration: daily.length
      ? Math.round(daily.reduce((s, d) => s + (d.avgDuration ?? 0), 0) / daily.length / 1000)
      : 0,
  }), [daily])

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
        <LiveVisitorCard projectId={projectId} />
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
        <ChartsBlock daily={daily} pages={pages} referrers={referrers} devices={devices} browsers={browsers} />
      )}
    </div>
  )
}
