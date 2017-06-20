package tech.shann.xuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by shann on 17/6/15.
 */
public class Query {
    private String sqlStr;
    private HashMap<String,Object> argsMap;

    @Override
    public String toString() {
        return readableSql(this.sqlStr,this.argsMap);
    }

    public static String readableSql(String colonSql, Object mapORpojo) {
        if(mapORpojo == null){
            return colonSql;
        }

        colonSql = new StringBuilder(colonSql).append(" ").toString();

        if(mapORpojo instanceof Map){
            Map<String, ?> map = (Map<String,?>)mapORpojo;
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                Object obj = entry.getValue();
                colonSql = colonSql.replace(
                        new StringBuilder(":").append(entry.getKey()).append(" "),
                        new StringBuilder("'").append(entry.getValue()).append("' ")
                );
            }
            return colonSql;
        }

//        Iterator it = new PropIterator(pojo).iterator();
        Iterator it = null;
        while(it.hasNext()){
            HashMap<String,Object> map = (HashMap<String,Object>)it.next();
            colonSql = colonSql.replace(
                    new StringBuilder(":").append(map.get("propName")).append(" "),
                    new StringBuilder("'").append(map.get("propValue")).append("' ")
            );
        }
        return colonSql;
    }

    public String getSqlStr() {
        return sqlStr;
    }

    public void setSqlStr(String sqlStr) {
        this.sqlStr = sqlStr;
    }

    public HashMap<String, Object> getArgsMap() {
        return argsMap;
    }

    public void setArgsMap(HashMap<String, Object> argsMap) {
        this.argsMap = argsMap;
    }
}
