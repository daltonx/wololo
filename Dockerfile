FROM ubuntu:jammy

ENV SOFFICE=/app/libreoffice/program/soffice

WORKDIR /app
COPY ./libreoffice ./libreoffice

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
	openjdk-17-jdk openjdk-17-jre ca-certificates p11-kit curl libxml2 libnss3 libfontconfig xsltproc; \
	rm -rf /var/lib/apt/lists/*; \
	curl -LJOf https://github.com/autentique/wololo/releases/download/stable/wololo.jar

ENTRYPOINT ["java", "-cp", "wololo.jar:/app/libreoffice/program/classes/libreoffice.jar", "Main"]
