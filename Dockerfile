FROM amazoncorretto:21-alpine

# Optional: Create a non-root user (or just skip this block)
RUN addgroup -S hmcts && adduser -S hmcts -G hmcts
USER hmcts

WORKDIR /opt/app

COPY build/libs/prl-cos-api.jar .

EXPOSE 4044

CMD ["java", "-jar", "prl-cos-api.jar"]

HEALTHCHECK --interval=30s --timeout=15s --start-period=60s --retries=3 \
    CMD wget -q --spider localhost:4044/health || exit 1
