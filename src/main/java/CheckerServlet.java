import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("-----------------------");
        System.out.println("Request handler 'test' is called.");

        String untrustedJSONString = request.getParameter("data");

        String resultJSONString = "";
        try {
            String trustedJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/test.json");
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

