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
