/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.data;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import rapaio.core.BaseMath;
import rapaio.filters.BaseFilters;
import rapaio.io.CsvPersistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortVectorTest {

    @Test
    public void smokeTest() {
        Vector v = new IndexVector("test", 0);
        Vector sorted = new SortedVector(v, v.getComparator(true));
        assertTrue(sorted.isNumeric());
        assertFalse(sorted.isNominal());

        v = new NumericVector("test", 0);
        sorted = new SortedVector(v, v.getComparator(true));
        assertTrue(sorted.isNumeric());
        assertFalse(sorted.isNominal());

        v = new NominalVector("test", 0, new String[]{});
        sorted = new SortedVector(v, v.getComparator(true));
        assertFalse(sorted.isNumeric());
        assertTrue(sorted.isNominal());
    }

    @Test
    public void testSortIndex() {
        Vector index = new IndexVector("x", 10, 1, -1);
        index.setMissing(2);
        index.setMissing(5);
        index.setIndex(0, 1);

        assertEquals(10, index.getRowCount());
        Vector sort = new SortedVector(index, index.getComparator(true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getIndex(i - 1) <= sort.getIndex(i));
        }

        sort = new SortedVector(index, index.getComparator(false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getIndex(i - 1) >= sort.getIndex(i));
        }

        Vector second = new SortedVector(sort, sort.getComparator(true));
        for (int i = 1; i < second.getRowCount(); i++) {
            assertTrue(second.getIndex(i - 1) <= second.getIndex(i));
        }
    }

    @Test
    public void testSortNumeric() {
        Vector numeric = new NumericVector("x", new double[]{2., 4., 1.2, 1.3, 1.2, 0., 100.});

        assertEquals(7, numeric.getRowCount());
        Vector sort = new SortedVector(numeric, numeric.getComparator(true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getValue(i - 1) <= sort.getValue(i));
        }

        sort = new SortedVector(numeric, numeric.getComparator(false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getValue(i - 1) >= sort.getValue(i));
        }

        Vector second = new SortedVector(sort, sort.getComparator(true));
        for (int i = 1; i < second.getRowCount(); i++) {
            assertTrue(second.getIndex(i - 1) <= second.getIndex(i));
        }
    }

    @Test
    public void testSortNominal() {
        String[] dict = new String[]{"a", "Aa", "b", "c", "Cc"};
        Vector nominal = new NominalVector("c", 10, dict);

        for (int i = 0; i < 10; i++) {
            nominal.setLabel(i, dict[i % dict.length]);
        }
        nominal.setMissing(2);
        nominal.setMissing(3);
        nominal.setMissing(4);
        nominal.setMissing(5);

        Vector sort = new SortedVector(nominal, nominal.getComparator(true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) <= 0);
        }

        sort = new SortedVector(nominal, nominal.getComparator(false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) >= 0);
        }

        Vector second = new SortedVector(sort, sort.getComparator(true));
        for (int i = 1; i < second.getRowCount(); i++) {
            assertTrue(second.getIndex(i - 1) <= second.getIndex(i));
        }
    }

    @Test
    public void testGetterSetter() throws IOException {

        final String csv = new String("" +
                "c, 1, 1.\n" +
                "b, 3, 4.\n" +
                "a, 2, 2.5\n" +
                "d, 2, 4");

        CsvPersistence persistence = new CsvPersistence();
        persistence.setHasHeader(false);
        persistence.setColSeparator(',');
        persistence.setHasQuotas(false);
        Frame df = persistence.read("df", new ByteArrayInputStream(csv.getBytes()));

        Vector nominal = df.getCol(0);
        Vector index = BaseFilters.toIndex("x", df.getCol(1));
        Vector numeric = BaseFilters.toNumeric("x", df.getCol(2));

        // nominal

        HashMap<String, String> transform = new HashMap<>();
        transform.put("a", "c");
        transform.put("b", "a");
        transform.put("c", "b");
        transform.put("d", "d");
        Vector sort = new SortedVector(nominal);
        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setLabel(i, transform.get(sort.getLabel(i)));
        }

        assertEquals("b", nominal.getLabel(0));
        assertEquals("a", nominal.getLabel(1));
        assertEquals("c", nominal.getLabel(2));
        assertEquals("d", nominal.getLabel(3));

        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setIndex(i, 2);
            assertEquals(nominal.getDictionary()[2], nominal.getLabel(i));
            assertEquals(2, nominal.getIndex(i));
        }

        assertEquals(nominal.getDictionary().length, sort.getDictionary().length);
        for (int i = 0; i < nominal.getDictionary().length; i++) {
            assertEquals(nominal.getDictionary()[i], sort.getDictionary()[i]);
        }

        // numeric

        sort = new SortedVector("x", numeric, numeric.getComparator(true));
        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setValue(i, sort.getValue(i) + BaseMath.E);
        }
        assertEquals(BaseMath.E + 1., numeric.getValue(0), 1e-10);
        assertEquals(BaseMath.E + 2.5, numeric.getValue(2), 1e-10);
        assertEquals(BaseMath.E + 4, numeric.getValue(1), 1e-10);
        assertEquals(BaseMath.E + 4., numeric.getValue(3), 1e-10);


        // index

        sort = new SortedVector("x", index, index.getComparator(true));
        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setValue(i, sort.getIndex(i) + 10);
        }
        assertEquals(11, index.getIndex(0));
        assertEquals(12, index.getIndex(2));
        assertEquals(12, index.getIndex(3));
        assertEquals(13, index.getIndex(1));
    }

    @Test
    public void testMissing() {
        Vector v = new IndexVector("x", 1, 10, 1);
        v = new SortedVector(v, v.getComparator(true));
        for (int i = 0; i < 10; i += 3) {
            v.setMissing(i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i % 3 == 0 ? true : false, v.isMissing(i));
        }
    }
}
