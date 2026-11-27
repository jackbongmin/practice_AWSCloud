# 11_01: 서버에는 Docker만 있다 — Java·Gradle 빌드도 상자(이미지) 안에서 한다 (멀티 스테이지)

# 1단계 build: JDK로 JAR를 만든다. 이 단계는 최종 이미지에 남지 않는다
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY . .
# Windows에서 올린 gradlew는 실행 권한이 없을 수 있다 (12장 126 오류) → sh로 실행
# --mount=type=cache: 내려받은 라이브러리를 서버에 남겨 두어, 다음 git pull 뒤 빌드가 빨라진다
# 테스트는 push 전에 내 PC에서 (./gradlew check). 12장부터는 CI가 맡는다
RUN --mount=type=cache,target=/root/.gradle sh gradlew bootJar -x test --no-daemon

# 2단계 실행: Java 17 실행 환경 + JAR만
FROM eclipse-temurin:17-jre
RUN useradd --system app
WORKDIR /app
COPY --from=build /src/build/libs/product-review-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
