FROM xploits/debian-jdk
ENV SOFFICE=/app/libreoffice/soffice
ENV MIN_INSTANCES=5

WORKDIR /app
COPY ./libreoffice ./libreoffice
COPY ./wololo.jar .

ENTRYPOINT ["java", "-cp", "wololo.jar:/app/libreoffice/classes/libreoffice.jar", "Main"]
