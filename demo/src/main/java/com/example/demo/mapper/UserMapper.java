package com.example.demo.mapper;

import com.example.demo.entity.User;

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