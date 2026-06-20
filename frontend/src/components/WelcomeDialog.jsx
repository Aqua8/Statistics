import { useState } from 'react'
import styles from './WelcomeDialog.module.css'

const STORAGE_KEY = 'hideWelcomeDate'

function getTodayString() {
  return new Date().toISOString().slice(0, 10)
}

export function shouldShowWelcome() {
  return localStorage.getItem(STORAGE_KEY) !== getTodayString()
}

export default function WelcomeDialog({ onClose }) {
  const [dontShowToday, setDontShowToday] = useState(false)

  const handleClose = () => {
    if (dontShowToday) {
      localStorage.setItem(STORAGE_KEY, getTodayString())
    }
    onClose()
  }

  return (
    <div className={styles.overlay} onClick={handleClose}>
      <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <div className={styles.brandRow}>
            <div className={styles.brandIcon}>S</div>
            <span className={styles.brandName}>StatDash</span>
          </div>
        </div>

        <div className={styles.body}>
          <h2 className={styles.title}>안녕하세요! 👋</h2>
          <p className={styles.desc}>
            이 웹사이트는 <strong>포트폴리오 목적으로 제작된 웹 분석 대시보드</strong>입니다.
          </p>
          <p className={styles.desc}>
            사이트에 설치된 트래커로 방문자 통계, 페이지뷰, 유입 경로 등을 실시간으로
            수집하고 시각화하는 서비스입니다.
          </p>

          <div className={styles.notice}>
            <span className={styles.noticeIcon}>💡</span>
            <div>
              <strong>포트폴리오 열람을 원하신다면</strong>
              <br />
              계정이 없어도 괜찮습니다. 로그인 화면에서{' '}
              <strong>&ldquo;게스트로 입장&rdquo;</strong> 버튼을 눌러 둘러보세요.
            </div>
          </div>
        </div>

        <div className={styles.footer}>
          <label className={styles.checkLabel}>
            <input
              type="checkbox"
              checked={dontShowToday}
              onChange={(e) => setDontShowToday(e.target.checked)}
              className={styles.checkbox}
            />
            오늘 하루 동안 보지 않기
          </label>
          <button className={styles.closeBtn} onClick={handleClose}>
            확인
          </button>
        </div>
      </div>
    </div>
  )
}
