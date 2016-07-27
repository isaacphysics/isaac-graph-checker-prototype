import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void parserTest1() throws IOException, CheckerException, ParseException {
        File file = new File("/Users/YUAN/Documents/workspace/isaac-graph-checker/src/test/java/parserTestCase.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        while (true) {
            String untrustedJSONString = br.readLine();
            if (untrustedJSONString == null) break;
            try {
                Parser.parseInputJSONString(untrustedJSONString);
            } catch (CheckerException exn) {
                assertThat(exn.getMessage(), is("Invalid JSON: key information missing"));
            }
        }
    }

}
