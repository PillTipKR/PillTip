import styles from "./AllergyInfo.module.css";
import { AllergyInfo } from "@/types/questionnaire";

export default function AllergyInfoBlock({
  allergyInfo,
}: {
  allergyInfo: AllergyInfo[];
}) {
  if (!allergyInfo.some((allergy) => allergy.submitted)) return null;
  return (
    <div className={styles.infoCard}>
      <span className={styles.infoTitle}>알레르기 정보</span>
      <div className={styles.infoList}>
        {allergyInfo
          .filter((allergy) => allergy.submitted)
          .map((allergy, idx) => (
            <div key={idx} className={styles.infoItem}>
              <span className={styles.infoValue}>{allergy.allergy_name}</span>
            </div>
          ))}
      </div>
    </div>
  );
}
