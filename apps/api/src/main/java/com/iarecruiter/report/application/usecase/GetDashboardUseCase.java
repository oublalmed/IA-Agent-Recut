package com.iarecruiter.report.application.usecase;

import com.iarecruiter.report.domain.model.DashboardKpis;
import com.iarecruiter.report.domain.port.DashboardPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDashboardUseCase {

    private final DashboardPort dashboardPort;

    public DashboardKpis execute(UUID companyId) {
        return dashboardPort.getDashboardKpis(companyId);
    }
}
