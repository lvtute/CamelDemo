package com.example.demo.controller;

import com.example.demo.mapper.UserMapper;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    //insert user
    @PostMapping
    public void insert(@RequestBody User user) {
        userMapper.insert(user);
    }

    //according to id delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        userMapper.delete(id);
    }

    //modify
    @PutMapping("/{id}")
    public void update(@RequestBody User user, @PathVariable("id") Integer id) {
        user.setId(id);
        userMapper.update(user);
    }

    //according to id Query students
    @GetMapping("/{id}")
    public User getById(@PathVariable("id") Integer id) {
        User user = userMapper.getById(id);
        return user;
    }

    //All queries
    @GetMapping
    public List<User> list() {
        List<User> users = userMapper.list();
        return users;
    }

}