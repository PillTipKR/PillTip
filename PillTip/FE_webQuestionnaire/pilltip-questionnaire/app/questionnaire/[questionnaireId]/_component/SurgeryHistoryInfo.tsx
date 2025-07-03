import styles from "./QuestionnaireDisplay.module.css";
import { SurgeryHistoryInfo } from "@/types/questionnaire";

export default function SurgeryHistoryInfoBlock({
  surgeryHistoryInfo,
}: {
  surgeryHistoryInfo: SurgeryHistoryInfo[];
}) {
  if (!surgeryHistoryInfo.some((surgery) => surgery.submitted)) return null;
  return (
    <div className={`${styles.infoCard} ${styles.surgeryHistoryInfo}`}>
      <h2 className={`${styles.infoTitle} ${styles.surgeryHistoryInfoTitle}`}>
        수술 이력
      </h2>
      <div className={styles.infoList}>
        {surgeryHistoryInfo
          .filter((surgery) => surgery.submitted)
          .map((surgery, idx) => (
            <div
              key={idx}
              className={`${styles.infoItem} ${styles.surgeryHistoryInfoItem}`}
            >
              • {surgery.surgeryHistory_name}
            </div>
          ))}
      </div>
    </div>
  );
}
