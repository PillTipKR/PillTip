import styles from "./QuestionnaireDisplay.module.css";

export default function BasicInfo({ data }: { data: any }) {
  return (
    <div className={`${styles.infoCard} ${styles.basicInfo}`}>
      <h2 className={`${styles.infoTitle} ${styles.basicInfoTitle}`}>
        기본 정보
      </h2>
      <div className={styles.infoList}>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>이름:</span> {data.realName}
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>주소:</span> {data.address}
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>전화번호:</span> {data.phoneNumber}
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>발행일:</span> {data.issueDate}
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>최종 수정일:</span>{" "}
          {data.lastModifiedDate}
        </div>
      </div>
    </div>
  );
}
