package br.psi.giganet.api.purchase.dashboard.service;

import br.psi.giganet.api.purchase.common.reports.csv.services.CsvReportService;
import br.psi.giganet.api.purchase.common.reports.pdf.services.PdfReportService;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.dashboard.model.DashboardData;
import br.psi.giganet.api.purchase.dashboard.model.DashboardOrders;
import br.psi.giganet.api.purchase.dashboard.model.DashboardQuotations;
import br.psi.giganet.api.purchase.dashboard.repository.DashboardRepositoryImpl;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepositoryImpl dashboardRepository;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private CsvReportService csvReportService;

    public DashboardData getData(LocalDate initialDate, LocalDate finalDate) {
        DashboardData data = new DashboardData();
        data.setInitialDate(initialDate);
        data.setFinalDate(finalDate);

        data.setOrders(buildDashboardOrders(initialDate, finalDate));
        data.setQuotations(buildDashboardQuotations(initialDate, finalDate));

        return data;
    }

    private DashboardOrders buildDashboardOrders(LocalDate initialDate, LocalDate finalDate) {
        DashboardOrders ordersData = new DashboardOrders();

        List<Thread> threads = new ArrayList<>();

        threads.add(new Thread(() -> ordersData.setCount(
                dashboardRepository.countOrdersByStatusNotInAndCompetence(initialDate, finalDate, ProcessStatus.CANCELED))));

        threads.add(new Thread(() -> ordersData.setOrders(
                dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate,
                        ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                        ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))));

        threads.add(new Thread(() -> ordersData.setRealizedOrders(
                dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate,
                        ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))));

        threads.add(new Thread(() -> ordersData.setPendingOrders(
                dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate, ProcessStatus.PENDING))));

        threads.add(new Thread(() -> ordersData.setInTransitOrders(
                dashboardRepository.findAllPurchaseOrdersByCompetence(initialDate, finalDate, ProcessStatus.IN_TRANSIT))));

        threads.add(new Thread(() -> ordersData.setTotalsByCostCenter(
                dashboardRepository.totalOrdersByStatusGroupByCostCenterAndCompetence(initialDate, finalDate,
                        ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                        ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))));

        threads.add(new Thread(() -> ordersData.setTotalsGroupByDay(
                dashboardRepository.totalOrdersByStatusGroupByDays(initialDate, finalDate,
                        ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                        ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))));

        threads.add(new Thread(() -> ordersData.setTotalsPending(
                dashboardRepository.totalOrdersByStatusByCompetence(initialDate, finalDate, ProcessStatus.PENDING))));

        threads.add(new Thread(() -> ordersData.setTotalsInTransit(
                dashboardRepository.totalOrdersByStatusByCompetence(initialDate, finalDate, ProcessStatus.IN_TRANSIT))));

        threads.add(new Thread(() -> ordersData.setTotalsRealized(
                dashboardRepository.totalOrdersByStatusByCompetence(initialDate, finalDate,
                        ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED))));

        threads.add(new Thread(() -> ordersData.setMostPurchasedItems(
                dashboardRepository.findMostPurchasedItemsByCompetence(initialDate, finalDate))));

        threads.add(new Thread(() -> ordersData.setMostPurchasedSuppliers(
                dashboardRepository.findMostPurchasedSuppliersByCompetence(initialDate, finalDate))));

        threads.forEach(t -> {
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

        return ordersData;
    }

    private DashboardQuotations buildDashboardQuotations(LocalDate initialDate, LocalDate finalDate) {
        DashboardQuotations quotationsData = new DashboardQuotations();

        List<Thread> threads = new ArrayList<>();

        threads.add(new Thread(() -> quotationsData.setTotalsApproved(
                dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.APPROVED))));

        threads.add(new Thread(() -> quotationsData.setTotalsPending(
                dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.PENDING))));

        threads.add(new Thread(() -> quotationsData.setTotalsRealized(
                dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.REALIZED))));

        threads.add(new Thread(() -> quotationsData.setTotalsRejected(
                dashboardRepository.countAndTotalsQuotations(initialDate, finalDate, ProcessStatus.REJECTED))));

        threads.add(new Thread(() -> quotationsData.setTotalsByCostCenter(
                dashboardRepository.totalQuotationsByStatusGroupByCostCenter(initialDate, finalDate,
                        ProcessStatus.REALIZED, ProcessStatus.PENDING, ProcessStatus.APPROVED))));

        threads.add(new Thread(() -> quotationsData.setTotalsGroupByDay(
                dashboardRepository.totalQuotationsByStatusGroupByDays(initialDate, finalDate,
                        ProcessStatus.REALIZED, ProcessStatus.PENDING, ProcessStatus.APPROVED))));

//        threads.add(new Thread(() -> quotationsData.setMostQuotedItems(
//                dashboardRepository.findMostQuotedItems(initialDate, finalDate))));

        threads.forEach(t -> {
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

        return quotationsData;
    }

    public File getPurchaseOrdersListReportAsPdf(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) throws SQLException, IOException, JRException {
        return pdfReportService.getPurchaseOrdersListDashboardReport(initialDate, finalDate, statuses);
    }

    public File getPurchaseOrdersListReportAsCsv(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) throws Exception {
        List<Map<String, Object>> orders = dashboardRepository.findAllDetailedPurchaseOrdersByCompetences(initialDate, finalDate, statuses);
        return csvReportService.getPurchaseOrdersListReportAsCsv(orders, initialDate, finalDate, statuses);
    }

}
