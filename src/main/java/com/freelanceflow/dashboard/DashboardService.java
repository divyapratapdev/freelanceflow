package com.freelanceflow.dashboard;

import com.freelanceflow.client.ClientRepository;
import com.freelanceflow.common.CacheConstants;
import com.freelanceflow.common.enums.InvoiceStatus;
import com.freelanceflow.common.enums.ProjectStatus;
import com.freelanceflow.dashboard.dto.DashboardResponse;
import com.freelanceflow.dashboard.dto.MonthlyRevenue;
import com.freelanceflow.invoice.Invoice;
import com.freelanceflow.invoice.InvoiceRepository;
import com.freelanceflow.project.ProjectRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;

    public DashboardService(InvoiceRepository invoiceRepository,
                            ProjectRepository projectRepository,
                            ClientRepository clientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.DASHBOARD, key = "#userId")
    public DashboardResponse getDashboardSummary(Long userId) {
        DashboardResponse res = new DashboardResponse();
        
        BigDecimal totalRev = invoiceRepository.sumPaidByUserId(userId);
        res.setTotalRevenue(totalRev != null ? totalRev : BigDecimal.ZERO);
        
        BigDecimal outstanding = invoiceRepository.sumOutstandingByUserId(userId);
        res.setOutstandingAmount(outstanding != null ? outstanding : BigDecimal.ZERO);
        
        BigDecimal overdue = invoiceRepository.sumOverdueByUserId(userId);
        res.setOverdueAmount(overdue != null ? overdue : BigDecimal.ZERO);
        
        long activeProjects = projectRepository.findByUserIdAndStatus(userId, ProjectStatus.IN_PROGRESS, Pageable.unpaged()).getTotalElements();
        res.setActiveProjects(activeProjects);
        
        long totalClients = clientRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
        res.setTotalClients(totalClients);

        // Revenue Chart Calculation (Last 6 Months)
        List<MonthlyRevenue> chart = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<Invoice> paidInvoices = invoiceRepository.findByUserIdAndStatus(userId, InvoiceStatus.PAID, Pageable.unpaged()).getContent();
        
        Map<String, BigDecimal> monthlyTotals = new HashMap<>();
        DateTimeFormatter mmyy = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Initialize last 6 months with 0
        for (int i = 5; i >= 0; i--) {
            monthlyTotals.put(now.minusMonths(i).format(mmyy), BigDecimal.ZERO);
        }
        
        for (Invoice inv : paidInvoices) {
            if (inv.getUpdatedAt().isAfter(now.minusMonths(6).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())) {
                String monthKey = inv.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).format(mmyy);
                if (monthlyTotals.containsKey(monthKey)) {
                    monthlyTotals.put(monthKey, monthlyTotals.get(monthKey).add(inv.getTotal()));
                }
            }
        }
        
        for (int i = 5; i >= 0; i--) {
            String key = now.minusMonths(i).format(mmyy);
            chart.add(new MonthlyRevenue(key, monthlyTotals.get(key)));
        }
        
        res.setRevenueChart(chart);
        return res;
    }
}
