/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Isak Karlsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.briljantframework.data.reader;

import java.io.BufferedReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.math.NumberUtils;
import org.briljantframework.data.resolver.Resolve;
import org.briljantframework.data.resolver.Resolver;
import org.briljantframework.data.series.Type;
import org.briljantframework.data.series.Types;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 * Reads a delimited file and produces one {@linkplain DataEntry data entry} for each line in the
 * file.
 *
 * @author Isak Karlsson
 * @see org.briljantframework.data.parser.CsvParser Read a data frame from a Csv-file
 */
public class CsvEntryReader implements EntryReader {

  private final CsvParser csvParser;
  private final String missingValue;
  private final DateTimeFormatter dateTimeFormatter;
  private String[] current = null;
  private List<Type> types = null;

  public CsvEntryReader(CsvParserSettings settings, DateTimeFormatter dateTimeFormatter,
      Reader reader, String missingValue) {
    csvParser = new CsvParser(settings);
    csvParser.beginParsing(new BufferedReader(reader));
    this.missingValue = missingValue;
    this.dateTimeFormatter = dateTimeFormatter;
  }

  @Override
  public List<Type> getTypes() {
    if (types == null) {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      types = new ArrayList<>();
      for (String repr : current) {
        if (repr != null) {
          repr = repr.trim();
        }
        if (repr == null || repr.equals(missingValue)) {
          types.add(Types.OBJECT);
        } else if (NumberUtils.isNumber(repr)) {
          Number number = NumberUtils.createNumber(repr);
          if (number.intValue() == 0) {
            types.add(Types.DOUBLE);
          } else {
            types.add(Types.getType(number.getClass()));
          }
        } else if (tryParseAsDateTime(repr)) {
          types.add(Types.getType(LocalDateTime.class));
        } else if (tryParseAsDate(repr)) {
          types.add(Types.getType(LocalDate.class));
        } else {
          types.add(Types.OBJECT);
        }
      }
    }
    return Collections.unmodifiableList(types);
  }

  private boolean tryParseAsDate(String repr) {
    Resolver<LocalDate> resolver = Resolve.getResolver(LocalDate.class);
    return resolver != null && resolver.resolve(repr) != null;
  }

  private boolean tryParseAsDateTime(String repr) {
    Resolver<LocalDateTime> resolver = Resolve.getResolver(LocalDateTime.class);
    return resolver != null && resolver.resolve(repr) != null;
  }

  @Override
  public DataEntry next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    DataEntry entry = new StringDataEntry(current, missingValue);
    current = null;
    return entry;
  }

  @Override
  public boolean hasNext() {
    if (current == null) {
      current = csvParser.parseNext();
    }
    return current != null;
  }
}
