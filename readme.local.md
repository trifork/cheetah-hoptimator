# Cheetah instructions

Run hoptimor without kafka topic crds and use our own kafka etc

## Build
```sh
docker run --rm -v ${PWD}:/home/project -v ${PWD}/.grade/cache/:/home/gradle/.gradle -w /home/project openjdk:11 ./gradlew build
docker build . -t hoptimator
docker build hoptimator-flink-runner -t hoptimator-flink-runner
```

## Deploy

```sh
kubectl config set-context gke_platypus-350508_europe-north1-a_cheetah
kubectl create namespace hoptimator --dry-run=server -o yaml | kubectl apply -f -
kubectl config set-context --current --namespace=hoptimator
kubectl create configmap hoptimator-configmap --from-file=model.yaml=test-model.yaml --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f ./deploy
```

inject jaas as env
tablefactory vs svhemafactory
ssl.truststore.password as env
<https://github.com/apache/calcite/blob/main/elasticsearch/src/main/java/org/apache/calcite/adapter/elasticsearch/ElasticsearchSchemaFactory.java> to openswarch with security and see <https://calcite.apache.org/docs/elasticsearch_adapter.html>
<https://medium.com/@masayuki/apache-calcite-code-reading-part-2-594e8ca17acf> look at avro schema
<https://calcite.apache.org/docs/kafka_adapter.html>