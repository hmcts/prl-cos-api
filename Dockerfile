ARG APP_INSIGHTS_AGENT_VERSION=3.2.6

# Application image
FROM hmctspublic.azurecr.io/base/java:17-distroless

# Change to non-root privilege
USER hmcts

COPY lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/prl-cos-api.jar /opt/app/

EXPOSE 4044
CMD [ "prl-cos-api.jar" ]

HEALTHCHECK --interval=30s --timeout=15s --start-period=60s --retries=3 \
    CMD wget -q --spider localhost:4044/health || exit 1
