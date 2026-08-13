package com.construction.material.service;

import com.construction.material.dto.request.CreateCompanyWithAdminRequest;
import com.construction.material.dto.response.CompanyResponse;

import java.util.List;

public interface CompanyService {
    /** Creates a Company, its default license and its first COMPANY_ADMIN user in one transaction. */
    CompanyResponse createCompanyWithAdmin(CreateCompanyWithAdminRequest request);

    List<CompanyResponse> findAll();

    CompanyResponse setActive(Long companyId, boolean active);
}
