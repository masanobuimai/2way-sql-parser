package doma;

import org.junit.Ignore;
import org.junit.Test;
import org.seasar.doma.internal.jdbc.sql.SqlParser;
import org.seasar.doma.jdbc.SqlNode;

public class DomaTest {
    @Test
    @Ignore
    public void testSample() {
        SqlParser parser = new SqlParser("select * from employee");
        SqlNode sqlNode = parser.parse();
        // Domaは既存のVisitorを流用できなそうなので，自力でVisitor作らないとダメっぽい。
        System.out.println("sqlNode = " + sqlNode);
    }
}
