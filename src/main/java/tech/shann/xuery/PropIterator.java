package tech.shann.xuery;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 构造器传入一个pojo
 * next返回一个HashMap<String,Object>，该map含有如下2个key
 * 	  propName
 * 	  propValue
 * 如：
 * 	map.get("propName")
 * 
 * @author shann
 *
 */
public class PropIterator implements Iterable<Object> {
	
	private final Object targetObject;
	private Class clazz;

	public PropIterator(final Object targetObject) {
		this.targetObject = targetObject;
		this.clazz = null;
	}

	public PropIterator(final Object targetObject,Class clazz){
	    this.targetObject = targetObject;
	    this.clazz = clazz;
    }

	public Iterator<Object> iterator() {
		return new PropertyIteratorImpl();
	}

	private class PropertyIteratorImpl implements Iterator<Object> {
		int index;
		Field[] fields;

		PropertyIteratorImpl() {
			try {
			    if(clazz==null){
                    fields = targetObject.getClass().getDeclaredFields();
                }else {
                    fields = clazz.getDeclaredFields();
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean hasNext() {
			return this.index < this.fields.length;
		}

		/*@Override
		public Object next() {
			Object obj = null;
			try {
				Field field = fields[index++];
				field.setAccessible(true);
				obj = field.get(targetObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj;
		}*/
		
		@Override
		public HashMap<String,Object> next() {
			HashMap<String,Object> map = new HashMap<String,Object>();
			try {
				Field field = fields[index++];
				field.setAccessible(true);
				map.put("propName", field.getName());
				map.put("propValue", field.get(targetObject));
				map.put("propType", field.getType());
				map.put("field",field);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return map;
		}
		
		@Override
		public void remove() {
		}
	}
}
