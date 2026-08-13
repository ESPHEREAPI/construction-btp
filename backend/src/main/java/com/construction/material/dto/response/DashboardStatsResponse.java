package com.construction.material.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private Long totalProjects;
    private Long activeProjects;
    private Long totalMaterials;
    private BigDecimal totalConsumption;
    private BigDecimal totalBudget;
    private Integer alertsCount;
    private List<ProjectStats> projectStats;
    private List<MaterialUsageStats> materialUsageStats;
    
    @Data
    @Builder
    public static class ProjectStats {
        private String projectName;
        private BigDecimal budgetUsed;
        private Integer materialsCount;
    }
    
    @Data
    @Builder
    public static class MaterialUsageStats {
        private String materialName;
        private BigDecimal quantityUsed;
        private String unit;
    }
}
