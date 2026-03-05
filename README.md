# Spring Boot MongoDB 연동 테스트 프로젝트

Spring Boot + MongoDB 연동 및 Thymeleaf 기반 웹 기능을 연습하는 프로젝트입니다.  
이미지·동영상 업로드, 메모 CRUD, 회원가입/로그인(Spring Security), GridFS 파일 저장 등 ODM/웹 기본기를 다룹니다.  
이 구조를 바탕으로 **미니 인프런 스타일** 서비스 확장을 예정입니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.5.x |
| DB | MongoDB (Docker 6.0.4 권장) |
| 뷰 | Thymeleaf + Layout Dialect |
| 보안 | Spring Security 6 (form login, BCrypt) |
| 파일 처리 | GridFS, Thumbnailator, Commons IO |

---

## 프로젝트 구조

```
SpringBoot_MongoDB_Connect_Test/
├── src/main/java/com/myMongoTest/
│   ├── SpringBootMongoDbConnectTestApplication.java   # 진입점
│   ├── config/
│   │   ├── SecurityConfig.java                        # 로그인/권한/PasswordEncoder
│   │   └── CustomAuthenticationEntryPoint.java        # 미인증 시 리다이렉트
│   ├── constant/
│   │   └── Role.java                                  # USER, ADMIN
│   ├── controller/
│   │   ├── UserController.java                        # 로그인/회원가입/메인·admin 뷰
│   │   ├── MemoController.java                        # 메모 CRUD·검색·첨부 이미지
│   │   └── ImageController.java                       # 이미지 업로드·다운로드·삭제
│   ├── document/                                      # MongoDB 도큐먼트(ODM)
│   │   ├── User2.java                                 # 컬렉션: user2 (회원, 이메일/역할)
│   │   ├── Memo.java                                  # 컬렉션: memo (메모+이미지)
│   │   ├── Category.java                              # 컬렉션: category (탭)
│   │   └── LoginForm.java                             # 로그인 폼
│   ├── DTO/
│   │   ├── User2DB.java
│   │   └── SearchDB.java                              # 메모 검색 조건
│   └── service/
│       ├── UserService.java                           # 유저·메모 CRUD, UserDetailsService
│       └── ImageService.java                          # GridFS 파일명 조회/삭제
├── src/main/resources/
│   ├── application-sample.yml                          # MongoDB URI, multipart 설정 샘플
│   ├── templates/                                     # Thymeleaf (main, admin, login, join 등)
│   ├── static/                                        # css, js
│   └── META-INF/
├── docs/                                              # 프로젝트 문서
│   ├── 01-개선점.md
│   └── 02-진행사항.md
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 주요 기능

- **인증/권한**: 이메일 기반 로그인, `/admin`은 ADMIN 역할만 접근
- **기본 관리자**: 최초 기동 시 자동 생성 — 이메일 `admin`, 비밀번호 `admin1234` (없을 때만 생성)
- **회원가입**: `/joinForm` → `/joinUser` (비밀번호 BCrypt 암호화)
- **메모**: 메모 등록/수정/삭제/검색, 이미지 첨부(썸네일 생성), GridFS 저장
- **이미지 API**: `/images` 업로드, `GET /images/{id}` 다운로드, 파일명 목록/삭제

---

## API 목록 요약

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/`, `/main` | 메인 페이지 | permitAll |
| GET | `/login`, `/login/error` | 로그인 폼·에러 | permitAll |
| GET | `/login/redirect` | 로그인 성공 후 경유 리다이렉트 | authenticated |
| POST | `/joinUser` | 회원가입 | permitAll |
| GET | `/joinForm` | 회원가입 폼 | permitAll |
| GET | `/admin` | 관리자 페이지 (메모·카테고리 탭) | ADMIN |
| GET | `/findAllMemo` | 메모 전체 목록(JSON) | authenticated |
| GET | `/findAllMemoPage?lastId=&limit=10` | 메모 커서 페이지네이션(무한 스크롤용) | authenticated |
| POST | `/searchDbPage?lastId=&limit=10` | 검색 커서 페이지네이션(무한 스크롤용) | authenticated |
| POST | `/insertMemo`, `/insertMemoWithImage` | 메모 등록(이미지 선택) | authenticated |
| POST | `/updateMemo`, `/updateWithMemo` | 메모 수정 | authenticated |
| POST | `/searchDb` | 메모 검색(JSON, SearchDB) | authenticated |
| GET | `/updateFormMemo/{id}` | 메모 수정 폼 | authenticated |
| DELETE | `/dbDelete/{id}/{imageFileName}` | 메모·첨부 이미지 삭제 | authenticated |
| GET | `/categories` | 카테고리 목록(JSON) | authenticated |
| POST | `/categories` | 카테고리 추가 | authenticated |
| POST | `/images` | 이미지 업로드(GridFS) | authenticated |
| GET | `/images/{id}` | 이미지 다운로드(filename) | permitAll |
| GET | `/images/findFileNameAll` | 전체 파일명 목록(JSON) | authenticated |
| DELETE | `/images/deleteImage/{filename}` | 이미지 삭제 | authenticated |

---

## 실행 방법

### 1. MongoDB 실행 (Docker 예시)

```bash
docker run -d -p 27017:27017 --name mongodb mongo:6.0.4
```

### 2. 설정 파일

`application-sample.yml` 내용을 참고해 `application.yml`(또는 `application-{profile}.yml`)을 만들고, MongoDB URI 등을 설정합니다.

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/blog3
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080` 접속 후 로그인/회원가입·메모·이미지 기능을 사용할 수 있습니다.

---

## 참고

- 필요하신 분은 이 레포를 참고용으로 활용하시면 됩니다.
- 미니 인프런 확장 후 소스 공개 예정입니다.
- 상세 개선점·진행 계획은 `docs/` 폴더의 문서를 참고하세요.
- **메시지**: 로그인/회원가입/에러 메시지는 `src/main/resources/messages.properties` 에서 키로 관리됩니다.
- **통합 테스트**: MongoDB Testcontainers 테스트는 Docker 실행 후 `ENABLE_MONGO_INTEGRATION=true` 로 실행할 수 있습니다 (`./gradlew test`).
