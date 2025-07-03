"use client";

import { useState } from "react";
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
              {isDownloading ? "다운로드 중..." : "📄 TXT 다운로드"}
            </button>
          </div>

          <div className={styles.grid}>
            <BasicInfo data={questionnaire.data} />
            <MedicationInfoBlock medicationInfo={medicationInfo} />
            <AllergyInfoBlock allergyInfo={allergyInfo} />
            <ChronicDiseaseInfoBlock chronicDiseaseInfo={chronicDiseaseInfo} />
            <SurgeryHistoryInfoBlock surgeryHistoryInfo={surgeryHistoryInfo} />
          </div>

          <Notes notes={questionnaire.data.notes ?? ""} />
        </div>
      </div>
    </div>
  );
}
