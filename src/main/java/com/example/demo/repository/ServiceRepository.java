package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String>, JpaSpecificationExecutor<Service> {

}
