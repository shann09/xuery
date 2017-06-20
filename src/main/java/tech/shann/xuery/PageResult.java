package tech.shann.xuery;

import java.util.List;

public class PageResult<T> {
	private int totalRows;
	private List<T> rows;

	public PageResult(){
		super();
	}
	public PageResult(int totalRows, List<T> rows){
		super();
		this.totalRows = totalRows;
		this.rows = rows;
	}
	
	
	public int getTotalRows() {
		return totalRows;
	}
	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}
	public List<T> getRows() {
		return rows;
	}
	public void setRows(List<T> rows) {
		this.rows = rows;
	}
	
	
}
