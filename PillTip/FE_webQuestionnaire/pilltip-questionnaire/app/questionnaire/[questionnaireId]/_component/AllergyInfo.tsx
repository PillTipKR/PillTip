import styles from "./QuestionnaireDisplay.module.css";
import { AllergyInfo } from "@/types/questionnaire";

export default function AllergyInfoBlock({
  allergyInfo,
}: {
  allergyInfo: AllergyInfo[];
}) {
  if (!allergyInfo.some((allergy) => allergy.submitted)) return null;
  return (
    <div className={`${styles.infoCard} ${styles.allergyInfo}`}>
      <h2 className={`${styles.infoTitle} ${styles.allergyInfoTitle}`}>
        알레르기 정보
      </h2>
      <div className={styles.infoList}>
        {allergyInfo
          .filter((allergy) => allergy.submitted)
          .map((allergy, idx) => (
            <div
              key={idx}
              className={`${styles.infoItem} ${styles.allergyInfoItem}`}
            >
              • {allergy.allergy_name}
            </div>
          ))}
      </div>
    </div>
  );
}
