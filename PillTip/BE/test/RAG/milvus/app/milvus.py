from pymilvus import DataType, AnnSearchRequest, MilvusClient, RRFRanker
import json
import numpy as np
from test_cluster import extract_top_symptoms
import re
from typing import List

# 1. MilvusClient 연결
client = MilvusClient(uri="http://0.0.0.0:19530")  # token 필요 시 token="root:Milvus"
collection_name = "drug_multi_symptom"
symptom_dims = 1536

# 2. 스키마 정의 및 컬렉션 생성 (객체지향 방식)
if not client.has_collection(collection_name):
    schema = client.create_schema(auto_id=True, enable_dynamic_field=False)
    schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
    schema.add_field(field_name="product_name", datatype=DataType.VARCHAR, max_length=200)
    for i in range(1, 5):
        schema.add_field(field_name=f"symptom_{i}", datatype=DataType.FLOAT_VECTOR, dim=symptom_dims)

    client.create_collection(
        collection_name=collection_name,
        schema=schema,
        description="약품 다중 증상 벡터 검색용"
    )
else:
    print(f"📦 이미 존재하는 컬렉션: {collection_name}")
    index_info = client.describe_index(index_name=collection_name,index_name="_default")
    has_symptom_vector_index = any(idx["field_name"] == "symptom_vector" for idx in index_info)
    if not has_symptom_vector_index:
        client.create_index(
            collection_name=collection_name,
            field_name="symptom_vector",
            index_type="AUTOINDEX",
            metric_type="L2"
        )
        print("🛠 인덱스 생성 완료 (기존 컬렉션)")


# ✅ 컬렉션 로딩
client.load_collection(collection_name)

# 3. 증상 임베딩 불러오기
with open("data/symptom_embeddings.json", "r", encoding="utf-8") as f:
    symptom_embeddings = json.load(f)

# 4. 표현사전 불러오기 (효능 효과 텍스트 정규화용)
with open("data/expression_mappings.json", "r", encoding="utf-8") as f:
    expression_dict = json.load(f)

# DB 데이터 중복 확인 함수
def is_product_exists(name: str) -> bool:
    expr = f'product_name == "{name}"'
    res = client.query(collection_name=collection_name, filter=expr, output_fields=["product_name"])
    return len(res) > 0

# 일반의약품 텍스트로부터 데이터 삽입
def insert_from_textblock(text_block):
    product_match = re.search(r"제품명: (.+)", text_block)
    efficacy_match = re.search(r"\[효능효과\]\n(.+?)\n\[", text_block, re.DOTALL)
    if not product_match or not efficacy_match:
        return

    name = product_match.group(1).strip()

    if is_product_exists(name):
        print(f"⚠️ 이미 존재함: {name} → 삽입 생략")
        return

    efficacy = efficacy_match.group(1).replace("\n", " ")
    symptoms = extract_matched_symptoms(efficacy)
    vectors = [symptom_embeddings[s] for s in symptoms if s in symptom_embeddings][:5]
    if len(vectors) < 5:
        vectors += [np.zeros(symptom_dims).tolist()] * (5 - len(vectors))

    data = {
        "product_name": name,
        "symptom_1": vectors[0],
        "symptom_2": vectors[1],
        "symptom_3": vectors[2],
        "symptom_4": vectors[3],
        "symptom_5": vectors[4],
    }

    client.insert(collection_name=collection_name, data=[data])
    print(f"✅ 삽입 완료: {name} ({len(symptoms)}개 증상)")


# 전체 파일에서 약품 삽입
def insert_all_from_file(filepath: str):
    with open(filepath, "r", encoding="utf-8") as f:
        raw = f.read()
    items = raw.strip().split("============================================================")
    for item in items:
        insert_from_textblock(item)

# 효능 효과 문장에서 매핑되는 증상 추출
def extract_matched_symptoms(efficacy_text):
    matched = []
    for keyword, mapped_symptom in expression_dict.items():
        if keyword in efficacy_text:
            matched.append(mapped_symptom)
    return matched

# 유사증상 → 증상 벡터로 변환
def process_user_input():
    top_symptoms = extract_top_symptoms()
    vectors = [symptom_embeddings[s] for s in top_symptoms if s in symptom_embeddings][:5]
    return top_symptoms, vectors

# 유사증상에 따른 약 검색
def search_drugs_by_symptoms(symptom_vectors: List[np.ndarray], top_k=5):
    queries = []
    for i, vec in enumerate(symptom_vectors):
        if i >= 5: break
        field_name = f"symptom_{i+1}"
        queries.append(AnnSearchRequest(field_name, [vec], "L2", limit=top_k))

    results = client.hybrid_search(
        collection_name=collection_name,
        queries=queries,
        rerank=RRFRanker(),
        output_fields=["product_name"]
    )

    for hit in results:
        print(f"💊 {hit.entity.get('product_name')} (score={hit.score:.4f})")

# --- 예시 실행 ---
if __name__ == "__main__":
    insert_all_from_file("일반의약품.txt")  # 🔁 약 40개 약품 일괄 삽입 (중복 시 생략)

    symptoms, vectors = process_user_input()
    if vectors:
        search_drugs_by_symptoms(vectors, top_k=5)
    else:
        print("⚠️ 입력에서 유효한 증상 벡터를 찾을 수 없습니다.")