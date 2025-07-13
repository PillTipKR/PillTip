import styles from "./ChronicDiseaseInfo.module.css";
import { ChronicDiseaseInfo } from "@/types/questionnaire";

export default function ChronicDiseaseInfoBlock({
  chronicDiseaseInfo,
}: {
  chronicDiseaseInfo: ChronicDiseaseInfo[];
}) {
  return (
    <div className={styles.infoCard}>
      <p className={styles.infoTitle}>만성질환 정보</p>
      <div className={styles.infoList}>
        {chronicDiseaseInfo.length > 0 ? (
          chronicDiseaseInfo.map((disease, idx) => (
            <div key={idx} className={styles.infoItem}>
              <span className={styles.infoValue}>
                {disease.chronicDisease_name}
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
