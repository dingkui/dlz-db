package com.example.demo.controller;

import com.dlz.db.DB;
import com.dlz.db.model.Page;
import com.example.demo.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 用户管理 Controller，演示 DLZ-DB 核心用法：
 * 直接用 DB.xx 操作数据库，不需要 Mapper / DAO / Service 层。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return DB.pojo.selectWrapper(User.class)
                .eq(User::getId, id)
                .queryBean();
    }

    @GetMapping
    public List<User> list(@RequestParam(required = false) String name) {
        return DB.pojo.selectWrapper(User.class)
                .like(name != null && !name.isEmpty(), User::getName, name)
                .orderByDesc(User::getCreateTime)
                .queryBeanList();
    }

    @GetMapping("/page")
    public Page<User> page(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize) {
        return DB.pojo.selectWrapper(User.class)
                .orderByDesc(User::getCreateTime)
                .page(Page.build(pageNum, pageSize))
                .queryBeanPage();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        if (user.getDeleted() == null) {
            user.setDeleted(0);
        }
        DB.pojo.insert(user);
        return user;
    }

    @PutMapping("/{id}")
    public int update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return DB.pojo.updateWrapper(user)
                .eq(User::getId, id)
                .execute();
    }

    @DeleteMapping("/{id}")
    public int delete(@PathVariable Long id) {
        return DB.pojo.deleteWrapper(User.class)
                .eq(User::getId, id)
                .execute();
    }

    @GetMapping("/count")
    public long count() {
        return DB.pojo.selectWrapper(User.class).count();
    }
}
