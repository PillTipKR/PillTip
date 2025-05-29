# 초기 설정

## Docker 설치
Milvus DB사용을 위해 Docker 설치가 필요합니다.
이를 위해, Window 사용자의 경우, WSL이 필요합니다.
https://gaesae.com/161
위 링크를 참고하여 WSL을 설치해주세요.

아래 링크를 참고하여 Docker, Docker-compose를 설치해주세요.
WSL사용자는 WSL을 실행하여 시도하여야 합니다!!
- Docker 설치
https://haengsin.tistory.com/128
- Docker-compose 설치
https://jsonobject.tistory.com/8

## 파이썬 설치
```
sudo apt update & sudo apt upgrade
sudo apt install python3
sudo apt install python3-pip
```

# 실행하기 

## 다운 받은 폴더로 이동하기
cd "다운로드 받은 경로.../milvus" 
(wsl 사용 시, 
C드라이브는 C: -> /mnt/c로 변경하기. 
D드라이브도 D: -> /mnt/d로 변경하기.
주소가 \를 통해 구분되어 있는 경우, \를 /로 변경하여 이동하기

## 패키지 설치
```
pip install -r requirements.txt
```

pip3 install이 안될 경우 아래 링크 참고 하기
https://blog.naver.com/b14nc4/223418048502


## 도커 컴포즈로 Milvus 구축
```
docker-compose up -d
```

## 실행 확인하기
```
docker ps
```
milvus, etcd, minio 컨테이너가 떠 있으면 성공입니다.

## Python 코드 실행
```
python3 app/milvus.py
```
이 코드는 다음 순서로 작동합니다:

0. 사용자 입력 
	- 증상이 포함된 자연어로 입력합니다.
	- ex) 배가 아파요, 몸이 으슬으슬하고 머리가 띵해요 

1. 벡터 DB 생성 및 삽입
	- 벡터 DB에 drug_multi_symptom 라는 이름의 컬렉션을 생성합니다.
	- 벡터 DB에 넣을 자료형을 schema로 정의하고, 인덱스를 설정합니다.
	- 일반의약품.txt에 정의된 약품 데이터를 읽습니다.
	- 이를 파싱하여 데이터를 가공합니다.
		- 효능,효과 데이터에서 증상 클러스터와 매핑하여 임베딩 벡터 정보를 가져옵니다.
		- 현재는 Milvus의 디폴트 한계점인 4개까지만 추가됩니다. 
	- 가공한 데이터를 자료형에 맞게 삽입합니다.
		- 이때, 중복이 없는 약품만 Milvus에 저장합니다.

2. 표현사전과 임베딩을 통해 증상 리스트 추출 및 벡터화
	- 0번에서 입력한 자연어로 유추할 수 있는 증상 리스트가 추출되어 들어옵니다.
	- 이를 증상 클러스터의 증상들과 매핑하여 벡터화합니다.

3. Top-N 증상 벡터로 Milvus 검색
	- 2번 과정에서 벡터화한 값을 쿼리로하여 Milvus의 hybrid_search를 사용하여 다중검색을 시도합니다.
	- 쿼리를 병렬로 처리한 후, RRFRanker 방식을 사용하여 벡터를 재정렬합니다.

4. 추천 약 출력
	- 추천 약의 정보가 출력됩니다.



