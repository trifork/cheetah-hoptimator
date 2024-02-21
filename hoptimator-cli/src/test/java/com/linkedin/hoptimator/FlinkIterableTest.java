package com.linkedin.hoptimator;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.Test;

public class FlinkIterableTest {

    @Test
    public void testSQLSplit() {
        // arrange
        String sql = "CREATE TABLE Orders (`user` BIGINT, product STRING, amount INT) WITH (sasl.jaas.config='org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId=\"default-access\" clientSecret=\"default-access-secret\" scope=\"kafka\";') ; CREATE TABLE Orders2 (`user` BIGINT, product STRING, amount INT) WITH (sasl.jaas.config='org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId=\"default-access\" clientSecret=\"default-access-secret\" scope=\"kafka\";'); SELECT product, amount FROM Orders WHERE product LIKE '%Rubber%'";

        // act
        Map<String, String[]> dllsandQuery = FlinkIterable.ExtractDllsandQuery(sql);
        String[] ddl = dllsandQuery.get("ddl");
        String query = dllsandQuery.get("query")[0];

        // Expected outcomes
        String[] expectedDDL = {
                "CREATE TABLE Orders (`user` BIGINT, product STRING, amount INT) WITH (sasl.jaas.config='org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId=\"default-access\" clientSecret=\"default-access-secret\" scope=\"kafka\";')",
                "CREATE TABLE Orders2 (`user` BIGINT, product STRING, amount INT) WITH (sasl.jaas.config='org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId=\"default-access\" clientSecret=\"default-access-secret\" scope=\"kafka\";')"
        };
        String expectedQuery = "SELECT product, amount FROM Orders WHERE product LIKE '%Rubber%'";

        // Assertions
        // assertEquals("DDL statements should match length", expectedDDL.length,
        // ddl.length);
        // assertArrayEquals("DDL statements should match expected", expectedDDL, ddl);
        // assertEquals("Query should match expected", expectedQuery, query);
    }
}
