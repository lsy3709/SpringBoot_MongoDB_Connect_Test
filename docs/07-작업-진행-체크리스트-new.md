# 작업 진행 체크리스트

현재까지 구현한 주요 기능과 개선 작업을 한눈에 볼 수 있도록 정리한 체크리스트입니다.

---

## 1. 기능 및 UX

- [x] 메모 CRUD (등록/수정/삭제/목록)
- [x] 이미지/동영상 첨부 및 표시 (GridFS 기반)
- [x] 무한 스크롤 목록 (커서 기반 페이지네이션)
- [x] 검색 기능 (제목/메세지/ID, **태그 검색** 포함)
- [x] 카테고리 탭(냉장고/팬트리 등)으로 메모 필터링
- [x] 메모 유통기한 날짜 입력 및 목록/상세 표시
- [x] 태그 입력/자동완성(localStorage), 태그 기반 필터링
- [x] 자주 사용하는 태그 버튼 (검색 영역 아래)
- [x] 목록에서 제목 클릭 시 **상세보기 모달** (큰 이미지/동영상, 메세지, 태그, 유통기한, 등록일)

## 2. 데이터·검색 구조

- [x] Memo 도큐먼트에 `categoryId`, `expiryDate`, `tags` 필드 추가
- [x] SearchDB DTO에 `categoryId` 추가 (탭별 검색)
- [x] 제목/메세지/ID/태그별 검색 모드 분리
- [x] 커서 기반 검색 페이지네이션 (`/searchDbPage`)

## 3. 이미지·스토리지

- [x] ImageService 분리 (storeForMemo, GridFS 연동)
- [x] 이미지 업로드 시 **1280px / 품질 0.8 JPEG**로 리사이즈 후 GridFS 저장
- [x] 동영상은 원본 그대로 저장
- [x] 목록에서는 작은 썸네일처럼 표시, 상세보기 모달에서는 크게 표시
- [x] 이미지 삭제 시 GridFS 파일까지 함께 정리

## 4. 인증·보안

- [x] 이메일 기반 로그인, BCrypt 비밀번호 암호화
- [x] 기본 관리자 `admin / admin1234` 자동 생성 (없을 때만)
- [x] 로그인 성공 시 `/login/redirect` 경유로 세션 안정화
- [x] 자동 로그인(remember-me) 옵션 추가 (체크박스 + 14일 유지)
- [x] 로그아웃 시 POST + CSRF 토큰 사용, `/` 메인으로 이동
- [x] CSRF 활성화 및 Thymeleaf 폼에 `_csrf` 적용

## 5. 운영·배포

- [x] AWS Lightsail 배포 가이드 (`docs/05-AWS-Lightsail-배포-가이드.md`)
- [x] 운영용 `application-prod.yml` 추가 (프로파일 분리)
- [x] 1GB RAM 환경 메모리 절약 전략 (`docs/06-메모리-절약-전략.md`)
- [x] MongoDB Docker + Spring Boot 동시 운영 기준으로 JVM/Mongo 메모리 상한 제안

## 6. 코드 구조·정리

- [x] Users(user) 도큐먼트·API 제거, User2(user2)만 사용
- [x] MemoController 분리 (메모 전용), UserController는 로그인/회원/뷰 전담
- [x] ImageServiceTest, MemoControllerTest, UserControllerTest 등 단위 테스트 구성
- [x] GlobalExceptionHandler + 공통 에러 응답 DTO 적용

## 7. 문서·가이드

- [x] 01-개선점 / 02-진행사항 / 03-로그인-리다이렉트-및-기본관리자 / 04-개선점-체크리스트
- [x] 05-AWS-Lightsail-배포-가이드 (Git 클론, Docker MongoDB, systemd)
- [x] 06-메모리-절약-전략 (1GB RAM 환경 튜닝)
- [x] 07-작업-진행-체크리스트 (본 문서)

---

앞으로는 **미니 인프런 스타일 확장(강의/수강 기능)**, 이미지/데이터 정리 배치(미사용 이미지 삭제), 추가 모니터링/알람 연동 등을 단계적으로 진행할 수 있습니다.
