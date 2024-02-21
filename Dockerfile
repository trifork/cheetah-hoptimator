FROM gradle:8-jdk11 as builder
WORKDIR /home/
WORKDIR /build/

COPY . ./
RUN --mount=type=cache,target=/home/gradle/.gradle/caches ./gradlew build

#JAVA_VERSION=jdk-18.0.2.1+1
FROM eclipse-temurin:18 as run
WORKDIR /home/

COPY --from=builder /build/hoptimator-cli-integration/build/distributions/hoptimator-cli-integration.tar ./
RUN tar -xf hoptimator-cli-integration.tar && rm hoptimator-cli-integration.tar

COPY --from=builder /build/hoptimator-operator-integration/build/distributions/hoptimator-operator-integration.tar ./
RUN tar -xf hoptimator-operator-integration.tar && rm hoptimator-operator-integration.tar

ADD ./etc/* ./

# nobody
USER 65534

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["./hoptimator-cli-integration/bin/hoptimator-cli-integration -n '' -p '' -u jdbc:calcite:model=model.yaml"]

