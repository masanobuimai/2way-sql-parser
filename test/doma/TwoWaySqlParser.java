package doma;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.seasar.doma.internal.expr.ExpressionEvaluator;
import org.seasar.doma.internal.expr.Value;
import org.seasar.doma.internal.jdbc.sql.NodePreparedSqlBuilder;
import org.seasar.doma.internal.jdbc.sql.PreparedSql;
import org.seasar.doma.internal.jdbc.sql.PreparedSqlParameter;
import org.seasar.doma.internal.jdbc.sql.SqlParser;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.DomaAbstractConfig;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.SqlNode;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.StandardDialect;
import org.seasar.doma.message.Message;
import org.seasar.doma.wrapper.ArrayWrapper;
import org.seasar.doma.wrapper.ArrayWrapperVisitor;
import org.seasar.doma.wrapper.BigDecimalWrapper;
import org.seasar.doma.wrapper.BigDecimalWrapperVisitor;
import org.seasar.doma.wrapper.BigIntegerWrapper;
import org.seasar.doma.wrapper.BigIntegerWrapperVisitor;
import org.seasar.doma.wrapper.BlobWrapper;
import org.seasar.doma.wrapper.BlobWrapperVisitor;
import org.seasar.doma.wrapper.BooleanWrapper;
import org.seasar.doma.wrapper.BooleanWrapperVisitor;
import org.seasar.doma.wrapper.ByteWrapper;
import org.seasar.doma.wrapper.ByteWrapperVisitor;
import org.seasar.doma.wrapper.BytesWrapper;
import org.seasar.doma.wrapper.BytesWrapperVisitor;
import org.seasar.doma.wrapper.ClobWrapper;
import org.seasar.doma.wrapper.ClobWrapperVisitor;
import org.seasar.doma.wrapper.DateWrapper;
import org.seasar.doma.wrapper.DateWrapperVisitor;
import org.seasar.doma.wrapper.DoubleWrapper;
import org.seasar.doma.wrapper.DoubleWrapperVisitor;
import org.seasar.doma.wrapper.EnumWrapper;
import org.seasar.doma.wrapper.EnumWrapperVisitor;
import org.seasar.doma.wrapper.FloatWrapper;
import org.seasar.doma.wrapper.FloatWrapperVisitor;
import org.seasar.doma.wrapper.IntegerWrapper;
import org.seasar.doma.wrapper.IntegerWrapperVisitor;
import org.seasar.doma.wrapper.LongWrapper;
import org.seasar.doma.wrapper.LongWrapperVisitor;
import org.seasar.doma.wrapper.NClobWrapper;
import org.seasar.doma.wrapper.NClobWrapperVisitor;
import org.seasar.doma.wrapper.ObjectWrapper;
import org.seasar.doma.wrapper.ObjectWrapperVisitor;
import org.seasar.doma.wrapper.ShortWrapper;
import org.seasar.doma.wrapper.ShortWrapperVisitor;
import org.seasar.doma.wrapper.StringWrapper;
import org.seasar.doma.wrapper.StringWrapperVisitor;
import org.seasar.doma.wrapper.TimeWrapper;
import org.seasar.doma.wrapper.TimeWrapperVisitor;
import org.seasar.doma.wrapper.TimestampWrapper;
import org.seasar.doma.wrapper.TimestampWrapperVisitor;
import org.seasar.doma.wrapper.UtilDateWrapper;
import org.seasar.doma.wrapper.UtilDateWrapperVisitor;
import org.seasar.doma.wrapper.Wrapper;

public class TwoWaySqlParser {

    private static Config config = new DefaultConfig();

    private static TypeResolver resolver = new TypeResolver();

    private final String sql;

    private final Map<String, Value> variableValues = new HashMap<String, Value>();

    public TwoWaySqlParser(String sql) {
        this.sql = sql;
    }

    public <T> void addValue(String name, Class<T> type, T value) {
        variableValues.put(name, new Value(type, value));
    }

    public Result execute() {
        SqlParser parser = new SqlParser(sql);
        SqlNode node = parser.parse();
        NodePreparedSqlBuilder builder = new NodePreparedSqlBuilder(config,
                SqlKind.SCRIPT, null, new ExpressionEvaluator(variableValues,
                        config.getDialect().getExpressionFunctions()));
        PreparedSql prearedSql = builder.build(node);
        return convert(prearedSql);
    }

    private Result convert(PreparedSql preparedSql) {
        Result result = new Result();
        result.sql = preparedSql.getRawSql();
        for (PreparedSqlParameter parameter : preparedSql.getParameters()) {
            result.values.add(parameter.getValue());
            result.valueTypes.add(resolver.resolve(parameter.getWrapper()));
        }
        return result;
    }

    public static class Result {
        public String sql;
        public List<Object> values = new ArrayList<Object>();
        public List<Class<?>> valueTypes = new ArrayList<Class<?>>();
    }

    private static class DefaultConfig extends DomaAbstractConfig {

        private static Dialect dialect = new StandardDialect();

