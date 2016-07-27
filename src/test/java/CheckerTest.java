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

import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CheckerTest {

    @Test
    public void testcase() throws IOException, CheckerException, ParseException {
        File file = new File("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/test/java/checkerTestcase.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        while (true) {
            String descriptor = br.readLine();
            String trustedJSONString = br.readLine();
            String untrustedJSONString = br.readLine();
            String isCorrect = br.readLine();
            String errCause = br.readLine();
            br.readLine();
            if (trustedJSONString == null) break;

            String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
            if (isCorrect.equals("true")) {
                assertTrue("test '" + descriptor + "': should be true;", Parser.getIsCorrect(resultJSONString));
            } else if (isCorrect.equals("false")) {
                assertFalse("test '" + descriptor + "': should be false;", Parser.getIsCorrect(resultJSONString));
            }
            assertEquals("test '" + descriptor + "': errCause incorrect;", errCause, Parser.getErrCause(resultJSONString));
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCase1() throws IOException, CheckerException, ParseException {
        // with sketches, exact same
        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted1.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted1.json");
        thrown.expect(CheckerException.class);
        thrown.expectMessage("Invalid JSON: key information missing");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
    }

    @Test
    public void testCase2() throws IOException, CheckerException, ParseException {
        // no sketches, exact same
        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted2.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted2.json");
        thrown.expect(ParseException.class);
//        thrown.expectMessage("Invalid JSON: key information missing");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
    }

    @Test
    public void testCase3() throws IOException, CheckerException, ParseException {
        // exact shape, wrong position
        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted3.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted3.json");
        thrown.expect(CheckerException.class);
        thrown.expectMessage("Trusted curve and untrusted curve have different number of points");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
    }

    @Test
    public void testCase4() throws IOException, CheckerException, ParseException {
        // wrong number of curves
        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted4.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted4.json");
        thrown.expect(CheckerException.class);
        thrown.expectMessage("Invalid JSON: key information missing");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
    }

    @Test
    public void testCase5() throws IOException, CheckerException, ParseException {
        // wrong shape
        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted5.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted5.json");
        thrown.expect(CheckerException.class);
        thrown.expectMessage("Invalid canvasWidth");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
    }

    @Test
    public void testCase6() throws IOException, CheckerException, ParseException {
        // wrong labels
        String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted6.json");
        String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted6.json");
        thrown.expect(CheckerException.class);
        thrown.expectMessage("Invalid JSON: incorrect format");
        String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
    }

//    @Test
//    public void testCase7() throws IOException, CheckerException, ParseException {
//        File file = new File("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/test/java/checkerTestcase.txt");
//        BufferedReader br = new BufferedReader(new FileReader(file));
//
//        while (true) {
//            String descriptor = br.readLine();
//            String trustedJSONString = br.readLine();
//            String untrustedJSONString = br.readLine();
//            String isCorrect = br.readLine();
//            String errCause = br.readLine();
//            br.readLine();
//            if (trustedJSONString == null) break;
//
//            String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
//            if (isCorrect.equals("true")) {
//                assertTrue("test '" + descriptor + "': should be true;", Parser.getIsCorrect(resultJSONString));
//            } else if (isCorrect.equals("false")) {
//                assertFalse("test '" + descriptor + "': should be false;", Parser.getIsCorrect(resultJSONString));
//            }
//            assertEquals("test '" + descriptor + "': errCause incorrect;", errCause, Parser.getErrCause(resultJSONString));
//        }
//
//
//
//        try {
//            String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/trusted7.json");
//            String untrustedJSONString = WholeFileReader.readFile("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/main/json/untrusted7.json");
//            String resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
//        } catch (CheckerException exn) {
//            assertThat(exn.getMessage(), is("Invalid JSON: incorrect format"));
//        }
//    }

}
