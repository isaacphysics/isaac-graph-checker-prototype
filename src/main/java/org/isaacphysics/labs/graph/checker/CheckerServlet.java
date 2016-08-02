package org.isaacphysics.labs.graph.checker;
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A HttpServlet that receives POST request, and transfers the request data to Checker.
 */
public class CheckerServlet extends HttpServlet {

    /**
     * handles POST request with url '/test'.
     * @param request A HttpServletRequest
     * @param response A HttpServletResponse
     * @throws ServletException thrown when some servlet related exceptions occur
     * @throws IOException thrown when fail to read trusted data from file
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("-----------------------");
        System.out.println("Request handler 'test' is called.");

        String untrustedJSONString = request.getParameter("data");

        String resultJSONString = "";
        try {
            String trustedJSONString = WholeFileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
            resultJSONString = Checker.test(trustedJSONString, untrustedJSONString);
        } catch (CheckerException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

//      response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(resultJSONString);
    }

}

