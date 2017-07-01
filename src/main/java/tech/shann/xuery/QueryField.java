package tech.shann.xuery;

import java.util.Date;

/**
 * 可用于select，update，等查询语句，QueryBuilder利用它生成条件语句，类似 user_name =:userName
 * 
 * @author shann
 *
 */
public class QueryField {

	private String fieldName;//t.name
	private String prefix;//t.
	private String columnName;//name
	private String symbol;
	private Object fieldValue;
	private SymbolType symbolType = SymbolType.DEFAULT;
	
	/**
	 * in ____ ni(not in) ____ ib(is blank) ____ nb(not blank) ____ def(= > >= < <= <> != like)
	 * @author shann
	 *
	 */
	public QueryField(String fieldName, String symbol, Object fieldValue) {
		super();
		if(fieldName==null||fieldName.length()<=0 || symbol==null||symbol.length()<=0){
			throw new RuntimeException("fieldName或者symbol不能为空");
		}
		//检验fieldName格式（不完全）
		if(fieldName.indexOf(".")==0
				|| (fieldName.indexOf(".")==fieldName.length()-1) ){
			throw new RuntimeException("fieldName格式错误");
		}
		
		this.fieldName = fieldName;
		if(fieldName.indexOf(".")>0){
			String[] splitedFieldName = fieldName.split("\\.");
			if(splitedFieldName.length>2){
				throw new RuntimeException("fieldName格式错误");
			}
			this.prefix = splitedFieldName[0];
			this.columnName = splitedFieldName[1];
		}else{
			this.prefix = "";
			this.columnName = fieldName;
		}

		if(fieldValue instanceof Date){
			this.fieldValue = new java.sql.Date(((Date)fieldValue).getTime());
		}else{
			this.fieldValue = fieldValue;
		}

		this.symbol = symbol;
		this.symbolType = SymbolType.getEnum(symbol);
	}
	
	public static enum SymbolType {
		IN("in"),// in ( )
		NOT_IN("ni"),// not in ( )
		IS_BLANK("ib"),// is blank 为空 ( is null or = '')
		NOT_BLANK("nb"),// not blank 不为空 ( is not null and <> '' )
		DEFAULT("def")// 默认 = > >= < <= <> != like
		;

		public String value;

		public String getValue() {
			return value;
		}

		SymbolType(String value) {
			this.value = value;
		}
		
		public static SymbolType getEnum(String value) {
			for (SymbolType v : values()){
				if (v.getValue().equals(value)){
					return v;
				}
			}
			return SymbolType.DEFAULT;
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Object getFieldValue() {
		return fieldValue;
	}
	
	public void setFieldValue(Object fieldValue) {
		this.fieldValue = fieldValue;
	}
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
		this.symbolType = SymbolType.getEnum(symbol);
	}

	public SymbolType getSymbolType() {
		return symbolType;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
}
