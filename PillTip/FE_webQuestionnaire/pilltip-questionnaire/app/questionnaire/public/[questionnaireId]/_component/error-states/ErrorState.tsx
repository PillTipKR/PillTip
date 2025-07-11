import styles from "./ErrorState.module.css";

interface ErrorStateProps {
  error: string;
  onRetry: () => void;
}

export default function ErrorState({ error, onRetry }: ErrorStateProps) {
  const isTokenExpired = error.includes("JWT 토큰이 만료되었습니다");

  return (
    <div className={styles.errorContainer}>
      <div className={styles.errorContent}>
        <div className={styles.errorIcon}>{isTokenExpired ? "⏰" : "⚠️"}</div>
        <h2 className={styles.errorTitle}>
          {isTokenExpired ? "토큰 만료" : "오류 발생"}
        </h2>
        <p className={styles.errorMessage}>{error}</p>
        {isTokenExpired && (
          <div className={styles.infoBox}>
            <p className={styles.infoText}>
              문진표를 다시 받으시려면 환자분께 문의해 주세요.
            </p>
          </div>
        )}
        <button onClick={onRetry} className={styles.retryButton}>
          다시 시도
        </button>
      </div>
    </div>
  );
}
