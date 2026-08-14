package com.construction.material.service;

import com.construction.material.dto.request.UsageRequest;
import com.construction.material.dto.response.UsageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsageService {

    UsageResponse create(UsageRequest request);

    UsageResponse update(Long id, UsageRequest request);

    UsageResponse findById(Long id);

    Page<UsageResponse> findAllPaginated(Long projectId, Long materialId, Pageable pageable);

    void delete(Long id);
}
