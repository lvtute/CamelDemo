package com.example.mybatis.mapper;

import com.example.mybatis.entity.User;

import java.util.List;

public interface UserMapper {

    //insert
    public void insert(User user);

    //according to id delete
    public void delete(Integer id);

    //according to user Of id modify
    public void update(User user);

    //according to id query
    public User getById(Integer id);

    //All queries
    public List<User> list();

}