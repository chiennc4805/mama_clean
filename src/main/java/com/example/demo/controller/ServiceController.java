package com.example.demo.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Service;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.ServiceService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping("/services")
    public ResponseEntity<Service> createService(@Valid @RequestBody Service reqService) throws IdInvalidException {
        Service newService = this.serviceService.create(reqService);
        return ResponseEntity.status(HttpStatus.CREATED).body(newService);
    }

    @GetMapping("/services")
    public ResponseEntity<ResultPaginationDTO> fetchAllServices(
            @Filter Specification<Service> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.serviceService.fetchAllService());
        } else {
            Pageable pageable = PageRequest.of(page - 1, size);
            return ResponseEntity.ok(this.serviceService.fetchAllWithPagination(spec, pageable));
        }
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<Service> fetchServiceById(@PathVariable("id") String id) throws IdInvalidException {
        Service service = this.serviceService.fetchById(id);
        if (service == null) {
            throw new IdInvalidException("Service with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(service);
    }

    @PutMapping("/services")
    public ResponseEntity<Service> updateService(@Valid @RequestBody Service reqService) throws IdInvalidException {
        Service service = this.serviceService.fetchById(reqService.getId());
        if (service == null) {
            throw new IdInvalidException("Service with id = " + reqService.getId() + " không tồn tại");
        }
        Service updatedService = this.serviceService.update(reqService);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable("id") String id) throws IdInvalidException {
        Service serviceDB = this.serviceService.fetchById(id);
        if (serviceDB == null) {
            throw new IdInvalidException("Service with id = " + id + " không tồn tại");
        }
        this.serviceService.delete(id);
        return ResponseEntity.ok(null);
    }

}
