import styles from "./NotFoundState.module.css";

export default function NotFoundState() {
  return (
    <div className={styles.notFoundContainer}>
      <div className={styles.notFoundContent}>
        <p className={styles.notFoundText}>문진표를 찾을 수 없습니다.</p>
      </div>
    </div>
  );
}
