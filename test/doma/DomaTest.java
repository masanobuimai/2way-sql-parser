package doma;

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static util.Utils.join;
import static util.Utils.load;

import java.util.List;

import org.junit.Test;
import org.seasar.doma.jdbc.JdbcException;

public class DomaTest {

    @Test
    public void パラメタなしの普通のSQLをパースする() {
        TwoWaySqlParser parser = new TwoWaySqlParser(load("20-doma_normal.sql"));
        TwoWaySqlParser.Result result = parser.execute();

        assertThat(result.sql, is("select * employee"));
        assertThat(result.values, isA(List.class));
    }

    @Test
    public void パラメタ付きのSQLをパースする() {
        TwoWaySqlParser parser = new TwoWaySqlParser(
                load("21-doma_parameter.sql"));
        parser.addValue("author", String.class, "AUTHOR");
        TwoWaySqlParser.Result result = parser.execute();

        assertThat(join(result.sql),
                is("SELECT * FROM BOOK WHERE AUTHOR = ? ORDER BY BOOK_ID ASC"));
        assertThat((String) result.values.get(0), is("AUTHOR"));
    }

    @Test
    public void 置換パラメタ付きのSQLをパースする() {
        TwoWaySqlParser parser = new TwoWaySqlParser(
                load("22-doma_replace.sql"));
        parser.addValue("orderByColumn", String.class, "AUTHOR");
        TwoWaySqlParser.Result result = parser.execute();

        assertThat(join(result.sql),
                is("SELECT * FROM BOOK ORDER BY AUTHOR ASC"));
        assertThat(result.values, is(emptyIterable()));
    }

    @Test(expected = JdbcException.class)
    public void 置換パラメタ付きのSQLをパースする_パラメタを何も指定しない場合() {
        TwoWaySqlParser parser = new TwoWaySqlParser(
                load("22-doma_replace.sql"));
        parser.execute();
    }

    @Test
    public void 条件判定用パラメタ付きSQLをパースする() {
        TwoWaySqlParser parser = new TwoWaySqlParser(
                load("23-doma_if_then_else.sql"));
        parser.addValue("minPrice", Integer.class, null);
        parser.addValue("maxPrice", Integer.class, 10);
        TwoWaySqlParser.Result result = parser.execute();

        assertThat(join(result.sql),
                is("SELECT * FROM BOOK   WHERE            PRICE <= ?"));
        assertThat((Integer) result.values.get(0), is(10));
    }

    @Test
    public void 条件判定用パラメタ付きSQLをパースする_全パラメタがnullの場合() {
        TwoWaySqlParser parser = new TwoWaySqlParser(
                load("23-doma_if_then_else.sql"));
        parser.addValue("minPrice", Integer.class, null);
        parser.addValue("maxPrice", Integer.class, null);
        TwoWaySqlParser.Result result = parser.execute();

        assertThat(join(result.sql), is("SELECT * FROM BOOK"));
        assertThat(result.values, is(emptyIterable()));
    }

    @Test(expected = JdbcException.class)
    public void 条件判定用パラメタ付きSQLをパースする_SQLに登場するパラメタをすべて指定しない場合() {
        TwoWaySqlParser parser = new TwoWaySqlParser(
                load("23-doma_if_then_else.sql"));
        parser.addValue("maxPrice", Integer.class, 10);
        parser.execute();
    }
}
