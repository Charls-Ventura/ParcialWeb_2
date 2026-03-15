FROM gradle:8.7-jdk21 AS builder

WORKDIR /app
COPY . .

RUN gradle clean installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/install/ParcialWeb_2 ./ParcialWeb_2

EXPOSE 7000

CMD ["./ParcialWeb_2/bin/ParcialWeb_2"]