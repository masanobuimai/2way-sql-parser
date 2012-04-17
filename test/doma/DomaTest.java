package doma;

import org.junit.Test;
import org.seasar.doma.internal.jdbc.sql.SqlParser;
import org.seasar.doma.jdbc.SqlNode;

public class DomaTest {
    @Test
    public void testSample() {
        SqlParser parser = new SqlParser("select * from employee");
        SqlNode sqlNode = parser.parse();
        System.out.println("sqlNode = " + sqlNode);
    }
}
