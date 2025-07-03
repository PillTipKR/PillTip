import styles from "./QuestionnaireDisplay.module.css";

export default function Notes({ notes }: { notes: string }) {
  if (!notes) return null;
  return (
    <div className={styles.notes}>
      <h2 className={styles.notesTitle}>특이사항</h2>
      <p className={styles.notesContent}>{notes}</p>
    </div>
  );
}
