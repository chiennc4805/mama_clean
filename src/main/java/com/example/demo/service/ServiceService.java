package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.domain.Service;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public ResultPaginationDTO fetchAllService(Specification<Service> spec) {
        List<Service> services = this.serviceRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(services.size());

        res.setMeta(mt);
        res.setResult(services);

        return res;
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<Service> spec, Pageable pageable) {
        Page<Service> pageService = this.serviceRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageService.getNumber() + 1);
        mt.setPageSize(pageService.getSize());
        mt.setPages(pageService.getTotalPages());
        mt.setTotal(pageService.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageService.getContent());

        return res;
    }

    public Service fetchById(String id) {
        Optional<Service> serviceOptional = this.serviceRepository.findById(id);
        return serviceOptional.isPresent() ? serviceOptional.get() : null;
    }

    public Service create(Service service) {
        return this.serviceRepository.save(service);
    }

    public void delete(String id) {
        this.serviceRepository.deleteById(id);
    }

    public Service update(Service updatedService) {
        return this.serviceRepository.save(updatedService);
    }

}
