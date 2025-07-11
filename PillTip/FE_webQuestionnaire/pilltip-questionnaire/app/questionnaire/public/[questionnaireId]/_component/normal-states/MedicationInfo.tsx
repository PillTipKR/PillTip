import styles from "./MedicationInfo.module.css";
import { MedicationInfo } from "@/types/questionnaire";

export default function MedicationInfoBlock({
  medicationInfo,
}: {
  medicationInfo: MedicationInfo[];
}) {
  if (!medicationInfo.some((med) => med.submitted)) return null;
  return (
    <div className={styles.infoCard}>
      <p className={styles.infoTitle}>복약 정보</p>
      <div className={styles.infoList}>
        {medicationInfo
          .filter((med) => med.submitted)
          .map((med, idx) => (
            <div key={idx} className={styles.infoItem}>
              <span className={styles.infoValue}>{med.medication_name}</span>
            </div>
          ))}
      </div>
    </div>
  );
}
