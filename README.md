<br>

# CK3 DNA Converter

게임 크루세이더 킹즈 3(Crusader Kings III)의 DNA 데이터를 서로 다른 형식(인게임용 Base64 DNA ↔ 캐릭터 커마용 Ruler Designer 형식)으로 변환해주는 웹 도구입니다.  
2022년에 공부 목적으로 개발되었으며, 
**2026년에 GitHub Actions 자동 배포 테스트를 위해 새롭게 리팩토링 및 통합되었습니다.**

> 💡 **참고**: 2022년에 작성된 코드이므로 현재 기준으로는 코딩 스타일이나 구조가 다소 올드할 수 있습니다.

## 🔗 서비스 링크
- **배포 주소**: [https://loah8.github.io/ck3-converter/](https://loah8.github.io/ck3-converter/)
- **대상 게임 버전**: v.1.11.3 (현재 데이터 기준)

---

## 🏗 프로젝트 구조
기존에 BE용, FE용, GitHub Pages용으로 분리되어 있던 3개의 repository를 하나로 통합하였습니다.  
프로젝트는 크게 **BE (Data Processor)**, **FE (Web Converter)** 두 부분으로 구성되어 있습니다.

```text
├── .github
│   └── workflows
│       └── pages.yml             // GitHub Actions 배포 설정 (work 브랜치 push 시 자동 배포)
├── BE (Back-End)
│   ├── resources                 // 게임 원본 데이터 및 참조 파일 폴더
│   └── src
│       └── org.simple.esta.ck3DnaConverter
│           ├── Ck3DnaConverter.java             // BE 메인 실행 클래스
│           ├── constants
│           │   └── DNAConstants.java            // 유전자 데이터 관련 상수 정의
│           └── service
│               ├── RawFileToJsonConverter.java  // 게임 텍스트 파일을 JSON 구조로 변환
│               ├── CustomJsonParser.java        // 외부 라이브러리 없이 구현한 커스텀 JSON 파서
│               ├── CustomFileWriter.java        // 변환된 데이터를 FE용 JS 파일로 출력
│               ├── GeneJsonToObjectConverter.java // 유전자 JSON 데이터를 객체로 매핑
│               └── GeneOrderService.java        // 유전자 순서 및 매핑 로직 처리
├── FE (Front-End)
│   ├── public                    // 정적 에셋 (index.html 등)
│   ├── src
│   │   ├── components            // Vue UI 컴포넌트
│   │   │   ├── Converter.vue     // DNA 변환 핵심 로직 및 UI가 포함된 메인 컴포넌트
│   │   │   └── CopiableTextArea.vue // 결과값을 복사할 수 있는 텍스트 영역 컴포넌트
│   │   ├── files                 // BE에서 생성된 유전자 매핑 데이터 (.js)
│   │   ├── App.vue               // 최상위 루트 컴포넌트
│   │   └── main.js               // Vue 인스턴스 설정 및 엔트리 포인트
│   ├── package.json              // FE 의존성 및 스크립트 설정
│   └── vue.config.js             // Vue CLI 빌드 설정 (publicPath 등)
└── README.md                     // 프로젝트 개요 및 가이드
```


### 1. BE (Back-End)
로컬에 설치된 게임 파일로부터 필요한 데이터를 추출하여, FE에서 사용할 수 있는 형태로 가공합니다.

- **기술 스택**: Plain Java
- **주요 기능**:
    - **게임 데이터 파싱**: 로컬 게임 폴더 내의 `.txt` 형식으로 구성된 enum 파일(유전자 데이터 등)을 읽어옵니다.
    - **Custom JSON Parser**: 외부 라이브러리 의존성을 줄이기 위해 직접 구현한 `CustomJsonParser`를 사용하여 데이터를 파싱합니다.
    - **JS 파일 자동 생성**: 파싱된 데이터를 자바스크립트 Map으로 변환하여 `FE/src/files/` 경로에 저장합니다.
- **핵심 클래스**:
    - `RawFileToJsonConverter`: 게임 원본 파일을 읽어 JSON 구조로 변환.
    - `CustomFileWriter`: 변환된 데이터를 JavaScript의 `export const` 구문을 포함한 파일로 출력.

### 2. FE (Front-End)
사용자가 웹 브라우저에서 DNA를 직접 변환할 수 있는 UI와 로직을 제공합니다.

- **기술 스택**: Vue.js 2, Tailwind CSS, Node.js
- **주요 기능**:
    - **DNA 상호 변환**: 
        - **Base64 to Hex** : 인게임에서 사용되는 Base64 형태의 DNA를 Hex 코드로 변환합니다.  
          (`atob`, `btoa` 함수와 `charCodeAt`을 사용하여 바이너리 데이터와 16진수 문자열로 변환합니다.)
        - **Hex to Ruler Designer** : Hex 데이터를 '커마용 데이터(Ruler Designer)' 형식으로 변환합니다.  
          BE에서 생성한 Map 데이터를(ex, `childIndexMap`, `childNameMap`)사용하여 숫자 인덱스를 게임 내 실제 속성명(ex,`chin_forward_pos`)으로 치환합니다.
        - **Ruler Designer to Base64**: '커마용 데이터(Ruler Designer)'를 다시 인게임용 'Base64 DNA'로 역변환합니다.
- **특징**: 2022년 당시 Vue 2와 Tailwind CSS 학습을 위해 제작되었으며, 정규 표현식(`RegExp`)과 비트 연산 기반의 인코딩/디코딩 로직이 포함되어 있습니다.

---

## 🚀 CI/CD 및 자동 배포

GitHub Actions를 활용한 자동 배포 환경을 공부하기 위해 구축되었습니다. 

- **배포 흐름**:
    1. **`work` 브랜치**에 코드를 `push` 합니다.
    2. **GitHub Actions**가 자동으로 트리거되어 Node.js 환경에서 FE 프로젝트를 빌드합니다.
    3. 빌드 결과물(`dist` 폴더)이 자동으로 호스팅 서비스에 반영됩니다.
- **설정 파일**: `.github/workflows/pages.yml`


<br>
<br>
<br>
