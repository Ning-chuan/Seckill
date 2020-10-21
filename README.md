# Seckill
## Spring、SpringMVC、Mybatis项目实战-秒杀项目

### 环境

- 技术栈：mysql、redis、springmvc、spring、mybatis
- 开发工具：idea

### 秒杀核心业务

在进行秒杀抢购时，处理用户高并发访问数据的请求。

### 前端页面

1. 链接动态化
2. 抢购按钮在规定时间内只能点击一次
3. 页面静态化

### 后端业务分析

1. 恶意请求
   - 同一个账号，对同一商品的秒杀请求，只接收一次，其他的过滤掉
2. 超卖
3. 数据库性能瓶颈
   - 利用redis解决
4. 验证码
   - 秒杀订单携带md5验证
5. 统一时间
   - 前端需要获取的时间都应该以服务器为准。即在需要获取时间时，发送请求从服务器获取。

### 搭建SSM框架

1. 添加pom文件依赖

   ```xml
   <dependencies>
   
       <!--        单元测试-->
       <dependency>
           <groupId>junit</groupId>
           <artifactId>junit</artifactId>
           <version>4.13</version>
           <scope>test</scope>
       </dependency>
   
       <!--        日志-->
       <dependency>
           <groupId>ch.qos.logback</groupId>
           <artifactId>logback-core</artifactId>
           <version>1.2.3</version>
       </dependency>
       <dependency>
           <groupId>ch.qos.logback</groupId>
           <artifactId>logback-classic</artifactId>
           <version>1.2.3</version>
       </dependency>
   
       <!--        数据库-->
       <dependency>
           <groupId>mysql</groupId>
           <artifactId>mysql-connector-java</artifactId>
           <version>5.1.47</version>
       </dependency>
   
       <!--        连接池-->
       <dependency>
           <groupId>com.alibaba</groupId>
           <artifactId>druid</artifactId>
           <version>1.1.22</version>
       </dependency>
   
       <!--        mybatis-->
       <dependency>
           <groupId>org.mybatis</groupId>
           <artifactId>mybatis</artifactId>
           <version>3.5.4</version>
       </dependency>
       <dependency>
           <groupId>org.mybatis</groupId>
           <artifactId>mybatis-spring</artifactId>
           <version>1.3.2</version>
       </dependency>
   
       <!--        servlet -->
       <dependency>
           <groupId>taglibs</groupId>
           <artifactId>standard</artifactId>
           <version>1.1.2</version>
       </dependency>
       <dependency>
           <groupId>jstl</groupId>
           <artifactId>jstl</artifactId>
           <version>1.2</version>
       </dependency>
   
       <dependency>
           <groupId>javax.servlet</groupId>
           <artifactId>javax.servlet-api</artifactId>
           <version>3.1.0</version>
       </dependency>
   
       <!--        spring 依赖-->
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-core</artifactId>
           <version>5.2.6.RELEASE</version>
       </dependency>
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-beans</artifactId>
           <version>5.2.6.RELEASE</version>
       </dependency>
   
       <!--        dao层依赖-->
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-jdbc</artifactId>
           <version>5.2.6.RELEASE</version>
       </dependency>
   
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-tx</artifactId>
           <version>5.2.6.RELEASE</version>
       </dependency>
   
       <!--        springmvc-->
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-web</artifactId>
           <version>5.2.6.RELEASE</version>
       </dependency>
   
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-webmvc</artifactId>
           <version>5.2.5.RELEASE</version>
       </dependency>
   
   
       <!--        spring test -->
       <dependency>
           <groupId>org.springframework</groupId>
           <artifactId>spring-test</artifactId>
           <version>5.2.6.RELEASE</version>
       </dependency>
   
   
       <!--        redis-->
       <dependency>
           <groupId>redis.clients</groupId>
           <artifactId>jedis</artifactId>
           <version>2.9.3</version>
       </dependency>
   
       <dependency>
           <groupId>org.springframework.data</groupId>
           <artifactId>spring-data-redis</artifactId>
           <version>2.1.10.RELEASE</version>
       </dependency>
   
   </dependencies>
   ```

2. 配置文件

   - spring
   - logback日志
   - mybatis
   - redis

### 数据库

> MySQL中创建数据库：Seckill

- 秒杀商品表

  ```mysql
  # 建表语句
  CREATE TABLE `seckill_item` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(45) DEFAULT NULL,
    `number` int DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `price` decimal(10,2) DEFAULT NULL,
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
  
  # 测试数据
  INSERT INTO `seckill_item` VALUES (1,'《MySQL从入门到跑路》',99,'2020-10-20 23:28:30','2020-10-21 20:00:00','2020-10-17 09:56:10',99.00),(2,'《Linux从海王到单身》',100,'2020-10-18 18:01:30','2020-10-18 18:02:30','2020-10-17 09:57:32',89.00),(3,'《运动解剖学》',100,'2020-10-18 18:10:30','2020-10-18 18:11:00','2020-10-17 09:59:22',99.00);
  ```

