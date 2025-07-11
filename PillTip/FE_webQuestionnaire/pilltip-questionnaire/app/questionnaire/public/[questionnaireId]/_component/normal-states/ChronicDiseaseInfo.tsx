import styles from "./ChronicDiseaseInfo.module.css";
import { ChronicDiseaseInfo } from "@/types/questionnaire";

export default function ChronicDiseaseInfoBlock({
  chronicDiseaseInfo,
}: {
  chronicDiseaseInfo: ChronicDiseaseInfo[];
}) {
  if (!chronicDiseaseInfo.some((disease) => disease.submitted)) return null;
  return (
    <div className={styles.infoCard}>
      <p className={styles.infoTitle}>만성질환 정보</p>
      <div className={styles.infoList}>
        {chronicDiseaseInfo
          .filter((disease) => disease.submitted)
          .map((disease, idx) => (
            <div key={idx} className={styles.infoItem}>
              <span className={styles.infoValue}>
                {disease.chronicDisease_name}
              </span>
            </div>
          ))}
      </div>
    </div>
  );
}
