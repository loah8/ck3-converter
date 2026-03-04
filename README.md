# CK3 DNA Converter

이 프로젝트는 크루세이더 킹즈 3(Crusader Kings III)의 DNA 데이터를 서로 다른 형식(Base64 DNA ↔ Ruler Designer)으로 상호 변환해주는 웹 도구입니다. 2022년에 공부 목적으로 개발되었으며, **2026년에 GitHub Actions 자동 배포 테스트를 위해 새롭게 리팩토링 및 통합되었습니다.**

> 💡 **참고**: 2022년에 작성된 코드이므로 현재 기준으로는 코딩 스타일이나 구조가 다소 올드(old)할 수 있습니다.

## 🔗 서비스 링크
- **배포 주소**: [https://loah8.github.io/ck3-converter/](https://loah8.github.io/ck3-converter/)
- **대상 게임 버전**: v.1.11.3 (현재 데이터 기준)

---

## 🏗 프로젝트 구조

기존에 **BE용, FE용, GitHub Pages용으로 분리되어 있던 3개의 저장소를 하나로 통합**하여 관리하고 있습니다. 프로젝트는 크게 **BE (Data Processor)**, **FE (Web Converter)** 두 부분으로 구성되어 있습니다.

### 1. BE (Back-End) - Data Extraction Module
로컬에 설치된 게임 파일로부터 필요한 데이터를 추출하여 FE에서 사용할 수 있는 형태로 가공하는 역할을 합니다.

- **기술 스택**: Plain Java
- **주요 기능**:
    - **게임 데이터 파싱**: 로컬 게임 폴더 내의 `.txt` 형식으로 구성된 enum 파일(유전자 데이터 등)을 읽어옵니다.
    - **Custom JSON Parser**: 외부 라이브러리 의존성을 줄이기 위해 직접 구현한 `CustomJsonParser`를 사용하여 데이터를 파싱합니다.
    - **JS 파일 자동 생성**: 파싱된 데이터를 FE 프로젝트에서 즉시 `import` 하여 사용할 수 있도록 `.js` 파일(Map 형태)로 변환하여 `FE/src/files/` 경로에 자동으로 저장합니다.
- **핵심 클래스**:
    - `RawFileToJsonConverter`: 게임 원본 파일을 읽어 JSON 구조로 변환.
    - `CustomFileWriter`: 변환된 데이터를 JavaScript의 `export const` 구문을 포함한 파일로 출력.

### 2. FE (Front-End) - Web Application
사용자가 웹 브라우저에서 직접 DNA를 변환할 수 있는 UI와 로직을 제공합니다.

- **기술 스택**: Vue.js 2, Tailwind CSS, Node.js
- **주요 기능**:
    - **DNA 상호 변환**: 
        - **Base64 to Hex**: 게임 세이브 파일 등에서 사용되는 Base64 형태의 DNA를 Hex 코드로 변환합니다.
        - **Hex to Ruler Designer**: Hex 데이터를 게임 내 '인물 설계자(Ruler Designer)' 형식의 텍스트로 변환합니다. 이 과정에서 BE에서 생성한 Map 데이터를 참조하여 인덱스를 실제 유전자 이름으로 매핑합니다.
        - **Ruler Designer to Base64**: 반대로 인물 설계자 텍스트를 다시 Base64 DNA로 역변환합니다.
- **특징**: 2022년 당시 Vue 2와 Tailwind CSS 학습을 위해 제작되었으며, 정규 표현식(`RegExp`)과 비트 연산 기반의 인코딩/디코딩 로직이 포함되어 있습니다.

---

## 🚀 CI/CD 및 자동 배포

GitHub Actions를 활용한 자동 배포 환경을 공부하기 위해 구축되었습니다. **`work` 브랜치에 코드를 `push` 하는 즉시 자동으로 빌드 및 배포가 진행됩니다.**

- **배포 흐름**:
    1. **`work` 브랜치**에 코드를 `push` 합니다. (배포 트리거)
    2. **GitHub Actions**가 자동으로 트리거되어 Node.js 환경에서 FE 프로젝트를 빌드합니다.
    3. 빌드 결과물(`dist` 폴더)이 `gh-pages` 액션을 통해 자동으로 호스팅 서비스에 반영됩니다.
- **설정 파일**: `.github/workflows/pages.yml`

---

## 📂 주요 기술 및 파일 입출력 상세

### Java 파일 입출력 (BE)
Java의 `BufferedWriter`와 `FileWriter`를 사용하여 변환된 데이터를 저장합니다. 특히 FE에서 정적 상수로 즉시 활용할 수 있도록 파일 상단에 `export const ...` 구문을 직접 삽입하여 저장하는 방식을 사용했습니다.

### 데이터 변환 로직 (FE)
- **Base64 ↔ Hex**: `atob`, `btoa` 함수와 `charCodeAt`을 사용하여 바이너리 데이터와 16진수 문자열 사이를 변환합니다.
- **Map Mapping**: BE에서 추출된 `childIndexMap`, `childNameMap` 등을 사용하여 숫자 인덱스를 게임 내 실제 속성명(예: `chin_forward_pos`)으로 치환합니다.

---

## 📝 참고 사항
- 본 프로젝트는 개인 공부용으로 제작되었으며, 최신 게임 패치 내용에 따라 데이터 맵핑이 달라질 수 있습니다.
- `main` 브랜치는 프로젝트의 소개와 관리를 위한 문서 위주로 유지될 예정입니다.
