package com.jqh.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jqh.mybatisplus.dao.UserMapper;
import com.jqh.mybatisplus.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void select(){
        List<User> users = userMapper.selectList(null);
        System.out.println(users.toString());
    }

    @Test
    public void insert(){
        User user = new User();
        user.setName("江强华11");
        user.setAge(31);
        user.setEmail("240437339@qq.com");
        user.setManagerId(1088248166370832385L);
        user.setCreateTime(LocalDateTime.now());
        int rows = userMapper.insert(user);
        System.out.println("insert rows = " + rows);
    }

    @Test
    public void SelectById(){
        User user = userMapper.selectById(1170369917443739649L);
        System.out.println(user);
    }

    @Test
    public void selectIds(){
        List<Long> list = Arrays.asList(1170369917443739649L,1170370995371425793L);
        List<User> users = userMapper.selectBatchIds(list);
        System.out.println(users);
    }

    @Test
    public void selectByMap(){
        Map<String, Object> columnMap = new HashMap<>(); // key保存的是数据库的列
        columnMap.put("name","江强华11");
        columnMap.put("age",31);
        List<User> userList = userMapper.selectByMap(columnMap);
        System.out.println(userList.toString());
        //SELECT id,name,age,email,manager_id,create_time FROM user WHERE name = ? AND age = ?
    }

    // 条件查询
    @Test
    public void selectByWrapper(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name","11").ge("age",30);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
        //DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? AND age > ?
        //DEBUG==> Parameters: %11%(String), 30(Integer)
    }

    //email不为空 age在29 到30 name包含 11的
    @Test
    public void selectByWrapper2(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // email不为空 age在29 到30 name包含 11的
        queryWrapper.like("name","11").between("age",29,40).isNotNull("email");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? AND age BETWEEN ? AND ? AND email IS NOT NULL
//        DEBUG==> Parameters: %11%(String), 29(Integer), 40(Integer)
    }

    //查找性江开头的，或者年龄大30，按照年龄降序排列，年龄相同按照id的升序排列
    @Test
    public void selectByWrapper3(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name","江").or().ge("age",30).orderByDesc("age").orderByAsc("id");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);

//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? OR age >= ? ORDER BY age DESC , id ASC
//        DEBUG==> Parameters: 江%(String), 30(Integer)
    }

    //条件构造器查询  数据库函数 创建日期为2019年2月14 并且 直属上级名字为王姓
    //date_format(create_time,'%Y-%m-%d') and  manager_id in (select id from t_user where name like '王%')
    @Test
    public void selectByWrapper4(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("date_format(create_time,'%Y-%m-%d') ={0}", "2019-02-14")  //这种方式不会sql注入, inSql是子查询
                //.apply("date_format(create_time,'%Y-%m-%d') ='2019-02-14'") //这样写有可能会引起sql注入 比如 '2019-02-14' or true or true,会查出全部记录
                .inSql(" manager_id", "select id from user where name like '王%'");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE date_format(create_time,'%Y-%m-%d') =? AND manager_id IN (select id from user where name like '王%')
//        DEBUG==> Parameters: 2019-02-14(String)
    }

    //名字性王，且（年龄小于40或邮箱不为空）
    //date_format(create_time,'%Y-%m-%d') and  manager_id in (select id from t_user where name like '王%')
    @Test
    public void selectByWrapper5(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name","王").and(wq->wq.lt("age",40).or().isNotNull("email"));
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);

//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? AND ( age < ? OR email IS NOT NULL )
//        DEBUG==> Parameters: 王%(String), 40(Integer)
    }

    @Test
    //条件构造器查询
    //name like ‘王%’ or (age<40 and age>20 and email is not null)
    public void selectByWrapper6(){
        QueryWrapper<User> queryWrapper= new QueryWrapper<User>();
        //QueryWrapper<User> query= Wrappers.query();
        //key为数据库的列名，不是实体中的属性名
        queryWrapper.likeRight("name","王")
                .or(wq->wq.lt("age",40).gt("age",20).isNotNull("email"));
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
//
//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ? OR ( age < ? AND age > ? AND email IS NOT NULL )
//        DEBUG==> Parameters: 王%(String), 40(Integer), 20(Integer)
    }

    @Test
    //条件构造器查询
    //  (age<40 or email is not null) and name like ‘王%’
    public void selectByWrapper7(){
        QueryWrapper<User> queryWrapper= new QueryWrapper<User>();
        //QueryWrapper<User> query= Wrappers.query();
        //key为数据库的列名，不是实体中的属性名
        queryWrapper.nested(wq->wq.lt("age",40).or().isNotNull("email"))
                .likeRight("name","王");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);

