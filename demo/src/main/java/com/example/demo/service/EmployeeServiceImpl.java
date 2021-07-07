package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repo.EmployeeMyBatisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    EmployeeMyBatisRepository employeeMyBatisRepository;

    @Override
    public int insert(Employee employee) {
        return employeeMyBatisRepository.insert(employee);
    }
}
