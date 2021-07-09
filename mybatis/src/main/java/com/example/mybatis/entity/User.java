package com.example.mybatis.entity;


public class User {
    /**
     * name:Student entity
     */

//Primary key id
    private int id;
    //Full name
    private String name;
    //Age
    private int age;

    // Get and Set Method
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}