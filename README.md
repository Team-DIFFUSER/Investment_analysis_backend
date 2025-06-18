# 📚 API 명세서 - Spring Boot 백엔드

> JWT 인증 기반, MongoDB 사용  
> 회원가입, 로그인, 사용자 조회, 자산 평가 등 API 제공

---

## ✅ 인증 관련 API

### 1. 📥 회원가입
- **Method:** `POST`
- **URL:** `/api/users/register`
- **설명:** 사용자 회원가입 처리

#### 📦 요청 Body
```json
{
  "username": "JunOh",
  "password": "testPassword!",
  "name": "준오",
  "email": "jun@example.com"
}
```

#### 📤 응답 예시
```json
{
    "username": "JunOh123",
    "name": "JunOh",
    "email": "jun123@example.com",
    "password": "$2a$10$bVbj3xl039e3dJJ4Si.90eL.4U6GZy372TbrRKkXw78hJmPx3Y27G"
}
```

#### 🔓 인증 필요 여부
> ❌ 인증 불필요 (`permitAll()`)

---

### 2. 🔍 아이디 중복 확인
- **Method:** `GET`
- **URL:** `/api/users/exists/{username}`
- **설명:** 사용자 ID가 이미 존재하는지 확인

#### 📤 응답 예시
```boolean
true
```

#### 🔓 인증 필요 여부
> ❌ 인증 불필요 (`permitAll()`)

---

### 3. 🙋 아이디 존재 확인
- **Method:** `GET`
- **URL:** `/api/users/{username}`
- **설명:** 사용자 ID가 이미 존재하는지 확인

#### 📤 응답 예시
```json
{
    "username": "JunOh123",
    "name": "JunOh",
    "email": "jun123@example.com",
    "password": "$2a$10$bVbj3xl039e3dJJ4Si.90eL.4U6GZy372TbrRKkXw78hJmPx3Y27G"
}
```

#### 🔒 인증 필요 여부
> ❌ 인증 불필요 (`permitAll()`)

---

### 4. 🔐 로그인
- **Method:** `POST`
- **URL:** `/api/users/login`
- **설명:** 사용자 로그인, JWT 토큰 발급

#### 📦 요청 Body
```json
{
  "username": "jun123",
  "password": "securePassword!"
}
```

#### 📤 응답 예시
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 🔓 인증 필요 여부
> ❌ 인증 불필요 (`permitAll()`)

---

### 5. 📥 투자성향 분석
- **Method:** `POST`
- **URL:** `/api/users/profile`
- **설명:** 로그인한 사용자의 투자성향 테스트 결과(응답 정보)를 기반으로, 투자 점수를 계산하고 투자 유형을 결정하여 사용자 계정을 업데이트한 후, 투자성향 반환

#### 📦 요청 Body
> 로그인된 사용자의 정보는 SecurityContext에서 가져오므로, 요청 Body에는 투자 테스트 응답(response) 데이터만 포함합니다.
  
```json
{
  "responses": [
    { "questionId": 1, "selectedOption": 1 },
    { "questionId": 2, "selectedOption": 1 },
    { "questionId": 3, "selectedOption": 1 },
    { "questionId": 4, "selectedOption": 1 },
    { "questionId": 5, "selectedOption": 1 },
    { "questionId": 6, "selectedOption": 1 },
    { "questionId": 7, "selectedOption": 1 }
  ]
}
```

#### 📤 응답 예시
> 프론트엔드에는 민감한 정보(비밀번호, 이메일 등)를 제외한 투자 결과만 전달됩니다.
  
```json
{
  "username": "jun123",
  "totalScore": 52.9,
  "investmentType": "위험중립형"
}
```

#### 🔓 인증 필요 여부
> ✔️ 인증 필요 (JWT Token을 통한 인증 필요)

---

## 💼 자산 관련 API

