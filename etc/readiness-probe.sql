
!connect "jdbc:calcite:model=/etc/config/model.yaml" "" ""

SELECT * FROM DATAGEN.PERSON;
SELECT * FROM DATAGEN.COMPANY;
--SELECT * FROM INVENTORY."products_on_hand" LIMIT 1;
--SELECT * FROM RAWKAFKA."sql-job-sink" LIMIT 0;