        @Override
        public DataSource getDataSource() {
            return null;
        }

        @Override
        public Dialect getDialect() {
            return dialect;
        }
    }

    public static class TypeResolver implements
            ArrayWrapperVisitor<Class<?>, Void, RuntimeException>,
            BigDecimalWrapperVisitor<Class<?>, Void, RuntimeException>,
            BigIntegerWrapperVisitor<Class<?>, Void, RuntimeException>,
            BlobWrapperVisitor<Class<?>, Void, RuntimeException>,
            BooleanWrapperVisitor<Class<?>, Void, RuntimeException>,
            ByteWrapperVisitor<Class<?>, Void, RuntimeException>,
            BytesWrapperVisitor<Class<?>, Void, RuntimeException>,
            ClobWrapperVisitor<Class<?>, Void, RuntimeException>,
            DateWrapperVisitor<Class<?>, Void, RuntimeException>,
            DoubleWrapperVisitor<Class<?>, Void, RuntimeException>,
            FloatWrapperVisitor<Class<?>, Void, RuntimeException>,
            IntegerWrapperVisitor<Class<?>, Void, RuntimeException>,
            LongWrapperVisitor<Class<?>, Void, RuntimeException>,
            NClobWrapperVisitor<Class<?>, Void, RuntimeException>,
            ShortWrapperVisitor<Class<?>, Void, RuntimeException>,
            StringWrapperVisitor<Class<?>, Void, RuntimeException>,
            TimeWrapperVisitor<Class<?>, Void, RuntimeException>,
            TimestampWrapperVisitor<Class<?>, Void, RuntimeException>,
            EnumWrapperVisitor<Class<?>, Void, RuntimeException>,
            UtilDateWrapperVisitor<Class<?>, Void, RuntimeException>,
            ObjectWrapperVisitor<Class<?>, Void, RuntimeException> {

        public Class<?> resolve(Wrapper<?> wrapper) {
            return wrapper.accept(this, null);
        }

        @Override
        public Class<?> visitArrayWrapper(ArrayWrapper wrapper, Void p) {
            return Array.class;
        }

        @Override
        public Class<?> visitBigDecimalWrapper(BigDecimalWrapper wrapper, Void p) {
            return BigDecimal.class;
        }

        @Override
        public Class<?> visitBigIntegerWrapper(BigIntegerWrapper wrapper, Void p) {
            return BigInteger.class;
        }

        @Override
        public Class<?> visitBlobWrapper(BlobWrapper wrapper, Void p) {
            return Blob.class;
        }

        @Override
        public Class<?> visitBooleanWrapper(BooleanWrapper wrapper, Void p) {
            return Boolean.class;
        }

        @Override
        public Class<?> visitByteWrapper(ByteWrapper wrapper, Void p) {
            return Byte.class;
        }

        @Override
        public Class<?> visitBytesWrapper(BytesWrapper wrapper, Void p) {
            return byte[].class;
        }

        @Override
        public Class<?> visitClobWrapper(ClobWrapper wrapper, Void p) {
            return Clob.class;
        }

        @Override
        public Class<?> visitDateWrapper(DateWrapper wrapper, Void p) {
            return Date.class;
        }

        @Override
        public Class<?> visitDoubleWrapper(DoubleWrapper wrapper, Void p) {
            return Double.class;
        }

        @Override
        public Class<?> visitFloatWrapper(FloatWrapper wrapper, Void p) {
            return Float.class;
        }

        @Override
        public Class<?> visitIntegerWrapper(IntegerWrapper wrapper, Void p) {
            return Integer.class;
        }

        @Override
        public Class<?> visitLongWrapper(LongWrapper wrapper, Void p) {
            return Long.class;
        }

        @Override
        public Class<?> visitNClobWrapper(NClobWrapper wrapper, Void p) {
            return NClob.class;
        }

        @Override
        public Class<?> visitShortWrapper(ShortWrapper wrapper, Void p) {
            return Short.class;
        }

        @Override
        public Class<?> visitStringWrapper(StringWrapper wrapper, Void p) {
            return String.class;
        }

        @Override
        public Class<?> visitTimeWrapper(TimeWrapper wrapper, Void p) {
            return Time.class;
        }

        @Override
        public Class<?> visitTimestampWrapper(TimestampWrapper wrapper, Void p) {
            return Timestamp.class;
        }

        @Override
        public <E extends Enum<E>> Class<?> visitEnumWrapper(
                EnumWrapper<E> wrapper, Void p) {
            return Enum.class;
        }

        @Override
        public Class<?> visitUtilDateWrapper(UtilDateWrapper wrapper, Void p) {
            return java.util.Date.class;
        }

        @Override
        public Class<?> visitObjectWrapper(ObjectWrapper wrapper, Void p) {
            return Object.class;
        }

        @Override
        public Class<?> visitUnknownWrapper(Wrapper<?> wrapper, Void p) {
            throw new JdbcException(Message.DOMA2019, wrapper.getClass()
                    .getName());
        }
    }
}