import { NextRequest, NextResponse } from "next/server";
import { ApiResponse } from "@/types/questionnaire";

// BE API 엔드포인트 설정
const BE_BASE_URL = process.env.BE_API_URL || "http://164.125.253.20:20022";

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ questionnaireId: string }> }
) {
  const { questionnaireId } = await params;

  // Authorization 헤더에서 JWT 토큰 추출
  const authHeader = request.headers.get("authorization");
  const jwtToken = authHeader?.startsWith("Bearer ")
    ? authHeader.substring(7)
    : null;

  try {
    // BE에서 문진표 데이터 가져오기
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };

    // JWT 토큰이 있으면 Authorization 헤더에 추가
    if (jwtToken) {
      headers["Authorization"] = `Bearer ${jwtToken}`;
    }

    const response = await fetch(
      `${BE_BASE_URL}/api/questionnaire/${questionnaireId}`,
      {
        method: "GET",
        headers,
      }
    );

    if (!response.ok) {
      // BE에서 문진표를 찾을 수 없어도 더미 데이터로 화면을 보여줌
      console.log(
        `문진표 ${questionnaireId}를 찾을 수 없어서 더미 데이터를 반환합니다.`
      );

      const dummyData = {
        questionnaireId: parseInt(questionnaireId),
        questionnaireName: `문진표 ${questionnaireId}`,
        realName: "필팁 더미데이터",
        address: "부산대 기약기약",
        issueDate: new Date().toISOString().split("T")[0],
        lastModifiedDate: new Date().toISOString().split("T")[0],
        notes: "더미미더미미미",
        medicationInfo: JSON.stringify([
          { medication_id: "1", submitted: true },
          { medication_id: "2", submitted: false },
        ]),
        allergyInfo: JSON.stringify([
          { submitted: true, allergy_name: "penicillin" },
          { submitted: false, allergy_name: "aspirin" },
        ]),
        chronicDiseaseInfo: JSON.stringify([
          { submitted: true, chronicDisease_name: "고혈압" },
          { submitted: false, chronicDisease_name: "당뇨병" },
        ]),
        surgeryHistoryInfo: JSON.stringify([
          { surgeryHistory_name: "맹장수술", submitted: true },
          { surgeryHistory_name: "관절수술", submitted: false },
        ]),
      };

      const questionnaire = {
        id: questionnaireId,
        title: dummyData.questionnaireName,
        description: "문진표 (더미 데이터)",
        data: dummyData,
        status: "published",
      };

      return NextResponse.json(questionnaire);
    }

    const result = await response.json();

    // BE 응답 구조에 맞게 변환
    if (result.status === "success" && result.data) {
      const questionnaire = {
        id: questionnaireId,
        title: result.data.questionnaireName,
        description: "문진표",
        data: result.data,
        status: "published", // 또는 BE에서 상태를 받아와서 설정
      };

      return NextResponse.json(questionnaire);
    } else {
      return NextResponse.json(
        { error: "문진표 데이터가 올바르지 않습니다." },
        { status: 400 }
      );
    }
  } catch (error) {
    console.error("문진표 조회 오류:", error);
    console.log(`네트워크 오류로 인해 더미 데이터를 반환합니다.`);

    // 네트워크 오류가 발생해도 더미 데이터로 화면을 보여줌
    const dummyData = {
      questionnaireId: parseInt(questionnaireId),
      questionnaireName: `문진표 ${questionnaireId}`,
      realName: "필팁 더미데이터",
      address: "부산대 기약기약",
      issueDate: new Date().toISOString().split("T")[0],
      lastModifiedDate: new Date().toISOString().split("T")[0],
      notes: "더미미더미미미",
      medicationInfo: JSON.stringify([
        { medication_id: "1", submitted: true },
        { medication_id: "2", submitted: false },
      ]),
      allergyInfo: JSON.stringify([
        { submitted: true, allergy_name: "penicillin" },
        { submitted: false, allergy_name: "aspirin" },
      ]),
      chronicDiseaseInfo: JSON.stringify([
        { submitted: true, chronicDisease_name: "고혈압" },
        { submitted: false, chronicDisease_name: "당뇨병" },
      ]),
      surgeryHistoryInfo: JSON.stringify([
        { surgeryHistory_name: "맹장수술", submitted: true },
        { surgeryHistory_name: "관절수술", submitted: false },
      ]),
    };

    const questionnaire = {
      id: questionnaireId,
      title: dummyData.questionnaireName,
      description: "문진표 (더미 데이터 - 네트워크 오류)",
      data: dummyData,
      status: "published",
    };

    return NextResponse.json(questionnaire);
  }
}
