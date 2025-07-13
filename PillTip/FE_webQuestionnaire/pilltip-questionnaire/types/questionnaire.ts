// 문진표 관련 타입 정의

export interface MedicationInfo {
  medication_id: string;
  medication_name?: string;
  submitted: boolean;
}

export interface AllergyInfo {
  submitted: boolean;
  allergy_name: string;
}

export interface ChronicDiseaseInfo {
  submitted: boolean;
  chronicDisease_name: string;
}

export interface SurgeryHistoryInfo {
  surgeryHistory_name: string;
  submitted: boolean;
}

export interface QuestionnaireData {
  questionnaireId: number;
  questionnaireName: string;
  realName: string;
  address: string;
  issueDate: string;
  lastModifiedDate: string;
  notes: string | null;
  medicationInfo: string;
  allergyInfo: string;
  chronicDiseaseInfo: string;
  surgeryHistoryInfo: string;
}

export interface Questionnaire {
  id: string;
  title: string;
  description: string;
  data: any;
  status: string;
  expirationDate?: Date;
}

// API 응답 타입
export interface ApiResponse {
  status: "success" | "error";
  message: string;
  data?: QuestionnaireData;
  error?: string;
}

// 컴포넌트 Props 타입
export interface QuestionnaireDisplayProps {
  questionnaire: Questionnaire;
  timeUntilExpiration?: number | null;
}

export interface QuestionnaireComponentDeciderProps {
  questionnaireId: string;
}
