ARG APP_INSIGHTS_AGENT_VERSION=3.2.6
FROM hmctspublic.azurecr.io/base/java:17-distroless

ENV APP prl-dgs-api.jar

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 4007

CMD ["prl-dgs-api.jar"]


HEALTHCHECK --interval=30s --timeout=15s --start-period=60s --retries=3 \
    CMD wget -q --spider localhost:3100/health || exit 1