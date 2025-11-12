FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup --system spring && adduser --system spring --ingroup spring

COPY target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8081 9091

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9091/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Ho_Chi_Minh", \
    "-jar", \
    "app.jar"]