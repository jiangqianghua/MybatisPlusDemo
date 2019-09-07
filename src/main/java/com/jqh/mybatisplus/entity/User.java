package com.jqh.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user") // 可以指定表名，如果表名和类名一直，可以省略
public class User {
    //主键
    @TableId  // 指定主键，如果名称是id，可以省略
    private Long id;
    // 姓名
    @TableField("name")  // 指定表列的名称
    private String name;
    // 年龄
    private Integer age;
    // 邮箱
    private String email;
    // 直属上级， 对应表列名是manager_id
    private Long managerId;
    // 创建时间
    private LocalDateTime createTime;

    // 以下 属性不参与 表 ,使用transient
    //private transient String remark;

    @TableField(exist = false)  // 使用这个注解也是不参与表
    private String remark;


}
