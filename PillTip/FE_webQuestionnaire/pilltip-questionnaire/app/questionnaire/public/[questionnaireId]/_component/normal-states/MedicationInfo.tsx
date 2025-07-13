import styles from "./MedicationInfo.module.css";
import { MedicationInfo } from "@/types/questionnaire";

export default function MedicationInfoBlock({
  medicationInfo,
}: {
  medicationInfo: MedicationInfo[];
}) {
  return (
    <div className={styles.infoCard}>
      <p className={styles.infoTitle}>복약 정보</p>
      <div className={styles.infoList}>
        {medicationInfo.length > 0 ? (
          medicationInfo.map((med, idx) => (
            <div key={idx} className={styles.infoItem}>
              <span className={styles.infoValue}>{med.medication_name}</span>
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
