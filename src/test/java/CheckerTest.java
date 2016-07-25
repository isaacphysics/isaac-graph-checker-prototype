/**
 * Copyright 2016 Junwei Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertFalse;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import java.io.IOException;

public class CheckerTest {

    @Test
    public void testCase1() throws IOException, CheckerException, ParseException {
        String trustedJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
        String untrustedJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
        boolean isCorrect = Parser.getIsCorrect(resultJSONString);
        assertFalse(isCorrect);
    }

}
