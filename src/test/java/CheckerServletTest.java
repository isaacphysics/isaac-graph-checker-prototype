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

        String drawnJSONString = FileReader.readFile("/Users/YUAN/Desktop/nodejs/public/json/drawn.json");

        MockHttpServletRequest mshReq = new MockHttpServletRequest();
        mshReq.addParameter("data", drawnJSONString);
        mshReq.setMethod("POST");
        mshReq.setServerPort(5000);
        mshReq.setServerName("localhost");
        mshReq.setProtocol("http");

        MockHttpServletResponse mhsResp = new MockHttpServletResponse();

        CheckerServlet ts = new CheckerServlet();
        ts.doPost(mshReq, mhsResp);

        String resultJSONString = mhsResp.getContentAsString();
        HashMap<String, Object> data = Parser.parseResultJSONString(resultJSONString);
        boolean isCorrect = (Boolean) data.get("isCorrect");
        assertTrue(isCorrect);
    }

}
