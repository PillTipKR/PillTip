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
    "공황장애", "주의력결핍과잉행동장애", "틱장애", "편두통", "두통", "현기증", "청각장애", "이명",
    "호흡곤란", "가슴통증", "복통", "흉통", "오한", "고열", "발열", "무기력", "피로",
    "메스꺼움", "구토", "식중독", "탈수", "화상", "골절", "타박상", "염좌", "부종",
    "손발저림", "근육경련", "피부건조", "땀샘질환", "생리통", "생리불순", "갱년기장애", "임신오조", "유산징후",
    "저혈당", "말초신경병증","자율신경실조증","기침","가래","인후통","콧물","시야 흐림"
]

original_related_symptoms = {
    "감기": ["기침", "콧물", "오한", "인후통", "두통", "발열"],
    "기침": ["가래","감기"],
    "독감": ["고열", "근육통", "두통", "기침", "오한", "피로"],
    "폐렴": ["기침", "가래", "호흡곤란", "고열", "흉통"],
    "기관지염": ["기침", "가래", "흉통", "인후통"],
    "천식": ["호흡곤란", "기침", "가슴통증", "쌕쌕거림"],
    "알레르기비염": ["콧물", "코막힘", "재채기", "눈 가려움"],
    "축농증": ["두통", "콧물", "코막힘", "얼굴통증"],
    "편도염": ["목통증", "발열", "삼킴곤란", "피로"],
    "인후염": ["목이 칼칼함", "인후통", "기침"],
    "중이염": ["귀통증", "발열", "청력저하", "현기증"],

    "위염": ["복통", "메스꺼움", "식욕부진", "속쓰림"],
    "소화불량": ["메스꺼움"],
    "장염": ["복통", "설사", "구토", "탈수"],
    "변비": ["배변곤란", "복통"],
    "설사": ["복통", "탈수", "구토"],

    "고혈압": ["두통", "현기증", "심계항진"],
    "저혈압": ["무기력", "피로", "현기증"],
    "고지혈증": ["가슴통증", "현기증"],
    "당뇨병": ["잦은 소변", "갈증", "피로", "체중감소", "손발 저림", "땀샘질환", "시야 흐림"],
    "저혈당": ["손발 저림", "땀샘질환", "시야 흐림", "피로", "두통"],
    "말초신경병증": ["손발 저림", "근육경련"],
    "자율신경실조증": ["심계항진", "현기증", "땀샘질환"],
    "두통": ["편두통", "현기증", "눈부심", "메스꺼움"],
    "편두통": ["두통", "편두통"],

    "요로감염": ["배뇨통", "빈뇨", "혈뇨", "하복통"],
    "방광염": ["배뇨통", "잔뇨감", "빈뇨"],

    "피로": ["무기력", "불면증", "집중력 저하"],
    "불면증": ["수면장애", "피로", "스트레스"],

    "우울증": ["무기력", "식욕부진", "불면증", "우울증"],
    "불안장애": ["심계항진", "불안", "공포감"],

    "생리통": ["복통", "요통", "피로", "메스꺼움"],
    "생리불순": ["주기변화", "무월경", "과다출혈"]
}

# ✅ 표현 매핑 사전 (비표준 표현 → 표준 증상)
symptom_aliases = {
  "속이 더부룩": "소화불량 ",
  "배가 더부룩": "소화불량 ",
  "소화가 안 돼": "소화불량 ",
  "체했": "소화불량 ",
  "속이 울렁": "메스꺼움 ",
  "토할 것": "구토 ",
  "머리가 띵": "두통 ",
  "어지러": "현기증 ",
  "몸이 쑤셔": "근육통 ",
  "가슴이 답답": "호흡곤란 ",
  "숨쉬기 힘들": "호흡곤란 ",
  "입맛이 없": "식욕부진 ",
  "기운이 없": "피로 ",
  "맥이 없": "무기력 ",
  "가슴이 철렁": "심계항진 ",
  "심장이 터질 것": "심계항진 ",
  "머리가 깨질 것": "두통 ",
  "하늘이 노래": "현기증 ",
  "하늘이 노랗게": "현기증 ",
  "헉헉거려": "호흡곤란 ",
  "으슬으슬": "오한 ",
  "허리가 끊어질 것": "요통 ",
  "토 나와": "구토 ",
  "트림": "소화불량 ",
  "배가 우리": "복통 ",
  "땀이 줄줄": "땀샘질환 ",
  "식은땀": "땀샘질환 ",
  "속이 쓰려": "속쓰림 ",
  "목이 따끔": "인후통 ",
  "목이 타들": "갈증 ",
  "쿨럭쿨럭": "기침 ",
  "콜록콜록": "기침 ",
  "뒷골": "후두통 ",
  "신물": "위산 역류 ",
  "손이 시": "수족냉증 ",
  "발이 시": "수족냉증 ",
  "손발이 시": "수족냉증 ",
  "손발이 저": "손발 저림 ",
  "손이 저": "손발 저림 ",
  "발이 저": "손발 저림 ",
  "손끝이 저": "손발 저림 ",
  "눈이 흐릿": "시야 흐림 ",
  "초점이 안 맞": "시야 흐림 ",
  "눈이 잘 안 보": "시야 흐림 ",
  "땀이 많이": "땀샘질환 "
}

