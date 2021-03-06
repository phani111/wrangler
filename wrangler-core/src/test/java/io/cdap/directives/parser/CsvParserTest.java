/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests {@link CsvParser}
 */
public class CsvParserTest {
  @Test
  public void testParseCSV() throws Exception {
    String[] directives = new String[] {
      "parse-as-csv body , false",
      "drop body",
      "rename body_1 date",
      "parse-as-csv date / false",
      "rename date_1 month",
      "rename date_2 day",
      "rename date_3 year"
    };

    List<Row> rows = Arrays.asList(
      new Row("body", "07/29/2013,Debt collection,\"Other (i.e. phone, health club, etc.)\",Cont'd attempts collect " +
        "debt not owed,Debt is not mine,,,\"NRA Group, LLC\",VA,20147,,N/A,Web,08/07/2013,Closed with non-monetary " +
        "relief,Yes,No,467801"),
      new Row("body", "07/29/2013,Mortgage,Conventional fixed mortgage,\"Loan servicing, payments, escrow account\",," +
        ",,Franklin Credit Management,CT,06106,,N/A,Web,07/30/2013,Closed with explanation,Yes,No,475823")
    );

    rows = TestingRig.execute(directives, rows);
    Assert.assertEquals(2, rows.size());
    Assert.assertEquals("07/29/2013", rows.get(0).getValue("date"));
  }

  @Test
  public void testHeaders() throws Exception {
    String[] directives = new String[] { "parse-as-csv body , true" };

    List<Row> rows = Arrays.asList(
      new Row("body", "first name, last  \t  name"),
      new Row("body", "alice,zed")
    );

    rows = TestingRig.execute(directives, rows);
    Assert.assertEquals(1, rows.size());
    Assert.assertEquals("alice", rows.get(0).getValue("first_name"));
    Assert.assertEquals("zed", rows.get(0).getValue("last_name"));
  }
}
