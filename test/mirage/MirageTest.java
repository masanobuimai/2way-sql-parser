package mirage;

import jp.sf.amateras.mirage.parser.*;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static util.Utils.join;
import static util.Utils.load;

public class MirageTest {
    @Test
    public void パラメタなしの普通のSQLをパースする() {
        SqlParser sqlParser = new SqlParserImpl(load("00-normal.sql"));
        Node node = sqlParser.parse();
        SqlContext ctx = new SqlContextImpl();
        node.accept(ctx);

        assertThat(ctx.getSql(), is("select * employee"));
        assertThat(ctx.getBindVariables(), is(emptyArray()));
    }


    @Test
    public void パラメタ付きのSQLをパースする() {
        SqlParser sqlParser = new SqlParserImpl(load("01-parameter.sql"));
        Node node = sqlParser.parse();
        SqlContext ctx = new SqlContextImpl();
        ctx.addArg("author", "AUTHOR", "AUTHOR".getClass());
        node.accept(ctx);

        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK WHERE AUTHOR = ? ORDER BY BOOK_ID ASC"));
//        assertThat(ctx.getBindVariables(), arrayContaining(new Object[]{"AUTHOR"}));
        assertThat((String) ctx.getBindVariables()[0], is("AUTHOR"));
    }


    @Test
    public void 置換パラメタ付きのSQLをパースする() {
        SqlParser sqlParser = new SqlParserImpl(load("02-replace.sql"));
        Node node = sqlParser.parse();
        SqlContext ctx = new SqlContextImpl();
        ctx.addArg("orderByColumn", "AUTHOR", "AUTHOR".getClass());
        node.accept(ctx);

        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK ORDER BY AUTHOR ASC"));
        assertThat(ctx.getBindVariables(), is(emptyArray()));

        // パラメタを何も指定しないと変なSQLになる
        node.accept(ctx = new SqlContextImpl());
        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK ORDER BY  ASC"));
        assertThat(ctx.getBindVariables(), is(emptyArray()));
    }


    @Test
    public void 条件判定用パラメタ付きSQLをパースする() {
        SqlParser sqlParser = new SqlParserImpl(load("03-if_then_else.sql"));
        Node node = sqlParser.parse();
        SqlContext ctx = new SqlContextImpl();
        ctx.addArg("minPrice", null, Integer.class);
        ctx.addArg("maxPrice", 10, Integer.class);
        node.accept(ctx);

        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK    WHERE      PRICE <= ?"));
//        assertThat(ctx.getBindVariables(), arrayContaining(new Object[]{10}));
        assertThat((Integer) ctx.getBindVariables()[0], is(10));

        ctx = new SqlContextImpl();
        ctx.addArg("minPrice", 20, Integer.class);
        ctx.addArg("maxPrice", 400, Integer.class);
        node.accept(ctx);
        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK    WHERE            PRICE >= ?           AND PRICE <= ?"));
        assertThat(ctx.getBindVariables(), arrayContaining(new Object[]{20, 400}));


        // パラメタを何も指定しないと BEGIN～END が消える
        node.accept(ctx = new SqlContextImpl());
        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK"));
        assertThat(ctx.getBindVariables(), is(emptyArray()));

        // パラメタを片方だけ指定しても，両方指定したときと等価になる
        ctx = new SqlContextImpl();
        ctx.addArg("maxPrice", 10, Integer.class);
        node.accept(ctx);
        assertThat(join(ctx.getSql()), is("SELECT * FROM BOOK    WHERE            PRICE >= ?           AND PRICE <= ?"));
        assertThat(ctx.getBindVariables(), arrayContaining(new Object[]{10, 10}));  // ← ？？？
    }

}
