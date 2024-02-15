# Cheetah instructions

Run hoptimor without kafka topic crds and use our own kafka etc

## Build
```sh
docker run --rm -v -v ${PWD}/.grade/cache/:/home/gradle/.gradle ${PWD}:/home/project -w /home/project openjdk:11 ./gradlew build
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