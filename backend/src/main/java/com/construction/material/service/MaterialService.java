package com.construction.material.service;

import com.construction.material.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MaterialService {
    List<Material> findAll();
    Page<Material> findAll(Pageable pageable);
    Material findById(Long id);
    Material save(Material material);
    Material update(Long id, Material material);
    void deleteById(Long id);
    List<Material> findByCategory(String category);
    List<Material> findActive();
    List<String> findCategories();
    List<String> findSuppliers();

    /** Hides a shared catalog material from the current company's own view only. */
    void hide(Long id);
    void unhide(Long id);
    List<Material> findHidden();
}
