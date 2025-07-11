import styles from "./Notes.module.css";

export default function Notes({ notes }: { notes: string }) {
  if (!notes) return null;
  return (
    <div className={styles.infoCard}>
      <span className={styles.infoTitle}>특이사항</span>
      <div className={styles.infoList}>
        <div className={styles.infoItem}>
          <span className={styles.infoValue}>
            {notes.replace(/^특이사항[:：]?\s*/, "")}
          </span>
        </div>
      </div>
    </div>
  );
}
