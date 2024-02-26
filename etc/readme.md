# Local setup

## Init

```sh
docker compose up -d --build
docker exec -it hoptimator-cli ./hoptimator-cli-integration/bin/hoptimator-cli-integration --historyfile=/tmp/history
docker exec -it hoptimator-cli ./hoptimator-cli-integration/bin/hoptimator-cli-integration --historyfile=/tmp/history --run=/etc/config/testoauth2.sql
```


## Verify

```sql
!connect "jdbc:calcite:model=/etc/config/rawkafka.model.yaml" "" ""
!tables

--java.io.IOException: Failed to deserialize CSV row '{"deviceId":"6","timestamp":"2024-02-20T12:36:03,613527394+00:00","value":18813}'.
--Unexpected character (':' (code 58)): Expected column separator character (',' (code 44)) or end-of-line
SELECT * FROM RAWKAFKA."JobNameInputTopic" LIMIT 1;
--Failed to deserialize CSV row '6,2024-02-20T12:36:03,613527394+00:00,18813'.
-- Too many entries: expected at most 1 (value #1 (19 chars) "2024-02-20T12:36:03")
SELECT * FROM RAWKAFKA."CSVTOPICTEST" LIMIT 1;
-- 'value' not found
!insert into RAWKAFKA."sql-job-sink" SELECT "value", "deviceId" AS KEY FROM RAWKAFKA."JobNameInputTopic"

SELECT STREAM * FROM KAFKA."JobNameInputTopic" LIMIT 1;

SELECT STREAM * FROM KAFKA."JobNameInputTopicString" LIMIT 1;

```