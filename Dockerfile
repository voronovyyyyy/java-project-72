FROM node:22-bookworm-slim AS styles
WORKDIR /project/app
COPY app/package.json app/package-lock.json ./
RUN npm ci
COPY app/assets ./assets
COPY app/src/main/resources/templates ./src/main/resources/templates
RUN npm run build:css

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /project/app
COPY app/ ./
COPY --from=styles /project/app/src/main/resources/static/css/main.css ./src/main/resources/static/css/main.css
RUN chmod +x gradlew && ./gradlew --no-daemon shadowJar

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN useradd --create-home appuser && chown appuser:appuser /app
COPY --from=build --chown=appuser:appuser /project/app/build/libs/app-1.0-SNAPSHOT-all.jar ./app.jar
ENV APP_ENV=production
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC -Dfile.encoding=UTF-8"
USER appuser
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
