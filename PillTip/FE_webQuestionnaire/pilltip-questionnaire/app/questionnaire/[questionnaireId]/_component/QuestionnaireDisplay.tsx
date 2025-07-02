"use client";

import { useRouter } from "next/navigation";
import styles from "./QuestionnaireDisplay.module.css";
import {
  MedicationInfo,
  AllergyInfo,
  ChronicDiseaseInfo,
  SurgeryHistoryInfo,
  QuestionnaireDisplayProps,
} from "@/types/questionnaire";

export default function QuestionnaireDisplay({
  questionnaire,
}: QuestionnaireDisplayProps) {
  const router = useRouter();

  // JSON 문자열을 파싱
  const medicationInfo: MedicationInfo[] = JSON.parse(
    questionnaire.data.medicationInfo
  );
  const allergyInfo: AllergyInfo[] = JSON.parse(questionnaire.data.allergyInfo);
  const chronicDiseaseInfo: ChronicDiseaseInfo[] = JSON.parse(
    questionnaire.data.chronicDiseaseInfo
  );
  const surgeryHistoryInfo: SurgeryHistoryInfo[] = JSON.parse(
    questionnaire.data.surgeryHistoryInfo
  );

  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        <div className={styles.card}>
          <div className={styles.header}>
            <h1 className={styles.title}>
              {questionnaire.data.questionnaireName}
            </h1>
            <p className={styles.subtitle}>문진표 정보</p>
            <div className={styles.questionnaireId}>
              문진표 ID: {questionnaire.data.questionnaireId}
            </div>
          </div>

          <div className={styles.grid}>
            {/* 기본 정보 */}
            <div className={`${styles.infoCard} ${styles.basicInfo}`}>
              <h2 className={`${styles.infoTitle} ${styles.basicInfoTitle}`}>
                기본 정보
              </h2>
              <div className={styles.infoList}>
                <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
                  <span className={styles.infoLabel}>이름:</span>{" "}
                  {questionnaire.data.realName}
                </div>
                <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
                  <span className={styles.infoLabel}>주소:</span>{" "}
                  {questionnaire.data.address}
                </div>
                <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
                  <span className={styles.infoLabel}>발행일:</span>{" "}
                  {questionnaire.data.issueDate}
                </div>
                <div className={`${styles.infoItem} ${styles.basicInfoItem}`}>
                  <span className={styles.infoLabel}>최종 수정일:</span>{" "}
                  {questionnaire.data.lastModifiedDate}
                </div>
              </div>
            </div>

            {/* 복용 중인 약물 */}
            {medicationInfo.some((med) => med.submitted) && (
              <div className={`${styles.infoCard} ${styles.medicationInfo}`}>
                <h2
                  className={`${styles.infoTitle} ${styles.medicationInfoTitle}`}
                >
                  복용 중인 약물
                </h2>
                <div className={styles.infoList}>
                  {medicationInfo
                    .filter((med) => med.submitted)
                    .map((med, index) => (
                      <div
                        key={index}
                        className={`${styles.infoItem} ${styles.medicationInfoItem}`}
                      >
                        • 약물 ID: {med.medication_id}
                      </div>
                    ))}
                </div>
              </div>
            )}

            {/* 알레르기 정보 */}
            {allergyInfo.some((allergy) => allergy.submitted) && (
              <div className={`${styles.infoCard} ${styles.allergyInfo}`}>
                <h2
                  className={`${styles.infoTitle} ${styles.allergyInfoTitle}`}
                >
                  알레르기 정보
                </h2>
                <div className={styles.infoList}>
                  {allergyInfo
                    .filter((allergy) => allergy.submitted)
                    .map((allergy, index) => (
                      <div
                        key={index}
                        className={`${styles.infoItem} ${styles.allergyInfoItem}`}
                      >
                        • {allergy.allergy_name}
                      </div>
                    ))}
                </div>
              </div>
            )}

            {/* 만성질환 정보 */}
            {chronicDiseaseInfo.some((disease) => disease.submitted) && (
              <div
                className={`${styles.infoCard} ${styles.chronicDiseaseInfo}`}
              >
                <h2
                  className={`${styles.infoTitle} ${styles.chronicDiseaseInfoTitle}`}
                >
                  만성질환 정보
                </h2>
                <div className={styles.infoList}>
                  {chronicDiseaseInfo
                    .filter((disease) => disease.submitted)
                    .map((disease, index) => (
                      <div
                        key={index}
                        className={`${styles.infoItem} ${styles.chronicDiseaseInfoItem}`}
                      >
                        • {disease.chronicDisease_name}
                      </div>
                    ))}
                </div>
              </div>
            )}

            {/* 수술 이력 */}
            {surgeryHistoryInfo.some((surgery) => surgery.submitted) && (
              <div
                className={`${styles.infoCard} ${styles.surgeryHistoryInfo}`}
              >
                <h2
                  className={`${styles.infoTitle} ${styles.surgeryHistoryInfoTitle}`}
                >
                  수술 이력
                </h2>
                <div className={styles.infoList}>
                  {surgeryHistoryInfo
                    .filter((surgery) => surgery.submitted)
                    .map((surgery, index) => (
                      <div
                        key={index}
                        className={`${styles.infoItem} ${styles.surgeryHistoryInfoItem}`}
                      >
                        • {surgery.surgeryHistory_name}
                      </div>
                    ))}
                </div>
              </div>
            )}
          </div>

          {/* 특이사항 */}
          {questionnaire.data.notes && (
            <div className={styles.notes}>
              <h2 className={styles.notesTitle}>특이사항</h2>
              <p className={styles.notesContent}>{questionnaire.data.notes}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
