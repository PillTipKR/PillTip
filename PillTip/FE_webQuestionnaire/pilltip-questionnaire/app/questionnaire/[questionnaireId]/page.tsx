/*
	문진표 ID에 따른 동적 페이지 컴포넌트
	ComponentDecider이 비동기적으로 문진표 데이터를 불러와서 적절한 컴포넌트를 렌더링
*/
import { Suspense } from "react";
import QuestionnaireComponentDecider from "./_component/QuestionnaireComponentDecider";

interface PageProps {
  params: Promise<{
    questionnaireId: string;
  }>;
}

// Next.js의 /questionnaire/[questionnaireId] 경로에 해당하는 페이지 컴포넌트
export default async function QuestionnairePage({ params }: PageProps) {
  const { questionnaireId } = await params;
  return (
    // Suspense로 감싸서 내부 컴포넌트가 비동기적으로 로딩될 때 대기 상태를 처리
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">문진표를 불러오는 중...</p>
          </div>
        </div>
      }
    >
      {/* 실제 렌더링되는 컴포넌트. 문진표 ID에 따라 적절한 문진표를 렌더링 */}
      <QuestionnaireComponentDecider questionnaireId={questionnaireId} />
    </Suspense>
  );
}
