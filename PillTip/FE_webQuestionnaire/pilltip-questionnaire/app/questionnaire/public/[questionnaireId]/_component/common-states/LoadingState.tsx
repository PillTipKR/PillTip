import styles from "./LoadingState.module.css";

export default function LoadingState() {
  return (
    <div className={styles.loadingContainer}>
      <div className={styles.loadingContent}>
        <div className={styles.spinner}></div>
        <p className={styles.loadingText}>문진표를 불러오는 중...</p>
      </div>
    </div>
  );
}