### 1. 💰 자산 평가 정보 조회
- **Method:** `GET`
- **URL:** `/api/assets`
- **설명:** 키움 API를 이용한 자산 평가 정보 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
{
  "entr": "000082987499",               // 예수금 (entr)
  "d2EntBalance": "000051842599",       // D+2 추정예수금 (d2_entra)
  "totalEstimate": "000417180200",      // 유가잔고 평가금액 (tot_est_amt)
  "totalPurchase": "000416117111",      // 총매입금액 (tot_pur_amt)
  "lspft_amt": "000000000000",          // 누적투자원금 (lspft_amt)
  "profitLoss": "000000000000",         // 누적 투자손익 (lspft)
  "profitLossRate": "0.00",             // 누적 손익률 (%) (lspft_rt)
  "stocks": [                           // 종목별 계좌평가 현황
    {
      "stk_cd": "A138040",               // 종목코드 (stk_cd)
      "name": "메리츠금융지주",           // 종목명 (stk_nm)
      "quantity": "000000000900",        // 보유수량 (rmnd_qty)
      "avgPrice": "000000118234",        // 평균단가 (avg_prc)
      "currentPrice": "000000119600",    // 현재가 (cur_prc)
      "evalAmount": "000106729370",      // 평가금액 (evlt_amt)
      "plAmount": "000000318870",        // 손익금액 (pl_amt)
      "plRate": "0.2997"                 // 손익률 (pl_rt)
    },
    ...
  ]
}
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

### 2. 💰 개별 종목 차트 정보 조회
- **Method:** `GET`
- **URL:** `/api/assets/{stkCd}`
- **설명:** 요청한 종목 코드(stkCd)에 해당하는 차트 정보를 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
{
    "stk_cd": "138040",
    "stk_dt_pole_chart_qry": [
        {
            "cur_prc": "122600",        //현재가
            "trde_qty": "211656",       //거래량
            "trde_prica": "26052",      //거래대금
            "dt": "20250502",           //일자
            "open_pric": "121700",      //시가
            "high_pric": "124500",      //고가
            "low_pric": "121600"        //저가
        },
        ...
    ]
}
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

### 3. 💰 개별 종목 예측 주가 조회
- **Method:** `GET`
- **URL:** `/api/assets/{stkCd}/predicted-prices`
- **설명:** 요청한 종목 코드(stkCd)에 해당하는 주가 예측 차트를 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
[
    {
        "date": "2025-06-20",
        "predictedPrice": "73000.00"
    },
    {
        "date": "2025-06-23",
        "predictedPrice": "73000.00"
    },
    {
        "date": "2025-06-24",
        "predictedPrice": "72900.00"
    },
    ...
]
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

### 4. 💰 개별 종목 재무재표 조회
- **Method:** `GET`
- **URL:** `/api/assets/{stkCd}/financials`
- **설명:** 요청한 종목 코드(stkCd)에 해당하는 재무재표 정보를 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
{
    "per": "9.05",
    "roe": "23.4",
    "pbr": "1.89",
    "ev": "",
    "bps": "56959",
    "cap": "1131",
    "stk_cd": "138040",
    "sale_amt": "40217",
    "bus_pro": "31889",
    "cup_nga": "23334"
}
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

### 5. 💰 전체 자산 차트 정보 조회
- **Method:** `GET`
- **URL:** `/api/assets/charts`
- **설명:** 사용자 전체 자산 차트 정보를 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
{
    "contYn": null,
    "nextKey": null,
    "daly_prsm_dpst_aset_amt_prst": [
        {
            "dt": "20241231",
            "prsm_dpst_aset_amt": "000000038343"
        },
        {
            "dt": "20250101",
            "prsm_dpst_aset_amt": "000000038093"
        },
        {
            "dt": "20250102",
            "prsm_dpst_aset_amt": "000000039491"
        },
        ...
    ]
}
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

