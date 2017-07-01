DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  user_name varchar(20) DEFAULT NULL COMMENT '姓名',
  mobile varchar(20) UNIQUE DEFAULT NULL COMMENT '电话',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户';

DROP TABLE IF EXISTS company;
CREATE TABLE company (
  id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  company_name varchar(20) DEFAULT NULL COMMENT '公司名',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公司';

DROP TABLE IF EXISTS user_company;
CREATE TABLE user_company (
  id bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  user_id bigint NOT NULL COMMENT '用户id',
  company_id bigint NOT NULL COMMENT '公司id',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户公司关联表';

INSERT INTO sys_user (id,user_name,mobile,create_time) VALUES (1,'刘备','111222333','2016-06-12');
INSERT INTO company (id,company_name) VALUES (1,'真三国公司');
INSERT INTO user_company (id,user_id,company_id) VALUES (1,1,1);
