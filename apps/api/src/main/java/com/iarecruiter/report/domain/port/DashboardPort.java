package com.iarecruiter.report.domain.port;

import com.iarecruiter.report.domain.model.DashboardKpis;
import java.util.UUID;

public interface DashboardPort {
    DashboardKpis getDashboardKpis(UUID companyId);
}
