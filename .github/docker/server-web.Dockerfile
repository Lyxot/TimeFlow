ARG BASE_IMAGE
FROM ${BASE_IMAGE}

COPY app.zip /opt/timeflow/web/app.zip

ENV TIMEFLOW_WEB_APP_SERVE_ENABLED=true
ENV TIMEFLOW_WEB_APP_ZIP_PATH=/opt/timeflow/web/app.zip
