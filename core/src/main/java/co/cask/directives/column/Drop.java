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

package co.cask.directives.column;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.wrangler.api.Arguments;
import co.cask.wrangler.api.DirectiveExecutionException;
import co.cask.wrangler.api.DirectiveParseException;
import co.cask.wrangler.api.RecipeContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.UDD;
import co.cask.wrangler.api.parser.ColumnNameList;
import co.cask.wrangler.api.parser.TokenType;
import co.cask.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * This class <code>Drop</code> implements a directive that will drop
 * the list of columns specified.
 */
@Plugin(type = UDD.Type)
@Name(Drop.DIRECTIVE_NAME)
@Description("Drop one or more columns.")
public class Drop implements UDD {
  public static final String DIRECTIVE_NAME = "drop";

  // Columns to be dropped.
  private List<String> columns;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("column", TokenType.COLUMN_NAME_LIST);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    ColumnNameList cols = args.value("column");
    columns = cols.value();
  }

  @Override
  public List<Row> execute(List<Row> rows, RecipeContext context)
    throws DirectiveExecutionException {
    for (Row row : rows) {
      for (String column : columns) {
        int idx = row.find(column.trim());
        if (idx != -1) {
          row.remove(idx);
        }
      }
    }
    return rows;
  }
}
