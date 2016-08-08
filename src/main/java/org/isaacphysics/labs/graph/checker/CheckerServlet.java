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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

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

        System.out.println("==================================================");

        BufferedReader requestStringReader = request.getReader();
        String requestString = "";
        String line;

        // Read the JSON object
        while ((line = requestStringReader.readLine()) != null) {

            requestString += line;

        }

        ObjectMapper mapper = new ObjectMapper();

        try {

            @SuppressWarnings("unchecked")
            HashMap<String, String> req = mapper.readValue(requestString, HashMap.class);

            if (req.containsKey("description")) {
                System.out.println(req.get("description"));
                System.out.println("==================================================");
            }

            if (req.containsKey("target") && req.containsKey("test")) {

                // Get target and test graph data strings from JSON object
                String trustedJSONString = req.get("target");
                String untrustedJSONString = req.get("test");

                // Debug print
                System.out.println("Input target: \"" + trustedJSONString + "\"");
                System.out.println("Input test: \"" + untrustedJSONString + "\"");

                // Return
                System.out.println(Checker.test(untrustedJSONString, trustedJSONString));
                response.getWriter().println(Checker.test(untrustedJSONString, trustedJSONString));

            } else {
                response.getWriter().println("{\"error\" : \"No input!\"}");
                System.out.println("ERROR: No input!");
            }


        } catch (Exception e) {
            e.printStackTrace();
            // Got an exception when checking expressions.
            response.getWriter().println("{\"error\" : true}");
            System.out.println("ERROR: Parser cannot parse input!");

        }

        System.out.println("==================================================\n");

    }

}