# ✅ 루트 증상 가중치 설정
root_boost = {
    # 호흡기 질환
    "감기": 1.2,
    "폐렴": 1.1,
    "천식": 1.1,

    # 소화기 질환
    "위염": 1.2,
    "장염": 1.1,
    "소화불량": 1.05,

    # 순환기/대사
    "고혈압": 1.1,
    "당뇨병": 1.2,
    "심근경색": 1.1,

    # 신경/정신과
    "두통": 1.1,
    "편두통": 1.05,
    "우울증": 1.2,
    "불안장애": 1.1,

    # 비뇨기
    "요로감염": 1.05,

    # 여성 질환
    "생리통": 1.1,

    # 감염성 or 흔한 증후군
    "독감": 1.1,
    "장염": 1.1
}

# ✅ 임베딩 함수
def get_embedding(text):
    return np.array(client.embeddings.create(input=[text], model="text-embedding-3-large").data[0].embedding)

# ✅ 증상 임베딩 파일 동기화 (추가/삭제/이름변경 반영)
symptom_emb_file = "data/symptom_embeddings.json"
if os.path.exists(symptom_emb_file):
    with open(symptom_emb_file, "r", encoding="utf-8") as f:
        symptom_emb = json.load(f)
else:
    symptom_emb = {}

# [수정] 이름 변경(매핑)된 증상 처리
# 1. cluster_symptoms에 없는 기존 증상 삭제
deleted = [k for k in symptom_emb if k not in cluster_symptoms]
for k in deleted:
    del symptom_emb[k]
# 2. 새로 추가된 증상 추가
added = [sym for sym in cluster_symptoms if sym not in symptom_emb]
for sym in added:
    print("✨ 새로 추가된 증상:", sym)
    symptom_emb[sym] = get_embedding(sym).tolist()

if deleted:
    print("🗑️ 삭제된 증상:", deleted)

with open(symptom_emb_file, "w", encoding="utf-8") as f:
    json.dump(symptom_emb, f, ensure_ascii=False, indent=2)

# ✅ 클러스터 보정용 벡터 캐시도 동기화 (이름 변경 반영)
cluster_map_file = "data/cluster_mappings.json"
if os.path.exists(cluster_map_file):
    with open(cluster_map_file, "r", encoding="utf-8") as f:
        cluster_map = json.load(f)
else:
    cluster_map = {}

all_cluster_keys = set(original_related_symptoms.keys())

# [수정] 이름 변경(매핑)된 클러스터 처리
# 1. 관련 증상에 없는 기존 클러스터 삭제
cluster_deleted = [k for k in list(cluster_map.keys()) if k not in all_cluster_keys]
for k in cluster_deleted:
    print("🗑️ 삭제된 클러스터:", k)
    del cluster_map[k]

# 2. 새로 추가된 클러스터 및 관련 증상 추가
for cluster, related in original_related_symptoms.items():
    if cluster not in cluster_map:
        cluster_map[cluster] = {}
    
    # 기존 연관 증상들도 symptom_aliases를 통해 매핑
    mapped_related = set()
    for rel in related:
        mapped_rel = symptom_aliases.get(rel, rel)
        if mapped_rel in cluster_symptoms and mapped_rel != cluster:
            mapped_related.add(mapped_rel)
    
    # 매핑된 연관 증상들만 유지
    cluster_map[cluster] = {}
    for rel in mapped_related:
        # 이미 임베딩이 있는 경우 재사용
        if rel in symptom_emb:
            #print("✅ 이미 임베딩이 있는 증상:", rel)
            cluster_map[cluster][rel] = symptom_emb[rel]
        else:
            print("✨ 새로 추가된 증상:", rel)
            cluster_map[cluster][rel] = get_embedding(rel).tolist()

if cluster_deleted:
    print("🗑️ 삭제된 클러스터:", cluster_deleted)

