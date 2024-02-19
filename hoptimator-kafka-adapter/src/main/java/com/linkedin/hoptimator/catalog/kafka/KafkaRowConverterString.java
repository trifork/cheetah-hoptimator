// https://github.com/apache/calcite/blob/main/kafka/src/main/java/org/apache/calcite/adapter/kafka/KafkaRowConverterImpl.java#L30
package com.linkedin.hoptimator.catalog.kafka;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;

import org.apache.calcite.adapter.kafka.KafkaRowConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Implementation of {@link KafkaRowConverter}. Both key and value
 * are saved as {@code string}.
 */
class KafkaRowConverterString implements KafkaRowConverter<String, String> {
  /**
   * Generates a row schema for a given Kafka topic.
   *
   * @param topicName Kafka topic name
   * @return row type
   */
  @Override
  public RelDataType rowDataType(final String topicName) {
    final RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    final RelDataTypeFactory.Builder fieldInfo = typeFactory.builder();

    fieldInfo.add("MSG_PARTITION", typeFactory.createSqlType(SqlTypeName.INTEGER)).nullable(false);
    fieldInfo.add("MSG_TIMESTAMP", typeFactory.createSqlType(SqlTypeName.BIGINT)).nullable(false);
    fieldInfo.add("MSG_OFFSET", typeFactory.createSqlType(SqlTypeName.BIGINT)).nullable(false);
    fieldInfo.add("MSG_KEY", typeFactory.createSqlType(SqlTypeName.VARCHAR)).nullable(true);
    fieldInfo.add("MSG_VALUE", typeFactory.createSqlType(SqlTypeName.VARCHAR))
        .nullable(false);

    fieldInfo.add("TOPIC_NAME", typeFactory.createSqlType(SqlTypeName.VARCHAR)).nullable(false);
    fieldInfo.add("TIMESTAMP_TYPE", typeFactory.createSqlType(SqlTypeName.VARCHAR)).nullable(true);

    return fieldInfo.build();
  }

  /**
   * Parses and reformats Kafka messages from consumer, to fit with row schema
   * defined as {@link #rowDataType(String)}.
   *
   * @param message Raw Kafka message record
   * @return fields in the row
   */
  @Override
  public Object[] toRow(final ConsumerRecord<String, String> message) {
    Object[] fields = new Object[7];

    fields[0] = message.partition();
    fields[1] = message.timestamp();
    fields[2] = message.offset();

    fields[3] = message.key();
    fields[4] = message.value();

    fields[5] = message.topic();
    fields[6] = message.timestampType().name;

    return fields;
  }
}