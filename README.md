# Backend-dashboard
DeeFacto Backend-Dashboard



# 🛠️ 프로젝트 환경 세팅 가이드 (for Backend)

## 💻 개발 환경

- Java 17 (Amazon Corretto)
- Spring Boot 3.5.3
- Gradle
- MySQL
- Kafka, MQTT, WebSocket, Docker, Jenkins, ArgoCD
- CI/CD: Jenkins + Docker + (AWS ECR) + ArgoCD
- MSA 구조

---

## 📦 프로젝트 초기 셋업 (처음 클론할 경우)

```bash
# 환경별 설정 및 의존성 설치 등 초기 세팅 명령어 작성 위치
```

---

## 🧩 주요 기능 / 구현 목록

- **Kafka Consumer**: 센서/웨어러블/설비 토픽 수신, DTO 파싱, 이벤트 위임
- **MQTT Client**: AWS IoT Shadow 연동, 인증서 기반 연결, 재시도/예외처리

---

## 🛠️ Backend 프로젝트 개발 가이드

### 📁 폴더 구조 및 설정 규칙

- 모든 설정은 `application.yml` 파일 사용 (`.properties ❌ 금지`)
- 새로운 설정 클래스는 `XXConfig.java` 네이밍 사용  
  예: `MqttConfig.java`, `KafkaConfig.java`

---

### ⚙️ 환경 변수 및 민감 정보 관리

- 모든 민감 정보는 환경 변수 또는 `.env` 파일로 관리
- 주요 환경 변수 예시:
  - `AWS_IAM_ACCESS_KEY`, `AWS_IAM_SECRET_KEY`
  - `GRAFANA_URL_OUTER`
  - `spring.datasource.*`, `spring.kafka.*` 등

---

## 🧑‍💻 커밋 메시지 컨벤션 (`|` 구분자 사용)

```bash
[type] | sprint | JIRA-KEY | 기능 요약 | 담당자
```

- **type**: feat, fix, docs, config, refactor, test, chore, style 등
- **sprint**: sprint0, sprint1, ...
- **JIRA-KEY**: JIRA 이슈 번호 또는 없음
- **기능 요약**: 핵심 변경 내용
- **담당자**: 실명 또는 닉네임

### 📌 예시

```
feat    | sprint0 | 없음     | 센서 등록 API 구현         | KIM
feat    | sprint0 | IOT-123  | 센서 등록 API 구현         | KIM
fix     | sprint1 | IOT-210  | MQTT 수신 실패 예외 처리   | RAFA
config  | sprint0 | IOT-001  | H2 DB 설정 추가            | MO
docs    | sprint1 | IOT-999  | README 초안 작성           | JONE
```

### ✅ 추천 커밋 예시 (복붙용)

```bash
git commit -m "feat    | sprint1 | IOT-112 | 작업자 센서 조회 API 추가 | KIM"
git commit -m "fix     | sprint0 | IOT-009 | H2 연결 오류 수정         | RAFA"
git commit -m "config  | sprint0 | IOT-000 | Spring Boot 3.4.4 적용    | MO"
git commit -m "chore   | sprint1 | IOT-999 | 커밋 컨벤션 README 정리   | JONE"
```

---

## 🌐 주요 URL

| 유형     | URL                              |
|----------|-----------------------------------|
| Swagger  | http://localhost:8080/swagger-ui.html |
| Grafana  | (운영 환경) 환경 변수 참조        |

---

## 🔒 보안 / 주의사항

- `.env`, `src/main/resources/certs/` 등 민감 파일은 **git에 커밋 금지**
- `.gitignore`에 이미 포함되어 있음
- 환경 변수/비밀키는 운영 서버 또는 CI/CD에서 안전하게 주입

---

## 🧪 테스트

- 테스트 코드는 `src/test/java` 디렉터리에 작성
- JUnit 5 기반 유닛/통합 테스트 구성

---

## 🚧 기타 운영 참고

- Jenkins 및 ArgoCD 연동은 `Jenkinsfile` 참조
- 신규 설정 파일 추가 시 반드시 `XXConfig.java` 네이밍 유지

