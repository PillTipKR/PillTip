import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from openai import OpenAI
import os
import json
from difflib import get_close_matches
import re

client = OpenAI(api_key="")  # 실제 키로 대체

# ✅ 기준 증상 및 연관 증상 정의
cluster_symptoms = [
    "감기", "독감", "폐렴", "기관지염", "천식", "알레르기비염", "축농증", "편도염", "인후염", "중이염",
    "위염", "위식도역류", "소화불량", "장염", "변비", "설사", "과민성대장증후군", "치핵", "간염", "지방간",
    "담석증", "요로감염", "방광염", "신우신염", "신장결석", "고혈압", "저혈압", "고지혈증", "당뇨병", "빈혈",
    "심부전", "협심증", "심근경색", "심계항진", "뇌졸중", "치매", "간경변", "갑상선기능저하증", "갑상선기능항진증",
    "무릎관절염", "퇴행성디스크", "요통", "견통", "근육통", "류마티스관절염", "통풍", "피부염", "아토피",
    "두드러기", "무좀", "습진", "결막염", "안구건조증", "백내장", "녹내장", "구내염", "구강건조증",
    "치통", "잇몸염증", "비만", "식욕부진", "불면증", "코골이", "수면무호흡증", "스트레스장애", "우울증", "불안장애",
    "공황장애", "주의력결핍과잉행동장애", "틱장애", "편두통", "두통", "어지럼증", "청각장애", "이명",
    "호흡곤란", "가슴통증", "복통", "흉통", "오한", "고열", "발열", "무기력", "피로",
    "메스꺼움", "구토", "식중독", "탈수", "화상", "골절", "타박상", "염좌", "부종",
    "손발저림", "근육경련", "피부건조", "땀샘질환", "생리통", "생리불순", "갱년기장애", "임신오조", "유산징후"
]

related_symptoms = {
    "감기": ["기침", "콧물", "오한", "목통증", "두통", "발열"],
    "독감": ["고열", "근육통", "두통", "기침", "오한", "피로"],
    "폐렴": ["기침", "가래", "호흡곤란", "고열", "흉통"],
    "기관지염": ["기침", "가래", "흉통", "인후통"],
    "천식": ["호흡곤란", "기침", "가슴통증", "쌕쌕거림"],
    "알레르기비염": ["콧물", "코막힘", "재채기", "눈 가려움"],
    "축농증": ["두통", "콧물", "코막힘", "얼굴통증"],
    "편도염": ["목통증", "발열", "삼킴곤란", "피로"],
    "인후염": ["목이 칼칼함", "인후통", "기침"],
    "중이염": ["귀통증", "발열", "청력저하", "어지럼증"],
    
    "위염": ["복통", "메스꺼움", "식욕부진", "속쓰림"],
    "소화불량": ["속더부룩함", "트림", "메스꺼움", "복부팽만감"],
    "장염": ["복통", "설사", "구토", "탈수"],
    "변비": ["배변곤란", "복부팽만", "배통"],
    "설사": ["복통", "탈수", "구토"],
    
    "고혈압": ["두통", "어지럼증", "가슴두근거림"],
    "저혈압": ["무기력", "피로", "어지럼증"],
    "고지혈증": ["무증상", "가슴통증", "현기증"],
    "당뇨병": ["잦은 소변", "갈증", "피로", "체중감소"],
    
    "두통": ["편두통", "어지럼증", "눈부심", "메스꺼움"],
    "편두통": ["두통", "눈통증", "오심", "소리예민"],
    
    "요로감염": ["배뇨통", "빈뇨", "혈뇨", "하복통"],
    "방광염": ["배뇨통", "잔뇨감", "빈뇨"],
    
    "피로": ["무기력", "불면증", "집중력 저하"],
    "불면증": ["수면장애", "피로", "스트레스"],
    
    "우울증": ["무기력", "식욕감소", "불면증", "우울감"],
    "불안장애": ["가슴두근거림", "불안", "공포감"],
    
    "생리통": ["복통", "요통", "피로", "메스꺼움"],
    "생리불순": ["주기변화", "무월경", "과다출혈"]
    # ✨ 생략된 항목도 요청 시 추가 가능
}


# ✅ 표현 매핑 사전 (비표준 표현 → 표준 증상)
expression_dict = {
  "속이 더부룩해": "소화불량",
  "배가 더부룩해": "소화불량",
  "소화가 안 돼": "소화불량",
  "체했나봐": "소화불량",
  "속이 울렁거려": "메스꺼움",
  "토할 것 같아": "구토",
  "속이 쓰려": "속쓰림",
  "머리가 띵해": "두통",
  "머리가 어지러워": "현기증",
  "몸이 쑤셔": "근육통",
  "가슴이 답답해": "호흡곤란",
  "숨쉬기 힘들어": "호흡곤란",
  "기침이 심해": "기침",
  "콧물이 계속 나와": "콧물",
  "목이 따끔해": "인후통",
  "입맛이 없어": "식욕부진",
  "기운이 없어": "피로",
  "맥이 없어": "무기력",
  "가슴이 철렁해": "심계항진",
  "심장이 터질 것 같아": "심계항진",
  "머리가 깨질 것 같아": "두통",
  "하늘이 노래진다": "현기증",
  "목이 타들어가": "갈증",
  "쿨럭쿨럭": "기침",
  "콜록콜록": "기침",
  "헉헉거려": "호흡곤란",
  "몸이 으슬으슬 떨려": "오한",
  "뒷골이 땡겨": "후두통",
  "신물이 올라와": "위산 역류",
  "손발이 시려": "수족냉증",
  "손발이 저려": "손발 저림",
  "허리가 끊어질 것 같아": "요통",
  "토 나와": "구토"
}


