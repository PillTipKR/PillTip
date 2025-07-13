import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

// BE API 엔드포인트 설정
const BE_BASE_URL = process.env.BE_API_URL || "http://localhost:20022";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ questionnaireId: string }> }
) {
  const { questionnaireId } = await context.params;

  console.log("[DEBUG] request.nextUrl:", request.nextUrl.toString());
  const jwtTokenFromQuery =
    request.nextUrl.searchParams.get("token") ||
    request.nextUrl.searchParams.get("jwtToken");
  const jwtTokenFromCookie = (await cookies()).get("jwtToken")?.value;
  const jwtToken = jwtTokenFromQuery || jwtTokenFromCookie;

  console.log(`[DEBUG] questionnaireId:`, questionnaireId);
  console.log(`[DEBUG] jwtToken:`, jwtToken);

  try {
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };

    if (jwtToken) {
      headers["Authorization"] = jwtToken.startsWith("Bearer ")
        ? jwtToken
        : `Bearer ${jwtToken}`;
    }

    const beUrl = `${BE_BASE_URL}/api/questionnaire/public/${questionnaireId}?jwtToken=${jwtToken}`;
    const response = await fetch(beUrl, {
      method: "GET",
      headers: headers,
    });

    console.log(`[DEBUG] BE response status:`, response.status);

    if (!response.ok) {
      return NextResponse.json(
        {
          error: `문진표를 불러올 수 없습니다. (BE status: ${response.status})`,
        },
        { status: response.status }
      );
    }

    const result = await response.json();
    console.log(`[DEBUG] BE response JSON:`, result);

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