- 秒杀订单表

  ```mysql
  # 建表语句
  CREATE TABLE `seckill_order` (
    `id` int NOT NULL AUTO_INCREMENT,
    `order_code` varchar(45) DEFAULT NULL,
    `seckill_item_id` int DEFAULT NULL,
    `user_id` int DEFAULT NULL,
    `state` tinyint NOT NULL DEFAULT '-1',
    `create_time` datetime DEFAULT NULL,
    `order_timeout` int DEFAULT '300',
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
  ```

- 用户表

  ```mysql
  # 建表语句
  CREATE TABLE `user` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(45) DEFAULT NULL,
    `password` varchar(45) DEFAULT NULL,
    `phone` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
  
  # 测试数据
  INSERT INTO `user` VALUES (1,'张三','333','12345678'),(2,'于自言','e10adc3949ba59abbe56e057f20f883e','13112341234');
  ```

### Redis环境搭建

==linux和windows任选一种就行==

#### 1. 使用linux安装redis（使用windows版的可以跳过）

> linux centos7.3系统
>
> vm虚拟机、
>
> iso镜像地址：http://vault.centos.org/7.3.1611/isos/x86_64/
>
> 安装系统之后，需要配置网络环境，联网的方式选择NAT方式
>
> 安装linux系统参考：https://blog.csdn.net/babyxue/article/details/80970526
>
> 官网5.0.8下载地址：http://download.redis.io/releases/redis-5.0.8.tar.gz?_ga=2.138488051.1943660982.1595424341-357589577.1576159742

1. 网络环境

   - 修改一个IP地址、网关地址、DNS地址、本地虚拟网卡的地址xxx.xxx.xxx.2
     主 WINS 服务器  . . . . . . . . . : 192.168.57.2

2. xshell工具连接

3. 安装redis（linux）

   > 参考：https://www.runoob.com/redis/redis-install.html

   ```shell
   老版本redis，wget=linux下载命令
   wget http://download.redis.io/releases/redis-2.8.17.tar.gz
   解压缩
   tar xzf redis-2.8.17.tar.gz
   进入文件夹
   cd redis-2.8.17
   编译
   make
   ```


#### 2. window搭建redis环境

   - 3.xx版本：百度网盘下载地址 https://pan.baidu.com/s/1z1_OdNVbtgyEjiktqgB83g 密码：kdfq
   - 5.0.9版本：https://github.com/tporadowski/redis/releases

#### 3. 修改配置文件 redis.conf

==Windows和redis都需要修改这个配置文件==

   - bind 127.0.0.1 注释掉

   - protected-mode no 设置no

   - daemonize yes 这是redis服务是后台进程yes

#### 4. java使用redis（SSM框架结构

   ```java
// 默认连接host是本机，post端口是6379
// 如果不是需要自定参数
//        JedisPool jedisPool = new JedisPool();
static JedisPool jedisPool = new JedisPool("192.168.1.213", 6379);

static Jedis jedis = jedisPool.getResource();

public static void kvString() {
    // key - value 字符串
    jedis.set("duyi", "hello");

    String value = jedis.get("duyi");
    System.out.println(value);
}
   ```

### springmvc

- bootstrap环境
- restful风格
- 静态资源处理（配置文件）

### 使用redis脚本lua

java中执行的lua扣库存的脚本

```java
// 初始化减库存lua脚本
// -1 库存不足
// -2 不存在
// 整数是正常操作，减库存成功
StringBuilder sb = new StringBuilder();
sb.append("if (redis.call('exists', KEYS[1]) == 1) then");
sb.append("    local stock = tonumber(redis.call('get', KEYS[1]));");
sb.append("    if (stock == -1) then");
sb.append("        return -1");
sb.append("    end;");
sb.append("    if (stock > 0) then");
sb.append("        redis.call('incrby', KEYS[1], -1);");
sb.append("        return stock - 1;");
sb.append("    end;");
sb.append("    return -1;");
sb.append("end;");
sb.append("return -2;");
```

### 秒杀按钮点击逻辑

1、验证请求的URL是否正确

2、是否已经登录

3、限制每一个用户只可以发送一次请求（5分钟内）

4、redis减库存（高并发在这一步被redis扛下，过滤掉大量没有抢购成功的用户）

- lua脚本保证原子性
- 库存大于0可以下单
- 库存小于0直接返回，秒杀失败

5、mysql的商品表减库存（在一个事物中）

6、mysql的订单表生成记录（在一个事物中）

7、支付

- 查询数据库中该订单状态
- 修改状态