package clione_sql;

import org.junit.Test;
import tetz42.clione.gen.SQLGenerator;
import tetz42.clione.node.SQLNode;
import tetz42.clione.parsar.SQLParser;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tetz42.clione.SQLManager.params;
import static util.Utils.*;

public class ClioneSQLTest {

    @Test
    public void パラメタなしの普通のSQLをパースする() {
        SQLGenerator gen = new SQLGenerator();
        String sql = gen.genSql(null, new SQLParser("").parse(load("00-normal.sql")));

        assertThat(join(sql), is("select * employee"));
        assertThat(gen.params, is(emptyList()));
    }

    @Test
    public void パラメタ付きのSQLをパースする() {
        SQLNode node = new SQLParser("").parse(load("01-parameter.sql"));
        SQLGenerator gen = new SQLGenerator();
        String sql = gen.genSql(null, node);

        assertThat(join(sql), is("SELECT * FROM BOOK  WHERE AUTHOR = ?  ORDER BY BOOK_ID ASC"));
        assertThat(gen.params, contains(nullValue()));

        sql = gen.genSql(params("author", "AUTHOR"), node);
        assertThat(join(sql), is("SELECT * FROM BOOK  WHERE AUTHOR = ?  ORDER BY BOOK_ID ASC"));
        assertThat(gen.params.get(0), is((Object) "AUTHOR"));
    }

    @Test
    public void 条件パラメタ_$_付きのSQLをパースする() {
        SQLNode node = new SQLParser("").parse(load("13-clione_parameter_$.sql"));
        SQLGenerator gen = new SQLGenerator();
        String sql;

        sql = gen.genSql(params("age", 10).$("pref", "Sendai"), node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE  WHERE    AGE = ?    AND PREFECTURE = ?"));
        assertThat(gen.params, is(asList(10, "Sendai")));

        // 何も指定しないと条件句が消える
        sql = gen.genSql(null, node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE"));
        assertThat(gen.params, is(emptyList()));

        // １つ目の条件を指定しない（ANDが消える）
        sql = gen.genSql(params("pref", "Sendai"), node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE  WHERE    PREFECTURE = ?"));
        assertThat(gen.params, is(asList("Sendai")));
    }

    @Test
    public void 条件パラメタ_アンパサンド_付きのSQLをパースする() {
        SQLNode node = new SQLParser("").parse(load("13-clione_parameter_and.sql"));
        SQLGenerator gen = new SQLGenerator();
        String sql;

        sql = gen.genSql(params("order", true), node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE  ORDER BY NAME"));
        assertThat(gen.params, is(emptyList()));

        // 何も指定しないと条件句が消える
        sql = gen.genSql(null, node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE"));
        assertThat(gen.params, is(emptyList()));
    }

    @Test
    public void 条件パラメタ付き_特殊ケース_のSQLをパースする() {
        SQLNode node = new SQLParser("").parse(load("13-clione_parameter_sp.sql"));
        SQLGenerator gen = new SQLGenerator();
        String sql;

        sql = gen.genSql(params("age", 10), node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE WHERE AGE  =  ?"));
        assertThat(gen.params, is(asList(10)));

        // nullだと IS NULL に展開
        sql = gen.genSql(null, node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE WHERE AGE  IS NULL"));
        assertThat(gen.params, is(emptyList()));

        // Listを指定すると in に展開
        sql = gen.genSql(params("age", asList(10, 20, 30)), node);
        assertThat(join(sql), is("SELECT * FROM PEOPLE WHERE AGE  IN ( ?, ?, ?)"));
        assertThat(gen.params, is(asList(10, 20, 30)));
    }
}
