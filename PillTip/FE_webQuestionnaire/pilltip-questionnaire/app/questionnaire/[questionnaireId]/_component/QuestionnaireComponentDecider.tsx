"use client";

import { useState, useEffect } from "react";
import QuestionnaireDisplay from "./QuestionnaireDisplay";
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

  useEffect(() => {
    fetchQuestionnaire();
  }, [questionnaireId]);

  const fetchQuestionnaire = async () => {
    try {
      setLoading(true);

      // URL에서 JWT 토큰 추출 (쿼리 파라미터 또는 URL 파라미터)
      const urlParams = new URLSearchParams(window.location.search);
      const jwtToken =
        urlParams.get("token") || localStorage.getItem("jwtToken");

      // BE API 호출 - JWT 토큰을 Authorization 헤더에 포함
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };

      if (jwtToken) {
        headers["Authorization"] = `Bearer ${jwtToken}`;
      }

      const response = await fetch(`/api/questionnaire/${questionnaireId}`, {
        headers,
      });

      if (!response.ok) {
        // 404 에러가 발생해도 더미 데이터가 반환되므로 에러를 던지지 않음
        console.log(`문진표 ${questionnaireId} 조회 실패, 더미 데이터 사용`);
      }

      const data = await response.json();
      setQuestionnaire(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">문진표를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-xl mb-4">⚠️</div>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={fetchQuestionnaire}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  if (!questionnaire) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">문진표를 찾을 수 없습니다.</p>
        </div>
      </div>
    );
  }

  // 문진표 정보를 표시
  return <QuestionnaireDisplay questionnaire={questionnaire} />;
}
