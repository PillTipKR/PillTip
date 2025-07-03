import styles from "./QuestionnaireDisplay.module.css";
import { MedicationInfo } from "@/types/questionnaire";

export default function MedicationInfoBlock({
  medicationInfo,
}: {
  medicationInfo: MedicationInfo[];
}) {
  if (!medicationInfo.some((med) => med.submitted)) return null;
  return (
    <div className={`${styles.infoCard} ${styles.medicationInfo}`}>
      <h2 className={`${styles.infoTitle} ${styles.medicationInfoTitle}`}>
        복용 중인 약물
      </h2>
      <div className={styles.infoList}>
        {medicationInfo
          .filter((med) => med.submitted)
          .map((med, idx) => (
            <div
              key={idx}
              className={`${styles.infoItem} ${styles.medicationInfoItem}`}
            >
              • 약물 ID: {med.medication_id}
            </div>
          ))}
      </div>
    </div>
  );
}
