import React from "react";

// 문진표 데이터를 TXT로 변환하는 함수
function generateQuestionnaireTxt(questionnaire: any): string {
  let content = "";

  // 헤더
  content += "=".repeat(50) + "\n";
  content += "문진표\n";
  content += "=".repeat(50) + "\n\n";

  // 기본 정보
  content += "[기본 정보]\n";
  content += `환자명: ${questionnaire.realName || "미입력"}\n`;
  content += `주소: ${questionnaire.address || "미입력"}\n`;
  content += `작성일: ${questionnaire.issueDate || "미입력"}\n`;
  content += `최종수정일: ${questionnaire.lastModifiedDate || "미입력"}\n`;
  content += `문진표명: ${questionnaire.questionnaireName || "미입력"}\n\n`;

  // 추가 메모
  if (questionnaire.notes) {
    content += "[추가 메모]\n";
    content += `${questionnaire.notes}\n\n`;
  }

  // 복용 중인 약물
  content += "[복용 중인 약물]\n";
  const medicationInfo = questionnaire.medicationInfo || [];
  const filteredMedication = medicationInfo.filter((med: any) => med.submitted);
  if (filteredMedication.length > 0) {
    filteredMedication.forEach((med: any, index: number) => {
      content += `${index + 1}. 약물 ID: ${
        med.medicationId || med.medication_id || "미입력"
      }\n`;
      content += `   복용 여부: 복용 중\n`;
    });
  } else {
    content += "복용 중인 약물 없음\n";
  }
  content += "\n";

  // 알레르기 정보
  content += "[알레르기 정보]\n";
  const allergyInfo = questionnaire.allergyInfo || [];
  const filteredAllergy = allergyInfo.filter(
    (allergy: any) => allergy.submitted
  );
  if (filteredAllergy.length > 0) {
    filteredAllergy.forEach((allergy: any, index: number) => {
      content += `${index + 1}. 알레르기: ${
        allergy.allergyName || allergy.allergy_name || "미입력"
      }\n`;
      content += `   알레르기 여부: 있음\n`;
    });
  } else {
    content += "알레르기 정보 없음\n";
  }
  content += "\n";

  // 만성질환 정보
  content += "[만성질환 정보]\n";
  const chronicDiseaseInfo = questionnaire.chronicDiseaseInfo || [];
  const filteredChronic = chronicDiseaseInfo.filter(
    (disease: any) => disease.submitted
  );
  if (filteredChronic.length > 0) {
    filteredChronic.forEach((disease: any, index: number) => {
      content += `${index + 1}. 질환: ${
        disease.chronicDiseaseName || disease.chronicDisease_name || "미입력"
      }\n`;
      content += `   보유 여부: 있음\n`;
    });
  } else {
    content += "만성질환 정보 없음\n";
  }
  content += "\n";

  // 수술 이력
  content += "[수술 이력]\n";
  const surgeryHistoryInfo = questionnaire.surgeryHistoryInfo || [];
  const filteredSurgery = surgeryHistoryInfo.filter(
    (surgery: any) => surgery.submitted
  );
  if (filteredSurgery.length > 0) {
    filteredSurgery.forEach((surgery: any, index: number) => {
      content += `${index + 1}. 수술: ${
        surgery.surgeryHistoryName || surgery.surgeryHistory_name || "미입력"
      }\n`;
      content += `   수술 여부: 있음\n`;
    });
  } else {
    content += "수술 이력 없음\n";
  }
  content += "\n";

  // 푸터
  content += "=".repeat(50) + "\n";
  content += `생성일시: ${new Date().toLocaleString("ko-KR")}\n`;
  content += "=".repeat(50) + "\n";

  return content;
}

export function downloadTxtFile(questionnaire: any) {
  const txtContent = generateQuestionnaireTxt(questionnaire);
  const patientName = questionnaire.realName || "환자";
  const issueDate =
    questionnaire.issueDate || new Date().toISOString().split("T")[0];
  const fileName = `${patientName}_문진표_${issueDate}.txt`;

  const blob = new Blob([txtContent], { type: "text/plain;charset=utf-8" });
  const url = window.URL.createObjectURL(blob);

  const a = document.createElement("a");
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  window.URL.revokeObjectURL(url);
}

// 사용 예시: <QuestionnaireTxtDownloadButton questionnaire={questionnaire} />
export default function QuestionnaireTxtDownloadButton({
  questionnaire,
}: {
  questionnaire: any;
}) {
  return (
    <button onClick={() => downloadTxtFile(questionnaire)}>
      TXT 파일 다운로드
    </button>
  );
}
