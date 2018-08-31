/*
 *  Copyright © 2017 Cask Data, Inc.
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

package co.cask.directives.parser;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.wrangler.api.Arguments;
import co.cask.wrangler.api.Directive;
import co.cask.wrangler.api.DirectiveExecutionException;
import co.cask.wrangler.api.DirectiveParseException;
import co.cask.wrangler.api.ErrorRowException;
import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.annotations.Categories;
import co.cask.wrangler.api.parser.ColumnName;
import co.cask.wrangler.api.parser.Text;
import co.cask.wrangler.api.parser.TokenType;
import co.cask.wrangler.api.parser.UsageDefinition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * A Executor to parse date into Date object.
 */
@Plugin(type = Directive.Type)
@Name("parse-as-simple-date")
@Categories(categories = { "parser", "date"})
@Description("Parses a column as date using format.")
public class ParseSimpleDate implements Directive {
  public static final String NAME = "parse-as-simple-date";
  private String column;
  private SimpleDateFormat formatter;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    builder.define("format", TokenType.TEXT);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.column = ((ColumnName) args.value("column")).value();
    String format = ((Text) args.value("format")).value();
    this.formatter = new SimpleDateFormat(format);
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context)
    throws DirectiveExecutionException, ErrorRowException {
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx != -1) {
        Object object = row.getValue(idx);
        // If the data in the cell is null or is already of date format, then
        // continue to next row.
        if (object == null || object instanceof ZonedDateTime) {
          continue;
        }
        if (object instanceof String) {
          try {
            // This implementation first creates Date object and then converts it into ZonedDateTime.
            // DateTimeFormat java 8 api to apply pattern on a string to get ZonedDateTime, throws exception when Zone
            // and Time is not provided in the pattern.
            // This behavior is because of JDK 1.8 bug - https://bugs.openjdk.java.net/browse/JDK-8033662
            Date date = formatter.parse(object.toString());

            // java.util.Date.toString() uses default system timezone, so ZonedDateTime instance is created with the
            // system default ZoneId
            ZonedDateTime zonedDateTime = ZonedDateTime.from(date.toInstant().atZone(ZoneId.systemDefault()));
            row.setValue(idx, zonedDateTime);
          } catch (ParseException e) {
            throw new ErrorRowException(String.format("Failed to parse '%s' with pattern '%s'",
                                                      object, formatter.toPattern()), 1);
          }
        } else {
          throw new ErrorRowException(
            String.format("%s : Invalid type '%s' of column '%s'. Should be of type String.", toString(),
                          object != null ? object.getClass().getName() : "null", column), 2
          );
        }
      }
    }
    return rows;
  }
}
