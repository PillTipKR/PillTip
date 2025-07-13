"use client";

import { useState, useEffect } from "react";
import QuestionnaireDisplay from "./normal-states/QuestionnaireDisplay";
import LoadingState from "./common-states/LoadingState";
import ErrorState from "./error-states/ErrorState";
import NotFoundState from "./error-states/NotFoundState";
import {
  Questionnaire,
  QuestionnaireComponentDeciderProps,
} from "@/types/questionnaire";

// 문진표 컴포넌트 결정자 - 문진표 ID에 따라 적절한 컴포넌트를 렌더링
export default function QuestionnaireComponentDecider({
  questionnaireId,
}: QuestionnaireComponentDeciderProps) {
  const [questionnaire, setQuestionnaire] = useState<Questionnaire | null>(
    null
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timeUntilExpiration, setTimeUntilExpiration] = useState<number | null>(
    null
  );

  useEffect(() => {
    fetchQuestionnaire();
  }, [questionnaireId]);

  // 토큰 만료 시간 체크 및 자동 새로고침
  useEffect(() => {
    if (!questionnaire?.data?.expirationDate) return;

    const checkExpiration = () => {
      const currentTime = Date.now();
      const expirationTime = questionnaire.data.expirationDate;
      const timeLeft = expirationTime - currentTime;

      // 남은 시간 업데이트
      setTimeUntilExpiration(timeLeft > 0 ? timeLeft : 0);

      if (currentTime >= expirationTime) {
        console.log(
          "[DEBUG] JWT 토큰이 만료되었습니다. 페이지를 새로고침합니다."
        );
        window.location.reload();
      }
    };

    // 즉시 체크
    checkExpiration();

    // 1초마다 체크
    const interval = setInterval(checkExpiration, 1000);

    return () => clearInterval(interval);
  }, [questionnaire?.data?.expirationDate]);

  const fetchQuestionnaire = async () => {
    try {
      setLoading(true);
      setError(null);

      // URL에서 JWT 토큰 추출 (쿼리 파라미터 또는 localStorage)
      const urlParams = new URLSearchParams(window.location.search);
      const jwtToken =
        urlParams.get("token") ||
        urlParams.get("jwtToken") ||
        localStorage.getItem("jwtToken");

      // BE API 호출 - JWT 토큰을 Authorization 헤더에 포함
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };

      if (jwtToken) {
        headers["Authorization"] = `Bearer ${jwtToken}`;
      }

      // fetch 호출 시 쿼리 파라미터로 jwtToken을 반드시 포함
      const response = await fetch(
        `/api/questionnaire/public/${questionnaireId}?jwtToken=${
          jwtToken ?? ""
        }`,
        { headers }
      );

      if (!response.ok) {
        if (response.status === 401) {
          // JWT 토큰 만료 또는 인증 실패
          setError("JWT 토큰이 만료되었습니다. 다시 문진표를 받아 주세요.");
          return;
        } else {
          // 기타 에러
          const errorData = await response.json().catch(() => ({}));
          setError(
            errorData.error ||
              `문진표를 불러올 수 없습니다. (상태 코드: ${response.status})`
          );
          return;
        }
      }

      const responseData = await response.json();

      // 401 에러가 아닌 경우에만 데이터 설정
      if (response.ok || response.status === 404) {
        // API 응답 구조에 맞게 데이터 설정
        const questionnaireData = {
          id: responseData.data?.questionnaireId?.toString() || questionnaireId,
          title: responseData.data?.questionnaireName || "문진표",
          description: "문진표 정보",
          data: responseData.data || {},
          status: responseData.status || "success",
        };

        setQuestionnaire(questionnaireData);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return <ErrorState error={error} onRetry={fetchQuestionnaire} />;
  }

  if (!questionnaire) {
    return <NotFoundState />;
  }

  // 데이터 가공: 각 *Info 필드가 문자열이면 JSON.parse로 변환
  const data = { ...questionnaire.data } as any;
  const infoFields = [
    "medicationInfo",
    "allergyInfo",
    "chronicDiseaseInfo",
    "surgeryHistoryInfo",
  ];
  infoFields.forEach((field) => {
    if (typeof data[field] === "string") {
      try {
        data[field] = JSON.parse(data[field]);
      } catch {
        data[field] = [];
      }
    }
  });
  const processedQuestionnaire = { ...questionnaire, data };

  // 문진표 정보를 표시
  console.log(
    "[DEBUG] QuestionnaireComponentDecider questionnaire:",
    processedQuestionnaire
  );
  return (
    <QuestionnaireDisplay
      questionnaire={processedQuestionnaire}
      timeUntilExpiration={timeUntilExpiration}
    />
  );
}
