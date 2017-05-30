FROM phusion/baseimage:latest

RUN apt-get update && \
    apt-get -y -q install libpcap0.8 wget

ENV VERSION=5.4.0 ARCH=x86_64 EXTENSION=tar.gz
ENV FILENAME=packetbeat-${VERSION}-linux-${ARCH}.${EXTENSION}

RUN wget https://artifacts.elastic.co/downloads/beats/packetbeat/${FILENAME} && \
    tar zxvf ${FILENAME}

WORKDIR packetbeat-${VERSION}-linux-${ARCH}
ADD packetbeat.yml packetbeat.yml
RUN chmod go-w packetbeat.yml

CMD ["./packetbeat", "-e", "-c=packetbeat.yml"]