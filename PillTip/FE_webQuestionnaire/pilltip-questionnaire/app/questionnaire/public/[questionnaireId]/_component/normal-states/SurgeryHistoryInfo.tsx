import styles from "./SurgeryHistoryInfo.module.css";
import { SurgeryHistoryInfo } from "@/types/questionnaire";

export default function SurgeryHistoryInfoBlock({
  surgeryHistoryInfo,
}: {
  surgeryHistoryInfo: SurgeryHistoryInfo[];
}) {
  return (
    <div className={`${styles.infoCard} ${styles.surgeryHistoryInfo}`}>
      <p className={styles.infoTitle}>수술 이력</p>
      <div className={styles.infoList}>
        {surgeryHistoryInfo.length > 0 ? (
          surgeryHistoryInfo.map((surgery, idx) => (
            <div key={idx} className={styles.infoItem}>
              <span className={styles.infoValue}>
                {surgery.surgeryHistory_name}
              </span>
            </div>
          ))
        ) : (
          <div className={styles.infoItem}>
            <span className={styles.infoValue}>없음</span>
          </div>
        )}
      </div>
    </div>
  );
}
