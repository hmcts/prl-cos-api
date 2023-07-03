ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/prl-cos-api.jar /opt/app/

EXPOSE 4044
CMD [ "prl-cos-api.jar" ]