with open(cluster_map_file, "w", encoding="utf-8") as f:
    json.dump(cluster_map, f, ensure_ascii=False, indent=2)

# ✅ 표현 매핑 사전 자동 동기화
expression_dict_file = "data/expression_mappings.json"
if os.path.exists(expression_dict_file):
    with open(expression_dict_file, "r", encoding="utf-8") as f:
        expression_dict = json.load(f)
else:
    expression_dict = {}


# [수정] 새로 추가된 비표준 표현만 반영
for k, v in symptom_aliases.items():
    if k not in expression_dict and v in cluster_symptoms and k not in cluster_symptoms:
        print("✨ 새로 추가된 표현:", k)
        expression_dict[k] = v

with open(expression_dict_file, "w", encoding="utf-8") as f:
    json.dump(expression_dict, f, ensure_ascii=False, indent=2)

# ✅ 비표준 표현 정리 및 자기자신 제거
related_symptoms = {}
for key, values in original_related_symptoms.items():
    # 키(주요 증상)도 symptom_aliases를 통해 매핑
    #print("✅ 키:", key)
    mapped_key = symptom_aliases.get(key, key)
    if mapped_key not in cluster_symptoms:
        continue
        
    new_values = set()
    for v in values:
        # 값(연관 증상)도 symptom_aliases를 통해 매핑
        std = symptom_aliases.get(v, v)
        if std in cluster_symptoms and std != mapped_key:
            new_values.add(std)
    if new_values:
        #print("✅ 값:", new_values)
        related_symptoms[mapped_key] = list(new_values)

# ✅ 사용자 입력 처리
user_input = input("\n📝 증상을 자연어로 입력하세요: ")


for expr, mapped in expression_dict.items():
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

# ✅ 사용자 임베딩 생성
embedding_targets = [normalized_input.strip()]
if normalized_input in related_symptoms:
    embedding_targets += related_symptoms[normalized_input]
embedding_targets = list(set(embedding_targets))
embedding_vectors = [get_embedding(sym) for sym in embedding_targets]
user_emb = np.mean(embedding_vectors, axis=0).reshape(1, -1)

# ✅ 유사도 계산
symptom_array = np.array([symptom_emb[s] for s in cluster_symptoms])
base_similarities = cosine_similarity(user_emb, symptom_array)[0]

# ✅ 보정 로직: 감쇠(log1p) 유지 + 입력 겹침 보정 강화
boosted_scores = []
input_length_penalty = 1 / (1 + 0.05 * len(tokens))

# ✅ 입력에서 증상명 단위로 추출 (사전 기반)
matched_symptoms = set()
for expr in cluster_symptoms:
    if expr in normalized_input:
        matched_symptoms.add(expr)
input_symptoms = matched_symptoms

for i, symptom in enumerate(cluster_symptoms):
    base_score = base_similarities[i] * input_length_penalty
    bonus = 0.0
    matched_rel_count = 0

    # ✅ 관련 증상과 입력 증상 직접 겹칠 경우: 강한 가중
    overlap_count = 0
    if symptom in related_symptoms:
        overlap = set(related_symptoms[symptom]) & input_symptoms
        overlap_count = len(overlap)
        bonus += 0.3 * overlap_count
        if overlap_count == 2:
            #print(f"🔍 중심 질환 점수: {base_score}")
            base_score *= 1.5  # 감기 등 중심 질환 강조
        if overlap_count >= 3:
            #print(f"🔍 중심 질환 점수: {base_score}")
            base_score *= 3.24  #중심 질환 강조

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

    # 루트 증상 보정
    if symptom in root_boost:
        bonus *= root_boost[symptom]

    # ✅ 다중 증상 입력 시, 단일 증상 가지치기(루프 내에서 바로 적용)
    if len(input_symptoms) >= 2 and symptom in input_symptoms:
        final_score *= 0.4  # 가지치기 비율(0.6)은 실험적으로 조정

    # 최종 감쇠된 보정 적용
    final_score = base_score * (1 + np.log1p(bonus))

    boosted_scores.append(final_score)
    
# ✅ 결과 출력
sorted_indices = np.argsort(boosted_scores)[::-1]
print("\n🔍 최종 유사 증상 Top-5 (보정 유사도 기반):")
for rank in range(5):
    idx = sorted_indices[rank]
    print(f"{rank+1}. {cluster_symptoms[idx]} (점수: {boosted_scores[idx]:.4f})")
    
    

def extract_top_symptoms(top_k: int = 5) -> list[str]:
    return [cluster_symptoms[i] for i in sorted_indices[:top_k]]

    
