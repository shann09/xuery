package tech.shann.xuery;

import com.google.common.base.CaseFormat;

import java.util.*;

public class QueryBuilder {

	private List<Object> list = new ArrayList<Object>();

    private QueryPager queryPager = null;

    private String dbType = "mysql";//配合queryPager，确定如何输出分页语句，默认使用mysql

	public QueryBuilder push(QueryField queryField){
        return pushIf(true,queryField);
	}

	public QueryBuilder pushIf(boolean doPush,QueryField queryField){
        if (doPush){
            list.add(queryField);
        }
		return this;
	}

	public QueryBuilder push(String linker){
		return pushIf(true,linker);
	}

	public QueryBuilder pushIf(boolean doPush,String linker){
        if (doPush){
            list.add(linker);
        }
		return this;
	}

	public QueryBuilder push(QueryFieldProp queryFieldProp){
		return pushIf(true, queryFieldProp);
	}

	public QueryBuilder pushIf(boolean doPush,QueryFieldProp queryFieldProp){
        if (doPush){
            list.add(queryFieldProp);
        }
		return this;
	}

    public QueryBuilder popIf(boolean doPop){
        if (doPop){
            popIf(true, 1);
        }
        return this;
    }

    public QueryBuilder popIf(boolean doPop,int popCount){
        if(popCount<=0){
            return this;
        }
        if (popCount>=list.size()){
            list.clear();
            return this;
        }
		if(doPop){
            for (int i=0;i<popCount;i++) {
                list.remove(list.size());
            }
		}
		return this;
	}

	/**
	 * 该方法默认自动帮你在语句中插入分页的条件，如果不适用，请自己使用push(String linker)拼接：
	 * 		1，mysql在语句最末尾加入类似limit 1,2
	 * 		2，oralce在语句在：
	 * 			a，在最前面加入" select * from ( "，
	 * 			b，在第一个FROM/from前插入" ,rownum rn "，
	 * 			c，在第一个WHERE/where后插入" rownum<13 and "，如果没有WHERE，就在末尾插入" where rownum<13 "
	 * 			d，有FROM就使用FROM而弃用from，WHERE同理，可以混合FROM和from来指定要替换的位置
	 * 			e，在语句的最末尾加入" ) where rn>10 "。
	 * 			f，生成类似如下语句
	 * 				select * from (
	 * 					select c.*,rownum rn from cost c
	 * 					where rownum<13
	 * 				) where rn>10
	 * @param queryPager
	 * @return
	 */
	public QueryBuilder setPager(QueryPager queryPager){
		this.queryPager = queryPager;
		return this;
	}
	
	public QueryBuilder setDbType(String dbType){
		if(dbType.toLowerCase().equals("oracle")){
			this.dbType = "oracle";
		}
		return this;
	}

	public Query build(){
	    Builder b = new Builder();

	    Query q = new Query();
	    q.setSqlStr(b.toSqlStr());
	    q.setArgsMap(b.map);

	    return q;
    }

    private class Builder{

        private HashMap<String,Object> map = new HashMap<String,Object>();

        private HashMap<String,Integer> keyUsageMap = new HashMap<String,Integer>();

        private String toSqlStr() {
            StringBuilder condSql = new StringBuilder();
            for(Object obj:list){
                if(obj instanceof QueryField){
                    QueryField queryField = (QueryField)obj;
                    condSql.append(this.fieldToString(queryField));
                }else if (obj instanceof String){
                    condSql.append(" ").append(obj).append(" ");
                }else if(obj instanceof QueryFieldProp) {
                    QueryFieldProp queryFieldProp = (QueryFieldProp)obj;
                    condSql.append(this.fieldPropToString(queryFieldProp));
                }
            }
            if("mysql".equals(dbType)){
                condSql = this.injectMySqlPager(condSql);
            }else{
                condSql = this.injectOraclePager(condSql);
            }
            return condSql.toString();
        }

        private String fieldPropToString(QueryFieldProp queryFieldProp) {

            Iterator it = new PropIterator(queryFieldProp.getPojo(),queryFieldProp.getBeanClazz()).iterator();

            StringBuilder sProperty = new StringBuilder(" ");
            while(it.hasNext()){
                HashMap<String,Object> map = (HashMap<String,Object>)it.next();
                String propName = (String)map.get("propName");
                Object propValue = map.get("propValue");

                QueryField queryField = new QueryField(
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, propName),
                        "=",
                        propValue
                );
                sProperty.append(this.fieldToString(queryField)).append(" ");
                if(it.hasNext()){
                    sProperty.append(" , ");
                }
            }
            return sProperty.toString();
        }

        private StringBuilder injectMySqlPager(StringBuilder condSql) {
            if(queryPager == null){
                return condSql;
            }
            condSql.append(" limit ")
                    .append(queryPager.getBeginIndex())
                    .append(" , ")
                    .append(queryPager.getPagesize())
                    .append(" ");
            return condSql;
        }

