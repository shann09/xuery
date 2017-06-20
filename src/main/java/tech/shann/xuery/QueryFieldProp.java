package tech.shann.xuery;

/**
 * 专用于update语句，QueryBuilder利用它生成类似
 * 		id =:id, account =:account , user_name=:userName 
 * 的语句
 * 
 * @author shann
 *
 * @param <T>
 */
public class QueryFieldProp<T> {
	private T pojo;
	private Class<T> beanClazz;
	
	private QueryFieldProp(){
		super();
	}

    //按照指定类型的属性来赋值，用于把model扩展的属性去除，属性回归entity
	public QueryFieldProp(T pojo, Class<T> beanClazz){
		super();
		this.pojo = pojo;
		this.beanClazz = beanClazz;
	}
	public QueryFieldProp(T pojo){
	    super();
	    this.beanClazz = null;
	    this.pojo = pojo;
    }

    public Class<T> getBeanClazz() {
        return beanClazz;
    }

    public void setBeanClazz(Class<T> beanClazz) {
        this.beanClazz = beanClazz;
    }

    public T getPojo() {
		return pojo;
	}
	public void setPojo(T pojo) {
		this.pojo = pojo;
	}



}