//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE ( age < ? OR email IS NOT NULL ) AND name LIKE ?
//                DEBUG==> Parameters: 40(Integer), 王%(String)
    }

    @Test
    //条件构造器查询
    // age in (30 ,31,34,40)
    public void selectByWrapper8(){
        QueryWrapper<User> queryWrapper= new QueryWrapper<User>();
        queryWrapper.in("age",Arrays.asList(31,31,34,35));//.last("limit 1");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);

//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE age IN (?,?,?,?)
//        DEBUG==> Parameters: 31(Integer), 31(Integer), 34(Integer), 35(Integer)
    }


    @Test
    //条件构造器查询 select不列出全部字段
    //name like ‘%雨%’ and age<40
    public void selectByWrapperSelect() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        //QueryWrapper<User> query= Wrappers.query();
        //select的列名如果和属性名不一致 可以这么写,但是没有查询的列映射到属性会变成null
//        queryWrapper.select("id userId","name realName","email").like("name","雨").lt("age",40);
        queryWrapper.select("id","name","email").like("name","雨").lt("age",40);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    @Test
    //条件构造器查询 select不列出全部字段
    public void selectByWrapperSelect2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        //QueryWrapper<User> query= Wrappers.query();
        //key为数据库的列名，不是实体中的属性名
        queryWrapper.select(User.class,tableFieldInfo -> !tableFieldInfo.getColumn().equals("create_time")&&
                !tableFieldInfo.getColumn().equals("manager_id"));//查的时候排除create_time和manager_id

        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    @Test
    // 模仿登录使用name或者emial登录，谁不为空，就使用条件
    public void selectByWrapperCondition() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        String name="王";
        String email="";

        queryWrapper.like(StringUtils.isNotEmpty(name),"name",name)
                .like(StringUtils.isNotEmpty(email),"email",email);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name LIKE ?
//                DEBUG==> Parameters: %王%(String)
    }

    @Test
    //条件构造器查询
    //name like ‘%雨%’ and age<40
    public void selectByWrapperEntity() {

        User whereUser =new User();
        whereUser.setName("刘红雨"); //但默认这样写是按照=来查询，如果想要like，可以在实体类属性的@TableField 中设置condition = SqlCondition.LIKE
        whereUser.setAge(32);

        QueryWrapper<User> queryWrapper = new QueryWrapper<User>(whereUser);
        //queryWrapper.like("name", "雨").lt("age", 40); //可以继续条件查询 不冲突
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);

//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE name=? AND age=?
//        DEBUG==> Parameters: 刘红雨(String), 32(Integer)
    }

    @Test //allEq的作用可以把参数map中 key对应的value为null进行处理  true  xx is null  false  去除 xx
    public void selectByWrapperAllEq() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        Map<String,Object> parms= new HashMap<String,Object>();
        parms.put("name","王天风");
        //parms.put("age",25);
        parms.put("age",null);//sql里就是age IS NULL

        queryWrapper.allEq(parms);
       // queryWrapper.allEq(parms, false);  // 传入false，忽略掉值为null的
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    @Test
    public void selectByWrapperAllEq3() {

        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        Map<String,Object> parms= new HashMap<String,Object>();
        parms.put("name","王天风");
        //parms.put("age",25);
        parms.put("age",null);
        queryWrapper.allEq((k,v)->!k.equals("name"),parms);// 满足条件的（true）才会添加到条件中
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);

//        DEBUG==>  Preparing: SELECT id,name,age,email,manager_id,create_time FROM user WHERE age IS NULL
    }

    // 查询返回map，只带上select的值
    @Test
    public void selectByWrapperMaps(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","name");
        List<Map<String,Object>> userList = userMapper.selectMaps(queryWrapper);
        userList.forEach(System.out::println);

        //DEBUG==>  Preparing: SELECT id,name FROM user
    }


}
