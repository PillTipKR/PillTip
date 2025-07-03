import styles from "./QuestionnaireDisplay.module.css";
import { ChronicDiseaseInfo } from "@/types/questionnaire";

export default function ChronicDiseaseInfoBlock({
  chronicDiseaseInfo,
}: {
  chronicDiseaseInfo: ChronicDiseaseInfo[];
}) {
  if (!chronicDiseaseInfo.some((disease) => disease.submitted)) return null;
  return (
    <div className={`${styles.infoCard} ${styles.chronicDiseaseInfo}`}>
      <h2 className={`${styles.infoTitle} ${styles.chronicDiseaseInfoTitle}`}>
        만성질환 정보
      </h2>
      <div className={styles.infoList}>
        {chronicDiseaseInfo
          .filter((disease) => disease.submitted)
          .map((disease, idx) => (
            <div
              key={idx}
              className={`${styles.infoItem} ${styles.chronicDiseaseInfoItem}`}
            >
              • {disease.chronicDisease_name}
            </div>
          ))}
      </div>
    </div>
  );
}
