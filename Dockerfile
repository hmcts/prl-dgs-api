ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
FROM hmctspublic.azurecr.io/base/java:17-distroless

ENV APP prl-dgs-api.jar

COPY build/libs/$APP /opt/app/

EXPOSE 4007

CMD ["prl-dgs-api.jar"]
