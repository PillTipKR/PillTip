import { NextRequest, NextResponse } from "next/server";
import { ApiResponse } from "@/types/questionnaire";
import { cookies } from "next/headers";

// BE API 엔드포인트 설정 (로컬 테스트용)
const BE_BASE_URL = process.env.BE_API_URL || "http://localhost:20022";

export async function GET(
  request: NextRequest,
  context: { params: { questionnaireId: string } }
) {
  const { questionnaireId } = context.params;

  // 쿼리 파라미터에서 jwtToken을 읽음 (QR 코드를 통한 접근)
  console.log("[DEBUG] request.nextUrl:", request.nextUrl.toString());
  const jwtTokenFromQuery = request.nextUrl.searchParams.get("jwtToken");
  console.log("[DEBUG] jwtTokenFromQuery:", jwtTokenFromQuery);
  const jwtTokenFromCookie = (await cookies()).get("jwtToken")?.value;
  const jwtToken = jwtTokenFromQuery || jwtTokenFromCookie;

  console.log(`[DEBUG] questionnaireId:`, questionnaireId);
  console.log(`[DEBUG] jwtToken:`, jwtToken);

  try {
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };

    // jwtToken이 이미 'Bearer '로 시작하는지 체크
    if (jwtToken) {
      headers["Authorization"] = jwtToken.startsWith("Bearer ")
        ? jwtToken
        : `Bearer ${jwtToken}`;
    } else {
      console.warn(`[WARN] No JWT token found in cookies.`);
    }

    const beUrl = `${BE_BASE_URL}/api/questionnaire/public/${questionnaireId}?jwtToken=${jwtToken}`;
    console.log(`[DEBUG] Fetching from BE:`, beUrl);
    const response = await fetch(beUrl, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        // Authorization 헤더는 필요 없음 (이 API는 쿼리 파라미터로만 받음)
      },
    });

    console.log(`[DEBUG] BE response status:`, response.status);

    if (!response.ok) {
      // BE에서 문진표를 찾을 수 없어도 더미 데이터로 화면을 보여줌
      console.warn(
        `문진표 ${questionnaireId}를 찾을 수 없어서 더미 데이터를 반환합니다. (BE status: ${response.status})`
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
    console.log(`[DEBUG] BE response JSON:`, result);

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
      console.error(`[ERROR] Invalid questionnaire data from BE:`, result);
      return NextResponse.json(
        { error: "문진표 데이터가 올바르지 않습니다." },
        { status: 400 }
      );
    }
  } catch (error) {
    console.error("[ERROR] 문진표 조회 오류:", error);
    console.log(`[DEBUG] 네트워크 오류로 인해 더미 데이터를 반환합니다.`);

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

export async function POST(request: Request) {
  // ... 로그인 로직 후 jwtToken 발급
  const jwtToken =
    "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzIiwiaWF0IjoxNzQ5MTk5NjIxLCJleHAiOjc3NDkxOTk1NjF9.9qrDulbKvu4FW6jjM2BBN7MOyyTbCqhwoVoeTHEBrPekwz3b5QEu6MtDLvNCv7Y4-bRyj8E__5XebmDPpwfZ2w";
  const response = NextResponse.json({ success: true });

  response.cookies.set("jwtToken", jwtToken, {
    httpOnly: true, // JS에서 접근 불가, 보안 ↑
    path: "/",
    maxAge: 60 * 60 * 24, // 1일
    sameSite: "lax", // 또는 "strict", "none"
    secure: process.env.NODE_ENV === "production", // HTTPS에서만
  });

  return response;
}
