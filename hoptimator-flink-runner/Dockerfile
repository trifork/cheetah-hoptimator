FROM gradle:8-jdk11 as builder
WORKDIR /home/
WORKDIR /build/

COPY . ./
RUN --mount=type=cache,target=/home/gradle/.gradle/caches ./gradlew build

FROM flink:1.16-java11
RUN mkdir -p /checkpoints && chmod 777 /checkpoints
COPY --from=builder /build/hoptimator-flink-runner/build/libs/hoptimator-flink-runner-all.jar /opt/hoptimator-flink-runner.jar
