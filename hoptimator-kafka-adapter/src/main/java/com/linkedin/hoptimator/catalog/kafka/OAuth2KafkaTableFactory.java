package com.linkedin.hoptimator.catalog.kafka;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.calcite.adapter.kafka.KafkaStreamTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuth2KafkaTableFactory extends org.apache.calcite.adapter.kafka.KafkaTableFactory {

    private final Logger logger = LoggerFactory.getLogger(OAuth2KafkaTableFactory.class);

    public OAuth2KafkaTableFactory() {
        super();
    }

    @Override
    public KafkaStreamTable create(SchemaPlus schema,
            String name,
            Map<String, Object> operand,
            @Nullable RelDataType rowType) {
        Map<String, Object> consumerParams = (Map<String, Object>) operand.get("consumer.params");
        // cheetah secrets handling
        if (System.getenv("KAFKA_SASL_JAAS_CONFIG") != null) {
            logger.info("Using KAFKA_SASL_JAAS_CONFIG to replace " + consumerParams.get("sasl.jaas.config") + " with "
                    + System.getenv("KAFKA_SASL_JAAS_CONFIG"));
            consumerParams.put("sasl.jaas.config", System.getenv("KAFKA_SASL_JAAS_CONFIG"));
        }
        return super.create(schema, name, operand, rowType);
    }
}
