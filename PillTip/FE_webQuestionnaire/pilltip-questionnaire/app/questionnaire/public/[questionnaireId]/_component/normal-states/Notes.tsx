import styles from "./Notes.module.css";

export default function Notes({ notes }: { notes: string }) {
  return (
    <div className={styles.infoCard}>
      <span className={styles.infoTitle}>특이사항</span>
      <div className={styles.infoList}>
        <div className={styles.infoItem}>
          <span className={styles.infoValue}>
            {notes && notes.trim()
              ? notes.replace(/^특이사항[:：]?\s*/, "")
              : "없음"}
          </span>
        </div>
      </div>
    </div>
  );
}
