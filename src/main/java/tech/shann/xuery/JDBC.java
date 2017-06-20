package tech.shann.xuery;

import com.google.common.base.CaseFormat;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by shann on 17/6/15.
 */
public class JDBC {

    private static Logger log = Logger.getLogger(JDBC.class);

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate named;

    private JDBC() {}

    public JDBC(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.named = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     *
     * @param colonSql	以 :=propertyName 作为占位符
     * @param mapORpojo	sql语句的参数，要么是一个Map，要么是一个POJO
     * @param beanClazz	sql查询结果的载体，是一个POJO
     * @return 不为null，但是size可能为0
     */
    public <T> List<T> queryForBeanList(String colonSql, Object mapORpojo, Class<T> beanClazz){
        if(isBaseType(beanClazz)){
            throw new RuntimeException("beanClazz是一个基础类型！");
        }

        log.debug(Query.readableSql(colonSql, mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        List<T> list = this.named.query(
                colonSql,
                source,
                new BeanPropertyRowMapper(beanClazz));

        log.debug(new StringBuilder("结果行数：").append(list.size()));

        return list;

    }
    public <T> List<T> queryForBeanList(Query q, Class<T> beanClazz){
        return queryForBeanList(q.getSqlStr(),q.getArgsMap(),beanClazz);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> List<T> queryForSingleValueList(String colonSql, Object mapORpojo, Class<T> baseClazz){
        if(!isBaseType(baseClazz)){
            throw new RuntimeException("baseClazz不是一个基础类型！");
        }

        log.debug(Query.readableSql(colonSql, mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        List<T> list = this.named.query(
                colonSql,
                source,
                new SingleColumnRowMapper(baseClazz));

        log.debug(new StringBuilder("结果行数：").append(list.size()));

        return list;
    }
    public <T> List<T> queryForSingleValueList(Query q, Class<T> baseClazz){
        return queryForSingleValueList(q.getSqlStr(),q.getArgsMap(),baseClazz);
    }

    public List<Map<String, Object>> queryForMapList(String colonSql, Object mapORpojo) {

        log.debug(Query.readableSql(colonSql, mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        List<Map<String, Object>> list = this.named.queryForList(colonSql, source);

        log.debug(new StringBuilder("结果行数：").append(list.size()));

        return list;
    }
    public List<Map<String, Object>> queryForMapList(Query q) {
        return queryForMapList(q.getSqlStr(),q.getArgsMap());
    }

    public <T> T queryForBean(String colonSql, Object mapORpojo, Class<T> beanClazz){
        if(isBaseType(beanClazz)){
            throw new RuntimeException("beanClazz是一个基础类型！");
        }

        log.debug(Query.readableSql(colonSql,mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        try {
            T t = this.named.queryForObject(
                    colonSql,
                    source,
                    new BeanPropertyRowMapper<T>(beanClazz)
            );


            return t;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.debug("结果行数：0");

            return null;
        } finally {

        }
    }
    public <T> T queryForBean(Query q, Class<T> beanClazz){
        return queryForBean(q.getSqlStr(),q.getArgsMap(),beanClazz);
    }

    public <T> T queryForSingleValue(String colonSql, Object mapORpojo, Class<T> baseClazz){
        if(!isBaseType(baseClazz)){
            throw new RuntimeException("baseClazz不是一个基础类型！");
        }

        log.debug(Query.readableSql(colonSql,mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        try {
            T t = this.named.queryForObject(
                    colonSql,
                    source,
                    baseClazz);

            log.debug("结果行数：1");

            return t;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.debug("结果行数：0");

            return null;
        } finally {

        }
    }
    public <T> T queryForSingleValue(Query q, Class<T> baseClazz){
        return queryForSingleValue(q.getSqlStr(),q.getArgsMap(),baseClazz);
    }

    public Map<String, Object> queryForMap(String colonSql, Object mapORpojo) {
        log.debug(Query.readableSql(colonSql,mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        try {
            Map<String,Object> map =
                    this.named.queryForMap(colonSql, source);

            log.debug("结果行数：1");

            return map;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.debug("结果行数：0");

            return null;
        } finally {

        }
    }
    public Map<String, Object> queryForMap(Query q) {
        return queryForMap(q.getSqlStr(),q.getArgsMap());
    }

    public Integer queryForInt(String colonSql, Object mapORpojo) {
        log.debug(Query.readableSql(colonSql,mapORpojo));

        SqlParameterSource source = transToSource(mapORpojo);

        Integer i = this.named.queryForObject(colonSql, source, Integer.class);

        log.debug(new StringBuilder("结果：").append(i));

        return i;
    }
    public Integer queryForInt(Query q) {
        return queryForInt(q.getSqlStr(),q.getArgsMap());
    }

    /**
     * 插入一条数据，插入规则跟直接运行sql一致
     * @param tableName
     * @param mapORpojo
     * @return 成功返回1，失败抛出异常
     */
    public int insert(String tableName, Object mapORpojo) {
        SimpleJdbcInsert insertTest = new SimpleJdbcInsert(dataSource).withTableName(tableName);

        log.debug(new StringBuilder("往表'").append(tableName).append("'插入一行"));

        SqlParameterSource source = transToSource(mapORpojo);

        int result = insertTest.execute(source);
        if (result < 1)
            throw new RuntimeException("insert保存数据出错！");

        log.debug(new StringBuilder("插入成功：").append(result));

        return result;
    }


    /**
     * 插入一条数据，
     * 规则：
     * 	1，要求id_column在数据库上是自动生成的字段，否则抛出异常。
     *  2，在mapORpojo中id_column的值会被忽略，不会写入数据库。
     *  3，返回的是指定字段id_column的值，而且是数据库自动生成的。
     *
     * @param table_name 蛇形命名
     * @param mapOrPojo 驼峰命名
     * @param id_column 返回值的字段名，蛇形命名
     * @return 返回插入的记录idColumn字段的值
     */
    public <T> T insertGenID(String table_name, T mapOrPojo, String id_column) {
        if (null == id_column || "".equals(id_column)){
            id_column = "id";
        }

        Iterator it = new PropIterator(mapOrPojo).iterator();
        Class c = null;
        Field f = null;
        while(c==null && it.hasNext()){
            HashMap<String,Object> m = (HashMap<String,Object>)it.next();
            if(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, id_column).equals(m.get("propName"))){
                c = (Class)m.get("propType");
                f = (Field)m.get("field");
            }
        }
        if(c==null){
            throw new RuntimeException("pojo找不到id_column字段");
        }
        if(c!=int.class && c!=long.class && c!=Long.class && c!=Integer.class){
            throw new RuntimeException("自增字段只支持Integer（int）或Long（long）类型");
        }

        log.debug(new StringBuilder("往表'").append(table_name)
                .append("'插入一行，并自动生成'").append(id_column).append("'列的数据"));

        SimpleJdbcInsert insertTest = new SimpleJdbcInsert(dataSource)
                .withTableName(table_name)
                .usingGeneratedKeyColumns(id_column);//得到插入的返回值

        SqlParameterSource source = transToSource(mapOrPojo);
        Number id = insertTest.executeAndReturnKey(source);

        if(c==int.class || c==Integer.class){
            if (id.intValue() < 1) {
                throw new RuntimeException("insert保存数据出错！");
            }
            try {
                f.set(mapOrPojo,id.intValue());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("IllegalAccessException");
            }
            log.debug(new StringBuilder("插入成功，生成：").append(id.longValue()));
            return mapOrPojo;
        }
        if(c==long.class || c==Long.class){
            if (id.longValue() < 1) {
                throw new RuntimeException("insert保存数据出错！");
            }
            try {
                f.set(mapOrPojo,id.longValue());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("IllegalAccessException");
            }
            log.debug(new StringBuilder("插入成功，生成：").append(id.longValue()));
            return mapOrPojo;
        }

        throw new RuntimeException("jdbc X_X");
    }

    public int[] batchInsert(String table_name, List mapORpojo) {
        log.debug(new StringBuilder("往表'").append(table_name)
                .append("'批量插入").append(mapORpojo.size()).append("行"));

        SimpleJdbcInsert insertTest = new SimpleJdbcInsert(dataSource).withTableName(table_name);
        SqlParameterSource[] batchArgs = new SqlParameterSource[mapORpojo.size()];
        int i = 0;
        for (Object object : mapORpojo) {
            batchArgs[i] = transToSource(object);
            i++;
        }

        int[] its = insertTest.executeBatch(batchArgs);

        log.debug("批量插入成功");

        return its;
    }

    /**
     *
     * @param colonSql
     * @param mapORpojo
     * @return 成功：i=1，失败：i=0
     */
    public int update(String colonSql, Object mapORpojo) {
        log.debug(Query.readableSql(colonSql,mapORpojo));

        int i = this.named.update(colonSql, transToSource(mapORpojo));

        log.debug(new StringBuilder("更新成功：").append(i));

        return i;
    }
    public int update(Query q) {
        return update(q.getSqlStr(),q.getArgsMap());
    }

    public int delete(String colonSql, Object mapORpojo) {
        log.debug(Query.readableSql(colonSql,mapORpojo));

        int i = this.named.update(colonSql, transToSource(mapORpojo));

        log.debug(new StringBuilder("删除成功：").append(i));

        return i;
    }
    public int delete(Query q) {
        return delete(q.getSqlStr(),q.getArgsMap());
    }

//    public int[] batchUpdate(String colonSql, List mapORpojoList) {
//        log.debug(new StringBuilder("批量更新：").append(mapORpojoList.size()).append("行，")
//                .append(colonSql));
//
//        SqlParameterSource[] batchArgs = new SqlParameterSource[mapORpojoList.size()];
//        int i = 0;
//        for (Object mapORpojo : mapORpojoList) {
//            batchArgs[i] = transToSource(mapORpojo);
//            i++;
//        }
//        int[] its = this.named.batchUpdate(colonSql, batchArgs);
//
//        log.debug("批量更新成功");
//        return its;
//    }

//    public Map callProcedure(String procedureName, Object mapORpojo) {
//        log.debug(new StringBuilder("执行过程：").append(procedureName));
//
//        SimpleJdbcCall call = new SimpleJdbcCall(dataSource).withProcedureName(procedureName);
//        // 输入参数
//        SqlParameterSource source = transToSource(mapORpojo);
//        Map out = call.execute(source);
//
//        log.debug("过程执行成功");
//
//        return out;
//    }

    /**
     *
     * @param mapORpojo 要么是一个map，要么是一个pojo
     * @return
     */
    private SqlParameterSource transToSource(Object mapORpojo) {
        if(mapORpojo == null){
            return new MapSqlParameterSource();
        }

        SqlParameterSource source = null;
        if (mapORpojo instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) mapORpojo;
            source = new MapSqlParameterSource(map);
        }
        else {
            source = new BeanPropertySqlParameterSource(mapORpojo);
        }

        return source;
    }

    /**
     *
     * @param clazz	String||Integer||Float||Double||Object
     * @return
     */
    private Boolean isBaseType(Class clazz) {
        return clazz == String.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == Object.class;
    }

}
