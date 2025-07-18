import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

// 1. 인증서 검사 사용 / 2. 인증서 검사 비활성화
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "1";

const BE_BASE_URL = process.env.BE_API_URL || "https://pilltip.com:20022";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ questionnaireId: string }> }
) {
  const { questionnaireId } = await context.params;

  console.log("[DEBUG] request.nextUrl:", request.nextUrl.toString());
  console.log("[DEBUG] questionnaireId:", questionnaireId);

  const tokenParam = request.nextUrl.searchParams.get("token");
  const jwtTokenParam = request.nextUrl.searchParams.get("jwtToken");

  console.log("[DEBUG] token param:", tokenParam);
  console.log("[DEBUG] jwtToken param:", jwtTokenParam);

  const jwtTokenFromQuery = tokenParam || jwtTokenParam;
  const jwtTokenFromCookie = (await cookies()).get("jwtToken")?.value;
  const jwtToken = jwtTokenFromQuery || jwtTokenFromCookie;

  try {
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };

    if (jwtToken) {
      headers["Authorization"] = jwtToken.startsWith("Bearer ")
        ? jwtToken
        : `Bearer ${jwtToken}`;
    }

    const beUrl = `${BE_BASE_URL}/api/questionnaire/public/${questionnaireId}?token=${jwtToken}`;

    // 디버깅용 로그
    console.log("beUrl", beUrl);
    console.log("jwtToken", jwtToken);
    const response = await fetch(beUrl, {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      return NextResponse.json(
        {
          error: `문진표를 불러올 수 없습니다. (BE status: ${response.status})`,
        },
        { status: response.status }
      );
    }

    const result = await response.json();

    if (result.status === "success" && result.data) {
      const questionnaire = {
        id: questionnaireId,
        title: result.data.questionnaireName,
        description: "문진표",
        data: result.data,
        status: "published",
        expirationDate: result.data.expirationDate,
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
    return NextResponse.json(
      { error: "문진표를 불러오는 도중 오류가 발생했습니다." },
      { status: 500 }
    );
  }
}
