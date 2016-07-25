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