### 6. 💰 추천 종목 정보 조회
- **Method:** `GET`
- **URL:** `/api/assets/recommendations`
- **설명:** 사용자 맞춤 추천 종목 정보를 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
[
    {
        "stock_code": "A001080",
        "stock_name": "만호제강",
        "recommendation_reason": "만호제강은 최근 한 달 동안 23.16%라는 높은 수익률을 기록하며 강력한 성장세를 입증하였습니다. 또한 변동성이 0.35로 안정적인 투자 환경을 보여주고 있으며, 뉴스 감성 점수가 0.50으로 긍정적인 뉴스 흐름을 확인할 수 있습니다. 이러한 이유로 만호제강은 중장기적으로 안정적인 수익 기대가 가능하며, 위험중립형 투자자에게 적합한 투자 대상이라고 추천드립니다."
    },
    {
        "stock_code": "A000670",
        "stock_name": "영풍",
        "recommendation_reason": "영풍은 최근 한 달 동안 19.15%의 높은 수익률을 기록하면서 탁월한 성과를 보여주었습니다. 또한, 변동성이 0.35로 상대적으로 안정적인 편이며, 뉴스 감성 점수가 0.50으로 중립적인 관점을 보여주고 있습니다. 이러한 점들을 고려할 때, 영풍은 위험 중립형 투자자에게 안정적인 수익률과 함께 상대적으로 낮은 위험을 제공하는 매력적인 투자 대상이 될 수 있습니다."
    },
    {
        "stock_code": "A003460",
        "stock_name": "유화증권",
        "recommendation_reason": "유화증권은 지난 1개월 동안 4.74%의 수익률을 보여주며 안정적인 성장을 이어가고 있습니다. 또한 변동성이 0.09로 상대적으로 낮은 편이어서 위험 중립형 투자자에게 적합한 투자 대상입니다. 뉴스 감성점수가 0.50인 것으로 보아 시장에서 긍정적인 반응을 얻고 있으며, 이는 앞으로의 성장 가능성을 시사하는 신호로 해석될 수 있습니다. 따라서 유화증권은 안정적인 수익과 낮은 위험을 동시에 추구하는 위험 중립형 투자자에게 탁월한 선택이 될 수 있습니다."
    },
    ...
]
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

### 7. 💰 종목 코드 조회
- **Method:** `GET`
- **URL:** `/api/assets/code/{종목명}`
- **설명:** 종목명을 입력받아 종목 코드를 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
005930
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

## 💼 뉴스 관련 API

### 8. 💰 뉴스 조회
- **Method:** `GET`
- **URL:** `/api/news`
- **설명:** 네이버 API를 이용한 키워드기반 뉴스 조회

#### 📥 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 📤 응답 예시
```json
[
    {
        "stockCode": "066570",  //보유주식 기반 기사
        "title": "올해의 발명왕에 최윤화 제엠제코 대표",
        "description": "은탑산업훈장은 조휘재 LG전자(066570) 부사장과 성낙섭 현대자동차 전무가 수훈했다. 조 부사장은 LG전자의 지식재산(IP) 조직을 총괄하며 세계 최고 수준의 특허권을 보유하고 체계적인 IP 전략을 통해 혁신 제품 기술을...",
        "url": "https://n.news.naver.com/mnews/article/011/0004487036?sid=105",
        "pubDate": "2025-05-19T06:13:00Z",
        "thumbnailUrl": "https://imgnews.pstatic.net/image/011/2025/05/19/0004487036_001_20250519195312943.jpg?type=w800"
    },
    {
        "stockCode": null,      //보유주식과 무관한 경제 기사
        "title": "中 4월 공장 가동률 선방…소비·수출 둔화는 여전",
        "description": "4월 실업률은 5.1%로 3월보다 0.1%포인트 하락했지만 미국 수출 의존도가 높은 일부 공장에서 일시 해고가 발생했다는 증언도 나온다. 중국 경제는 1분기 5.4% 성장하며 예상을 웃돌았다. 중국 정부는 올해 경제성장률 목표인...",
        "url": "http://www.g-enews.com/ko-kr/news/article/news_all/202505192001019489a1f309431_1/article.html",
        "pubDate": "2025-05-19T11:04:00Z",
        "thumbnailUrl": "https://nimage.g-enews.com/phpwas/restmb_allidxmake.php?idx=5&simg=20250519200137032279a1f3094311109215171.jpg"
    },
    ...
]
```

#### 🔒 인증 필요 여부
> ✅ 인증 필요 (JWT)

---

## 🧾 비고

- 모든 요청은 `application/json` 헤더를 포함해야 합니다.
- 로그인 후 받은 JWT 토큰은 모든 인증이 필요한 API에 `Authorization` 헤더로 함께 보내야 합니다.
  ```
  Authorization: Bearer <token>
  ```

---

🛠 작성일: 2025.06.18
👨‍💻 담당자: 백엔드 개발 - 박준오
