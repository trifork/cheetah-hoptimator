package com.linkedin.hoptimator;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.util.CloseableIterator;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.internals.OAuthBearerSaslClientCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.internals.OAuthBearerSaslClientProvider;
//import org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler;
import org.apache.flink.types.Row;

//import org.apache.flink.kafka.shaded.org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/** Runs Flink SQL in-process and iterates over the result. */
public class FlinkIterable implements Iterable<Object> {
  private final Logger logger = LoggerFactory.getLogger(FlinkIterable.class);

  private static final Logger fakelog = LoggerFactory.getLogger(
      OAuthBearerLoginCallbackHandler.class);

  private final String sql;
  private final long timeoutMillis;

  /** Execute SQL, stopping after timeoutMillis. */
  public FlinkIterable(String sql, int timeoutMillis) {
    this.sql = sql;
    this.timeoutMillis = timeoutMillis;
  }

  /** Execute SQL. */
  public FlinkIterable(String sql) {
    this.sql = sql;
    this.timeoutMillis = Long.MAX_VALUE;
  }

  /**
   * Returns an Iterator that returns results from a local Flink job.
   *
   * The Flink job runs on a local in-process worker.
   */
  @Override
  public Iterator<Object> iterator() {
    try {
      return closeExpired(datastream().map(r -> toArray(r)).executeAndCollect());
    } catch (Exception e) {
      return new ExceptionalIterator<>(e);
    }
  }

  /**
   * Returns an Iterator that returns results from a local Flink job, in Flink's
   * native Row container.
   *
   * The Flink job runs on a local in-process worker.
   */
  public Iterator<Row> rowIterator() {
    try {
      return closeExpired(datastream().executeAndCollect());
    } catch (Exception e) {
      return new ExceptionalIterator<>(e);
    }
  }

  /*
   * Iterates over the selected field/column only, with a limit set on the number
   * of collected elements
   */
  public <T> Iterable<T> field(int pos, Integer limit) {
    if (limit == null) {
      return this.field(pos);
    }
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        try {
          return datastream().map(r -> r.<T>getFieldAs(pos)).executeAndCollect(limit).iterator();
        } catch (Exception e) {
          return new ExceptionalIterator<>(e);
        }
      }
    };
  }

  /** Iterates over the selected field/column only. */
  public <T> Iterable<T> field(int pos) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        try {
          return closeExpired(datastream().map(r -> r.<T>getFieldAs(pos)).executeAndCollect());
        } catch (Exception e) {
          return new ExceptionalIterator<>(e);
        }
      }
    };
  }

  /** Iterates over the selected field/column only. */
  public <T> Iterable<T> field(String name) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        try {
          return closeExpired(datastream().map(r -> r.<T>getFieldAs(name)).executeAndCollect());
        } catch (Exception e) {
          return new ExceptionalIterator<>(e);
        }
      }
    };
  }

  private <T> Iterator<T> closeExpired(CloseableIterator<T> inner) {
    if (timeoutMillis < Long.MAX_VALUE) {
      (new Thread(() -> {
        try {
          Thread.sleep(timeoutMillis);
          inner.close();
        } catch (Exception e) {
          // nop
        }
      })).start();
    }
    return inner;
  }

  // cheetah hack to fix ; in some properties such as JAAS_CONFIG
  public static Map<String, String[]> ExtractDllsandQuery(String sql) {

    // Assume that DDL statements come first, and that the last statement is a query
    String[] statements = sql.trim().split("\\)\\s?;"); // handles ') ;' and ');' as delimiters
    String[] ddlRaw = Arrays.copyOfRange(statements, 0, statements.length - 1);
    // readd ) to the last statement to make it a valid DDL statement
    String[] ddl = Arrays.stream(ddlRaw)
        .map(s -> (s + ")")
            // .replace(
            // org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler.class.getName(),
            // org.apache.flink.kafka.shaded.org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler.class
            // .getName())
            // .replace(org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.class.getName(),
            // org.apache.flink.kafka.shaded.org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.class
            // .getName())
            .trim())
        .toArray(String[]::new);
    String query = statements[statements.length - 1];
    OAuthBearerSaslClientProvider.initialize(); // not part of public API

    Map<String, String[]> map = new HashMap<>();

    map.put("ddl", ddl);
    map.put("query", new String[] { query.trim() });

    return map;
  }

  private DataStream<Row> datastream() {
    Configuration conf = new Configuration();
    // cheetah class loading.
    // https://nightlies.apache.org/flink/flink-docs-release-1.18/docs/ops/debugging/debugging_classloading/
    // conf.setString("classloader.resolve-order", "parent-first");
    conf.setString("classloader.parent-first-patterns.additional",
        "org.apache.kafka");
    conf.setString("plugin.classloader.parent-first-patterns.additional",
        "org.apache.kafka");
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(conf);
    StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

    // Assume that DDL statements come first, and that the last statement is a query
    String[] statements = sql.trim().split(";");
    String[] ddl = Arrays.copyOfRange(statements, 0, statements.length - 1);
    String query = statements[statements.length - 1];

    // Cheetah Execute DDL statements
    Map<String, String[]> dllsandQuery = ExtractDllsandQuery(sql);
    ddl = dllsandQuery.get("ddl");
    query = dllsandQuery.get("query")[0];

    // Execute DDL statements
    for (String stmt : ddl) {
      // cheetah: don't print as properties might contain sensitive information
      logger.info("Flink DDL: {}", stmt.replaceAll("\\n", "").trim());
      if (!stmt.isEmpty() && !stmt.startsWith("--")) {
        tEnv.executeSql(stmt);
      }
    }

    // Run query
    logger.info("Flink SQL: {}", query.replaceAll("\\n", ""));
    Table resultTable = tEnv.sqlQuery(query);
    return tEnv.toChangelogStream(resultTable);
  }

  static private Object toArray(Row r) {
    if (r.getArity() == 1) {
      return r.getField(0);
    }
    Object[] fields = new Object[r.getArity()];
    for (int i = 0; i < fields.length; i++) {
      fields[i] = r.getField(i);
    }
    return fields;
  }

  static class ExceptionalIterator<T> implements Iterator<T> {
    private final Exception e;

    ExceptionalIterator(Exception e) {
      this.e = e;
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public T next() {
      throw new RuntimeException(e);
    }
  }
}