# ✅ 루트 증상 가중치 설정
root_boost = {
    "감기": 1.2,
    "폐렴": 1.1,
    "천식": 1.1
}

# ✅ 임베딩 함수
def get_embedding(text):
    return np.array(client.embeddings.create(input=[text], model="text-embedding-3-large").data[0].embedding)

# ✅ 증상 벡터 캐시
symptom_emb_file = "symptom_embeddings.json"
if os.path.exists(symptom_emb_file):
    with open(symptom_emb_file, "r", encoding="utf-8") as f:
        symptom_emb = json.load(f)
else:
    symptom_emb = {}

updated = False
for sym in cluster_symptoms:
    if sym not in symptom_emb:
        symptom_emb[sym] = get_embedding(sym).tolist()
        updated = True
if updated:
    with open(symptom_emb_file, "w", encoding="utf-8") as f:
        json.dump(symptom_emb, f, ensure_ascii=False, indent=2)

# ✅ 클러스터 보정용 벡터 캐시
cluster_map_file = "cluster_mappings.json"
if os.path.exists(cluster_map_file):
    with open(cluster_map_file, "r", encoding="utf-8") as f:
        cluster_map = json.load(f)
else:
    cluster_map = {}

updated = False
for cluster, related in related_symptoms.items():
    if cluster not in cluster_map:
        cluster_map[cluster] = {}
    for rel in related:
        if rel not in cluster_map[cluster]:
            cluster_map[cluster][rel] = get_embedding(rel).tolist()
            updated = True
if updated:
    with open(cluster_map_file, "w", encoding="utf-8") as f:
        json.dump(cluster_map, f, ensure_ascii=False, indent=2)

expression_map_file = "expression_mappings.json"
if os.path.exists(expression_map_file):
    with open(expression_map_file, "r", encoding="utf-8") as f:
        expression_map = json.load(f)
else:
    expression_map = expression_dict


# ✅ 사용자 입력 처리
user_input = input("\n📝 증상을 자연어로 입력하세요: ")

# 1️⃣ 먼저 전체 문장 기준 정확 표현 매핑
for expr, mapped in expression_map.items():
    if expr in user_input:
        user_input = user_input.replace(expr, mapped)

# 2️⃣ 퍼지 매칭으로 오타 보정 적용
tokens = re.findall(r'[가-힣a-zA-Z0-9]+', user_input)
normalized_tokens = []
for token in tokens:
    close = get_close_matches(token, expression_dict.keys(), n=1, cutoff=0.6)
    if close:
        normalized_tokens.append(expression_dict[close[0]])
    else:
        normalized_tokens.append(token)

normalized_input = " ".join(normalized_tokens)
print(f"🔧 정규화된 입력: {normalized_input}")

user_emb = get_embedding(normalized_input).reshape(1, -1)

# ✅ 기본 유사도 계산
symptom_array = np.array([symptom_emb[s] for s in cluster_symptoms])
base_similarities = cosine_similarity(user_emb, symptom_array)[0]

# ✅ 보정 점수 계산 (개선 버전)
boosted_scores = []
input_length_penalty = 1 / (1 + 0.05 * len(tokens))  # 입력 길이 정규화

for i, symptom in enumerate(cluster_symptoms):
    base_score = base_similarities[i] * input_length_penalty
    bonus = 0.0
    matched_rel_count = 0

    if symptom in cluster_map:
        for rel, rel_vec in cluster_map[symptom].items():
            rel_score = cosine_similarity(user_emb, np.array(rel_vec).reshape(1, -1))[0][0]
            if rel_score > 0.7:
                bonus += rel_score * 0.3  # 비례 보정
                matched_rel_count += 1
            elif rel_score > 0.5:
                bonus += rel_score * 0.2
                matched_rel_count += 1

    # 연관 증상 다수 포함 시 추가 보정 (선형)
    if matched_rel_count >= 2:
        bonus *= (1 + 0.1 * matched_rel_count)  # 예: 3개 일치 시 1.3배

    # 루트 증상 추가 가중치 적용
    if symptom in root_boost:
        bonus *= root_boost[symptom]

    boosted_scores.append(base_score + bonus)


# ✅ 결과 출력
sorted_indices = np.argsort(boosted_scores)[::-1]
print("\n🔍 최종 유사 증상 Top-5 (보정 유사도 기반):")
for rank in range(5):
    idx = sorted_indices[rank]
    print(f"{rank+1}. {cluster_symptoms[idx]} (점수: {boosted_scores[idx]:.4f})")
