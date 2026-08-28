package com.example.demo.controller;

import com.dlz.db.DB;
import com.dlz.db.model.Page;
import com.example.demo.entity.User;
import org.noear.solon.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 用户管理 Controller，演示 DLZ-DB 核心用法：
 * 直接用 DB.xx 操作数据库，不需要 Mapper / DAO / Service 层。
 */
@Controller
@Mapping("/user")
public class UserController {

    @Mapping("/{id}")
    @Get
    public User getById(@Path("id") Long id) {
        return DB.pojo.selectWrapper(User.class)
                .eq(User::getId, id)
                .queryBean();
    }

    @Mapping
    @Get
    public List<User> list(@Param(defaultValue = "") String name) {
        return DB.pojo.selectWrapper(User.class)
                .like(!name.isEmpty(), User::getName, name)
                .orderByDesc(User::getCreateTime)
                .queryBeanList();
    }

    @Mapping("/page")
    @Get
    public Page<User> page(@Param(defaultValue = "1") int pageNum,
                           @Param(defaultValue = "10") int pageSize) {
        return DB.pojo.selectWrapper(User.class)
                .orderByDesc(User::getCreateTime)
                .page(Page.build(pageNum, pageSize))
                .queryBeanPage();
    }

    @Mapping
    @Post
    public User create(@Body User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        if (user.getDeleted() == null) {
            user.setDeleted(0);
        }
        DB.pojo.insert(user);
        return user;
    }

    @Mapping("/{id}")
    @Put
    public int update(@Path("id") Long id, @Body User user) {
        user.setId(id);
        return DB.pojo.updateWrapper(user)
                .eq(User::getId, id)
                .execute();
    }

    @Mapping("/{id}")
    @Delete
    public int delete(@Path("id") Long id) {
        return DB.pojo.deleteWrapper(User.class)
                .eq(User::getId, id)
                .execute();
    }

    @Mapping("/count")
    @Get
    public long count() {
        return DB.pojo.selectWrapper(User.class).count();
    }
}
