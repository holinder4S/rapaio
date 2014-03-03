/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.core.stat

import rapaio.printer.Printable
import rapaio.data.Feature

/**
 * Finds the maximum value from a [[rapaio.data.Feature]].
 * Ignores missing elements.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:39 PM
 */
class Maximum extends Printable {

  private var _value: Double = _

  def value: Double = _value

  private def compute(feature: Feature): Maximum = {
    val filtered = feature.values.filter(x => !x.isNaN)
    _value = if (filtered.length == 0) filtered.max else Double.NaN
    this
  }

  override def buildSummary(sb: StringBuilder): Unit = sb.append("maximum\n%.10f".format(_value))
}

object Maximum {
  def apply(feature: Feature): Maximum = new Maximum().compute(feature)
}