import styles from "./BasicInfo.module.css";

export default function BasicInfo({ data }: { data: any }) {
  return (
    <div className={styles.infoCard}>
      <div className={styles.infoList}>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>이름</span>
          <span className={styles.infoValue}>{data.realName}</span>
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>생년월일</span>
          <span className={styles.infoValue}>{data.birthDate}</span>
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>성별</span>
          <span className={styles.infoValue}>
            {data.gender === "MALE"
              ? "남성"
              : data.gender === "FEMALE"
              ? "여성"
              : "미입력"}
          </span>
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>전화번호</span>
          <span className={styles.infoValue}>{data.phoneNumber}</span>
        </div>
        <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
          <span className={styles.infoLabel}>주소</span>
          <span className={styles.infoValue}>{data.address}</span>
        </div>
      </div>
    </div>
  );
}
