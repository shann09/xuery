### 一个精简的java sql dsl，只要会sql，再看test中的示例，就能够灵活的增删改查

1. 不用注解，实体类是干干净净的pojo

2. 自动将查询出的蛇形命名转成pojo中的小驼峰命名


  public class JdbcTest {
  
      private JDBC jdbc = null;
  
      {
          DruidDataSource dataSource = new DruidDataSource();
          dataSource.setDriverClassName("com.mysql.jdbc.Driver");
  
          dataSource.setUrl("jdbc:mysql://localhost:3306/wo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT");
          dataSource.setUsername("root");
          dataSource.setPassword("root");
  
          dataSource.setInitialSize(50);
          dataSource.setMinIdle(30);
          dataSource.setMaxActive(600);
          dataSource.setTimeBetweenEvictionRunsMillis(1000 * 30);
          dataSource.setMinEvictableIdleTimeMillis(1000 * 30);
          dataSource.setValidationQuery("SELECT 'x'");
          dataSource.setTestWhileIdle(true);
          dataSource.setTestOnBorrow(false);
          dataSource.setTestOnReturn(false);
          dataSource.setRemoveAbandoned(true);
          dataSource.setRemoveAbandonedTimeout(1200);
          dataSource.setLogAbandoned(false);
  
          jdbc = new JDBC(dataSource);
  
      }
  
      @Test
      public void testJoin(){
          Query q = new QueryBuilder()
                  .push("select " +
                          "u.id,u.user_name," +
                          "u.mobile," +
                          "u.create_time," +
                          "uc.company_id," +
                          "c.company_name " +
                          "from sys_user u")
                  .push("left join user_company uc")
                  .push("on(u.id = uc.user_id)")
                  .push("left join company c")
                  .push("on(uc.company_id = c.id)")
                  .push("where")
                  .push(new QueryField("u.mobile","=","111222333"))
                  .push("order by u.user_name")
                  .build();
          System.out.println(q);
          List<UserModel> l = jdbc.queryForBeanList(
                  q,
                  UserModel.class
          );
          System.out.println(l.size());
          System.out.println(l.get(0).getUserName());
          System.out.println(l.get(0).getCompanyId());
          System.out.println(l.get(0).getCompanyName());
      }
  
      @Test
      public void testModelCastToEntity(){
          UserModel a = new UserModel();
          a.setId(1l);
          a.setUserName("周瑜");
          a.setCompanyId(22l);
  
          jdbc.update(
                  new QueryBuilder()
                          .push("update sys_user set")
                          .push(new QueryFieldProp(a,User.class))
                          .push("where")
                          .push(new QueryField("id","=",a.getId()))
                          .build());
  
      }
  
      @Test
      public void testStream(){
          Query q = new QueryBuilder()
                  .push("select * from sys_user")
                  .build();
  
          Map<String,User> am =
  //                new ArrayList<User>()
                  jdbc.queryForBeanList(q.getSqlStr(),q.getArgsMap(),User.class)
                  .stream()
                  .reduce(new HashMap<String, User>(),
                          new BiFunction<HashMap<String, User>, User, HashMap<String, User>>() {
                              @Override
                              public HashMap<String, User> apply(HashMap<String, User> m, User a) {
                                  m.put(a.getMobile(),a);
                                  return m;
                              }
                          },
                          new BinaryOperator<HashMap<String, User>>() {
                              @Override
                              public HashMap<String, User> apply(HashMap<String, User> m1, HashMap<String, User> m2) {
                                  m1.putAll(m2);
                                  return m1;
                              }
                          }
                  );
          System.out.println(am.size());
      }
      @Test
      public void testDate(){
          User u1 = new User();
          u1.setUserName("黄忠");
          u1.setMobile("88");
          u1.setCreateTime(new Date());
          jdbc.insertGenID("sys_user",u1,"id");
  
      }
  
      @Test
      public void testDuplicate(){
          User u1 = new User();
          u1.setUserName("诸葛亮1");
          u1.setMobile("66");
  
          User u2 = new User();
          u2.setUserName("诸葛亮2");
          u2.setMobile("66");
  
          try {
              jdbc.insertGenID("sys_user",u1,"id");
              jdbc.insertGenID("sys_user",u2,"id");
  
              System.out.println("yes");
          }catch (Exception e){
              Throwable cause = null;
              if(!((cause = e.getCause())!=null
                      && cause.getClass().getName().equals("com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException")
                      && cause.getMessage().startsWith("Duplicate entry"))){
                  throw e;
              }
              System.out.println("吃掉重复异常，其他异常重新抛出");
          }
      }
  
      @Test
      public void testDelete(){
          Query q = new QueryBuilder()
                  .push("delete from sys_user where")
                  .push(new QueryField("user_name", "=", "周瑜"))
                  .build();
          System.out.println(q.toString());
          int i = jdbc.delete(q.getSqlStr(),q.getArgsMap());
          //也能直接传query实例
  //        int i = jdbc.delete(q);
  
          System.out.println(i);
      }
      @Test
      public void testBatchInsert(){
          User u1 = new User();
          u1.setUserName("f");
          u1.setMobile("11");
  
          User u2 = new User();
          u2.setUserName("h");
          u2.setMobile("22");
  
          User u3 = new User();
          u3.setUserName("i");
          u3.setMobile("33");
  
          List<User> l = new ArrayList<User>(){{
              add(u1);
              add(u2);
              add(u3);
          }};
  
          //mobile是有唯一性索引字段
          //如果数据库中22存在，11不存在，33不存在
          // 那么抛出异常，但是11和33还是会插入到数据库中！
  
          jdbc.batchInsert("sys_user",l);
  
      }
  
      @Test
      public void testInsertGenID(){
          User u = new User();
          u.setUserName("e");
          u.setMobile("44");
  
          jdbc.insertGenID("sys_user",u,null);
          System.out.println(u.getId());
      }
      @Test
      public void testInsert(){
          User u = new User();
          u.setId(3l);
          u.setUserName("d");
          u.setMobile("33");
  
          jdbc.insert("sys_user",u);
          System.out.println(u.getId());
      }
  
      @Test
      public void testQueryForInt(){
          Query q = new QueryBuilder()
                  .push("select count(*) from sys_user")
                  .build();
          System.out.println(q.toString());
          int i = jdbc.queryForInt(q);
  
          System.out.println(i);
      }
      @Test
      public void testQueryForMap(){
          Query q = new QueryBuilder()
                  .push("select * from sys_user where")
                  .push(new QueryField("id","=",1l))
                  .build();
          System.out.println(q.toString());
          Map<String,Object> m = jdbc.queryForMap(q);
  
          System.out.println(m.get("mobile"));
          System.out.println(m.get("user_name"));//对的
          System.out.println(m.get("userName"));//错的
      }
      @Test
      public void testQueryForMapList(){
          Query q = new QueryBuilder()
                  .push("select * from sys_user")
                  .build();
          System.out.println(q.toString());
          List<Map<String,Object>> l = jdbc.queryForMapList(q);
          System.out.println(l.size());
          for(Map<String,Object> m:l){
              System.out.println(m.get("mobile"));
              System.out.println(m.get("user_name"));//对的
              System.out.println(m.get("userName"));//错的
  
          }
      }
  
      @Test
      public void testQueryForSingleValueList(){
          Query q = new QueryBuilder()
                  .push("select mobile from sys_user")
                  .build();
          System.out.println(q.toString());
          List<String> sl = jdbc.queryForSingleValueList(q,String.class);
          for(String s:sl){
              System.out.println(s);
          }
      }
  
      @Test
      public void testQueryForSingleValue(){
          Query q = new QueryBuilder()
                  .push("select user_name from sys_user where")
                  .push(new QueryField("id","=",1))
                  .build();
          System.out.println(q.toString());
          String s = jdbc.queryForSingleValue(q.getSqlStr(),q.getArgsMap(),String.class);
          System.out.println(s);
  
      }
  
      @Test
      public void testQueryForBeanList(){
          Query q = new QueryBuilder()
                  .push("select * from sys_user")
                  .push("where")
                  .push(new QueryField("id","=",1))
                  .push("and")
                  .push(new QueryField("user_name","in",new ArrayList<String>(){{
                      add("周瑜");
                      add("诸葛亮");
                      add("刘备");
                  }}))
                  .build();
          System.out.println(q.toString());
          List<User> l = jdbc.queryForBeanList(q,User.class);
          System.out.println(l.size());
  
      }
  
      @Test
      public void testQueryForBeanAndUpdate(){
          Query q = new QueryBuilder()
                  .push("select * from sys_user where")
                  .push(new QueryField("id","=",14))
                  .build();
          System.out.println(q.toString());
          User h = jdbc.queryForBean(q.getSqlStr(),q.getArgsMap(),User.class);
  
          h.setUserName("c");
          Query q1 = new QueryBuilder()
                  .push("update sys_user set ")
                  .push(new QueryFieldProp(h))
                  .push("where")
                  .push(new QueryField("id", "=", h.getId()))
                  .build();
          jdbc.update(q1);
  
      }
  
      @Test
      public void testRandomUpdate(){
          Query q = new QueryBuilder()
                  .push("update sys_user set")
                  .push(new QueryField("mobile", "=", "111222333"))
                  .push(",")
                  .push(new QueryField("user_name", "=", "刘备"))
                  .push("where")
                  .push(new QueryField("id","=",1))
                  .push("or")
                  .push(new QueryField("id","=",2))
                  .build();
          System.out.println(q.toString());
          int r = jdbc.update(q.getSqlStr(),q.getArgsMap());
          System.out.println(r);//返回被update的行数
          //如果更新字段违反了唯一性约束，那么任何一行都不会被更新
  
      }
  }
