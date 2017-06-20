package tech.shann.xuery;

/**
 * 行（一般给oracle使用） [起始行,结束行)
 * 1,2,3,4,5,    6,7,8,9,10,    11,12,13,14,15,     16
 * 0:5	[1,6)
 * 0:0	[1,1)
 * 1:5	[6,11)
 * 1:0	[6,6)
 * 2:5	[11,16)
 * 2:0	[11,11)
 * 
 * 下标（一般给mysql使用）
 * 0,1,2,3,4,    5,6,7,8,9,     10,11,12,13,14,     15
 * 0:5	[0,5)
 * 0:0	[0,0)
 * 1:5	[5,10)
 * 1:0	[5,5)
 * 2:5	[10,15)
 * 2:0	[10,10)
 * 
 * @author shann
 */
public class QueryPager {
	private int pagenum = 0;//第几页，从0开始，0表示第一页，默认第0页，不能小于0
	private int pagesize = 0;//每一页行数，默认每页0行，不能小于0
	
	public QueryPager(){
		super();
	}
	public QueryPager(int pagenum, int pagesize){
		super();
		this.setPagenum(pagenum);
		this.setPagesize(pagesize);
	}
	
	public int getBeginIndex(){//起始下标,从0开始，查询结果包括本行
		return pagenum*pagesize;
	}
	public int getEndIndex(){//结束下标，查询结果不包括本行
		if(this.pagesize==0){
			return this.getBeginIndex();
		}
		return (pagenum+1)*pagesize;
	}
	
	public int getBeginRow(){
		return this.getBeginIndex()+1;
	}
	public int getEndRow(){
		return this.getEndIndex()+1;
	}
	
	public int getPagenum() {
		return pagenum;
	}
	public void setPagenum(int pagenum) {
		if (pagenum<0) {
			this.pagenum = 0;
			return;
		}
		this.pagenum = pagenum;
	}
	public int getPagesize() {
		return pagesize;
	}
	public void setPagesize(int pagesize) {
		if (pagesize<0) {
			this.pagesize = 0;
			return;
		}
		this.pagesize = pagesize;
	}
}
