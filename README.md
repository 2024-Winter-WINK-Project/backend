# 모꼬지 (Mokkoji)

## 프로젝트 개요
이 프로젝트는 카카오 소셜 로그인을 통한 인증과 모임별 가계부 관리 기능을 제공하는 서비스 모꼬지 (Mokkoji)의 백엔드 서버입니다.

## 주요 기능

### 1. 인증 (Auth)
- 카카오 소셜 로그인
- 회원 탈퇴
- 로그아웃

#### API 엔드포인트
- `GET /auth/kakao/login` - 카카오 로그인 콜백 처리
- `DELETE /auth/withdraw` - 회원 탈퇴
- `GET /auth/logout` - 로그아웃

### 2. 가계부 (Budget)
- 모임별 가계부 조회
- 수입/지출 내역 관리
- 정산 정보 관리 (카카오페이, 토스, 계좌이체)

#### API 엔드포인트
- `GET /groups/{groupId}/ledger` - 모임 가계부 조회
- `POST /groups/{groupId}/ledger/transactions` - 지출/수입 내역 추가
- `PUT /groups/{groupId}/ledger/transactions/{transactionId}` - 내역 수정
- `DELETE /groups/{groupId}/ledger/transactions/{transactionId}` - 내역 삭제

#### 정산 관련 API
- `POST /groups/{groupId}/transfer/kakao` - 카카오 송금 코드 등록
- `POST /groups/{groupId}/transfer/toss` - 토스 송금 코드 등록
- `POST /groups/{groupId}/transfer/bank-account-number` - 계좌번호 등록
- `POST /groups/{groupId}/transfer/bank-account-name` - 은행명 등록
- `GET /groups/{groupId}/transfer` - 등록된 송금코드 조회

### 3. 초대 (Invitation)
- 모임 초대 코드 생성 및 관리
- 24시간 후 만료되는 초대 코드

## 기술 스택
- Spring Boot
- Spring Security
- JPA/Hibernate
- OAuth2 (Kakao)
- WebClient

## 데이터베이스 구조

### 주요 엔티티
1. Auth (인증)
   - id: Long
   - socialId: String
   - socialType: String
   - nickname: String
   - profileImage: String

2. Budget (예산)
   - id: Long
   - totalAmount: Long
   - kakaoRemitLink: String
   - tossRemitLink: String
   - accountNumber: String
   - bank: String

3. BudgetDetail (예산 상세)
   - id: Long
   - category: String
   - amount: Long
   - description: String

4. Invitation (초대)
   - id: Long
   - inviteCode: String
   - expiresAt: LocalDateTime
   - createdAt: LocalDateTime

## 보안
- JWT 기반 인증
- HttpOnly 쿠키 사용
- OAuth2 소셜 로그인
