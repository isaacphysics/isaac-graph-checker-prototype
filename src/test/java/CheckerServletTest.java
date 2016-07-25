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
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class CheckerServletTest {

    @Test
    public void testCase1() throws IOException, CheckerException, ServletException, ParseException {

        String untrustedJSONstring = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");

        MockHttpServletRequest mshReq = new MockHttpServletRequest();
        mshReq.addParameter("data", untrustedJSONstring);
        mshReq.setMethod("POST");
        mshReq.setServerPort(5000);
        mshReq.setServerName("localhost");
        mshReq.setProtocol("http");

        MockHttpServletResponse mhsResp = new MockHttpServletResponse();

        CheckerServlet ts = new CheckerServlet();
        ts.doPost(mshReq, mhsResp);

        String resultJSONString = mhsResp.getContentAsString();
        boolean isCorrect = Parser.getIsCorrect(resultJSONString);
        assertFalse(isCorrect);
    }

}
