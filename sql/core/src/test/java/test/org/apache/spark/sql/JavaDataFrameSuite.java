/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.apache.spark.sql;

import java.io.Serializable;
import java.util.Arrays;

import scala.collection.Seq;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.test.TestSQLContext;
import org.apache.spark.sql.test.TestSQLContext$;
import org.apache.spark.sql.types.*;

import static org.apache.spark.sql.functions.*;

public class JavaDataFrameSuite {
  private transient JavaSparkContext jsc;
  private transient SQLContext context;

  @Before
  public void setUp() {
    // Trigger static initializer of TestData
    TestData$.MODULE$.testData();
    jsc = new JavaSparkContext(TestSQLContext.sparkContext());
    context = TestSQLContext$.MODULE$;
  }

  @After
  public void tearDown() {
    jsc = null;
    context = null;
  }

  @Test
  public void testExecution() {
    DataFrame df = context.table("testData").filter("key = 1");
    Assert.assertEquals(df.select("key").collect()[0].get(0), 1);
  }

  /**
   * See SPARK-5904. Abstract vararg methods defined in Scala do not work in Java.
   */
  @Test
  public void testVarargMethods() {
    DataFrame df = context.table("testData");

    df.toDF("key1", "value1");

    df.select("key", "value");
    df.select(col("key"), col("value"));
    df.selectExpr("key", "value + 1");

    df.sort("key", "value");
    df.sort(col("key"), col("value"));
    df.orderBy("key", "value");
    df.orderBy(col("key"), col("value"));

    df.groupBy("key", "value").agg(col("key"), col("value"), sum("value"));
    df.groupBy(col("key"), col("value")).agg(col("key"), col("value"), sum("value"));
    df.agg(first("key"), sum("value"));

    df.groupBy().avg("key");
    df.groupBy().mean("key");
    df.groupBy().max("key");
    df.groupBy().min("key");
    df.groupBy().sum("key");

    // Varargs in column expressions
    df.groupBy().agg(countDistinct("key", "value"));
    df.groupBy().agg(countDistinct(col("key"), col("value")));
    df.select(coalesce(col("key")));
  }

  @Ignore
  public void testShow() {
    // This test case is intended ignored, but to make sure it compiles correctly
    DataFrame df = context.table("testData");
    df.show();
    df.show(1000);
  }

  public static class Bean implements Serializable {
    private double a = 0.0;
    private Integer[] b = new Integer[]{0, 1};

    public double getA() {
      return a;
    }

    public Integer[] getB() {
      return b;
    }
  }

  @Test
  public void testCreateDataFrameFromJavaBeans() {
    Bean bean = new Bean();
    JavaRDD<Bean> rdd = jsc.parallelize(Arrays.asList(bean));
    DataFrame df = context.createDataFrame(rdd, Bean.class);
    StructType schema = df.schema();
    Assert.assertEquals(new StructField("a", DoubleType$.MODULE$, false, Metadata.empty()),
      schema.apply("a"));
    Assert.assertEquals(
      new StructField("b", new ArrayType(IntegerType$.MODULE$, true), true, Metadata.empty()),
      schema.apply("b"));
    Row first = df.select("a", "b").first();
    Assert.assertEquals(bean.getA(), first.getDouble(0), 0.0);
    // Now Java lists and maps are converetd to Scala Seq's and Map's. Once we get a Seq below,
    // verify that it has the expected length, and contains expected elements.
    Seq<Integer> result = first.getAs(1);
    Assert.assertEquals(bean.getB().length, result.length());
    for (int i = 0; i < result.length(); i++) {
      Assert.assertEquals(bean.getB()[i], result.apply(i));
    }
  }
}