        private StringBuilder injectOraclePager(StringBuilder condSql){
            if(queryPager == null){
                return condSql;
            }

            //添加oracle特有的rownum
            String rawSql = condSql.toString();
            if(!(rawSql.indexOf(" FROM ")!=-1)){//没有全大写的FROM标记，就把所有from变成小写，再把第一个from标记为FROM
                rawSql = rawSql
                        .replaceAll(" (?i)from ", " from ")
                        .replaceFirst(" from ", " FROM ");
            }
            if(!(rawSql.indexOf(" WHERE ")!=-1)){//WHERE同上
                rawSql = rawSql
                        .replaceAll(" (?i)where ", " where ")
                        .replaceFirst(" where ", " WHERE ");
            }

            rawSql = rawSql.replaceFirst(" FROM ", " ,rownum rn from ");

            if(rawSql.indexOf(" WHERE ")!=-1){
                rawSql = rawSql.replaceFirst(" WHERE ",
                        new StringBuilder(" where rownum< ")
                                .append(queryPager.getEndRow())
                                .append(" and ")
                                .toString()
                );
            }else{
                rawSql = new StringBuilder(rawSql)
                        .append(" where rownum< ")
                        .append(queryPager.getEndRow())
                        .append(" ")
                        .toString();
            }

            condSql = new StringBuilder(" select * from ( ")
                    .append(rawSql)
                    .append(" ) where rn>= ")
                    .append(queryPager.getBeginRow())
                    .append(" ");

            return condSql;
        }

        private String fieldToString(QueryField queryField){
            StringBuilder sField = new StringBuilder();
            String fieldName = queryField.getFieldName();
            //下划线转驼峰命名，作为key，适配pojo参数
            String columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,
                    queryField.getColumnName());

            Integer count = null;
            Iterator it = null;
            switch (queryField.getSymbolType()) {
                case IN:
                    Collection inValList = (Collection)queryField.getFieldValue();
                    if(inValList==null||inValList.size()==0){
                        return " 1=0 ";
                    }
                    sField.append(" ").append(fieldName).append(" ");

                    sField.append(" in ( ");

                    count = keyUsageMap.containsKey(columnName) ? keyUsageMap.get(columnName) : 0 ;

                    it = inValList.iterator();
                    while(it.hasNext()){
                        Object val = it.next();
                        StringBuilder key = new StringBuilder(columnName);
                        if(count!=0){
                            key.append("_").append(count);
                        }
                        count++;
                        sField.append(":").append(key).append(" ");
                        map.put(key.toString(), val);
                        if(!it.hasNext()){
                            break;
                        }
                        sField.append(" , ");
                    }
                    sField.append(" ) ");
                    keyUsageMap.put(columnName, count);
                    return sField.toString();
                case NOT_IN:
                    Collection notinValList = (Collection)queryField.getFieldValue();
                    if(notinValList==null||notinValList.size()==0){
                        return " 1=0 ";
                    }
                    sField.append(" ").append(fieldName).append(" ");

                    sField.append(" not in ( ");

                    count = keyUsageMap.containsKey(columnName) ? keyUsageMap.get(columnName) : 0 ;

                    it = notinValList.iterator();
                    while(it.hasNext()){
                        Object val = it.next();
                        StringBuilder key = new StringBuilder(columnName);
                        if(count!=0){
                            key.append("_").append(count);
                        }
                        count++;
                        sField.append(":").append(key).append(" ");
                        map.put(key.toString(), val);
                        if(!it.hasNext()){
                            break;
                        }
                        sField.append(" , ");
                    }
                    sField.append(" ) ");
                    keyUsageMap.put(columnName, count);
                    return sField.toString();
                case NOT_BLANK:
                    return sField.append(" ( ")
                            .append(fieldName).append(" is not null and ")
                            .append(fieldName).append(" <> '' ) ")
                            .toString();
                case IS_BLANK:
                    return sField.append(" ( ")
                            .append(fieldName).append(" is null or ")
                            .append(fieldName).append(" = '' ) ")
                            .toString();
                case DEFAULT:
                    count = keyUsageMap.containsKey(columnName) ? keyUsageMap.get(columnName) : 0 ;

                    StringBuilder key =new StringBuilder(columnName);
                    if(count!=0){
                        key.append("_").append(count);
                    }
                    count++;

                    map.put(key.toString(), queryField.getFieldValue());
                    keyUsageMap.put(columnName, count);

                    return sField.append(" ").append(fieldName).append(" ")
                            .append(queryField.getSymbol()).append(" :").append(key).append(" ").toString();
                default:
                    return " 1=0 ";
            }
        }

	}


}
