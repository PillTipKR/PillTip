"use client";

import { useEffect, useState } from "react";
import styles from "./QuestionnaireDisplay.module.css";
import BasicInfo from "./BasicInfo";
import MedicationInfoBlock from "./MedicationInfo";
import AllergyInfoBlock from "./AllergyInfo";
import ChronicDiseaseInfoBlock from "./ChronicDiseaseInfo";
import SurgeryHistoryInfoBlock from "./SurgeryHistoryInfo";
import Notes from "./Notes";
import { downloadTxtFile } from "@/app/questionnaire/QuestionnaireTxtDownloadButton";
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
  const [isDownloading, setIsDownloading] = useState(false);
  const [remainingSeconds, setRemainingSeconds] = useState<number | null>(null);

  function safeParse(data: any) {
    if (Array.isArray(data)) return data;
    if (typeof data === "string") {
      try {
        return JSON.parse(data);
      } catch {
        return [];
      }
    }
    return [];
  }

  // medicationInfo: medication_id or medicationId
  const medicationInfoRaw = questionnaire.data.medicationInfo;
  const medicationInfo = safeParse(medicationInfoRaw).map((item: any) => ({
    medication_id: item.medication_id ?? item.medicationId ?? "",
    medication_name: item.medication_name ?? item.medicationName ?? "",
    submitted: item.submitted,
  }));

  // allergyInfo: allergy_name or allergyName
  const allergyInfoRaw = questionnaire.data.allergyInfo;
  const allergyInfo = safeParse(allergyInfoRaw).map((item: any) => ({
    allergy_name: item.allergy_name ?? item.allergyName ?? "",
    submitted: item.submitted,
  }));

  // chronicDiseaseInfo: chronicDisease_name or chronicDiseaseName
  const chronicDiseaseInfoRaw = questionnaire.data.chronicDiseaseInfo;
  const chronicDiseaseInfo = safeParse(chronicDiseaseInfoRaw).map(
    (item: any) => ({
      chronicDisease_name:
        item.chronicDisease_name ?? item.chronicDiseaseName ?? "",
      submitted: item.submitted,
    })
  );

  // surgeryHistoryInfo: surgeryHistory_name or surgeryHistoryName
  const surgeryHistoryInfoRaw = questionnaire.data.surgeryHistoryInfo;
  const surgeryHistoryInfo = safeParse(surgeryHistoryInfoRaw).map(
    (item: any) => ({
      surgeryHistory_name:
        item.surgeryHistory_name ?? item.surgeryHistoryName ?? "",
      submitted: item.submitted,
    })
  );

  useEffect(() => {
    let timer: NodeJS.Timeout | null = null;
    if (questionnaire && questionnaire.expirationDate) {
      const expirationTime = new Date(questionnaire.expirationDate).getTime();
      const updateRemaining = () => {
        const currentTime = new Date().getTime();
        const remainingTime = expirationTime - currentTime;
        setRemainingSeconds(
          remainingTime > 0 ? Math.ceil(remainingTime / 1000) : 0
        );
      };
      updateRemaining();
      timer = setInterval(updateRemaining, 1000);
      if (expirationTime - new Date().getTime() > 0) {
        const reloadTimer = setTimeout(() => {
          window.location.reload();
        }, expirationTime - new Date().getTime());
        // 컴포넌트 언마운트 시 타이머 정리
        return () => {
          clearInterval(timer!);
          clearTimeout(reloadTimer);
        };
      } else {
        // 이미 만료된 경우 바로 새로고침
        window.location.reload();
      }
    }
    return () => {
      if (timer) clearInterval(timer);
    };
  }, [questionnaire]);

  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        <div className={styles.card}>
          <div className={styles.header}>
            <h1 className={styles.title}>
              {questionnaire.data.questionnaireName}
            </h1>
            <div className={styles.questionnaireId}>
              {questionnaire.data.questionnaireId}
            </div>
          </div>

          <button
            onClick={() => {
              setIsDownloading(true);
              downloadTxtFile({
                ...questionnaire.data,
                medicationInfo,
                allergyInfo,
                chronicDiseaseInfo,
                surgeryHistoryInfo,
              });
              setIsDownloading(false);
            }}
            disabled={isDownloading}
            className={styles.downloadButton}
          >
            {isDownloading ? "다운로드 중..." : "TXT 파일 다운로드"}
          </button>

          <div className={styles.divider} />

          <div className={styles.grid}>
            <BasicInfo data={questionnaire.data} />
            <MedicationInfoBlock medicationInfo={medicationInfo} />
            <AllergyInfoBlock allergyInfo={allergyInfo} />
            <ChronicDiseaseInfoBlock chronicDiseaseInfo={chronicDiseaseInfo} />
            <SurgeryHistoryInfoBlock surgeryHistoryInfo={surgeryHistoryInfo} />
          </div>
          <div className={styles.divider} />
          <div className={styles.infoItem}>
            <Notes notes={questionnaire.data.notes ?? ""} />
            <span> 작성일 : {questionnaire.data.lastModifiedDate}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
