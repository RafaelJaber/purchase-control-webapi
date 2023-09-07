package br.psi.giganet.api.purchase.dashboard.controller;

import br.psi.giganet.api.purchase.common.utils.controller.DownloadFileControllerUtil;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.dashboard.model.DashboardData;
import br.psi.giganet.api.purchase.dashboard.service.DashboardService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardData> getData(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate) {
        return ResponseEntity.ok(dashboardService.getData(initialDate, finalDate));
    }

    @GetMapping(path = "/downloads/orders", params = {"initialDate", "finalDate", "statuses"})
    public void getAllPurchaseOrdersListReportByStatusAsPdf(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            @RequestParam ProcessStatus[] statuses,
            HttpServletResponse response)
            throws ResourceNotFoundException, IOException, SQLException, JRException {
        File file = this.dashboardService.getPurchaseOrdersListReportAsPdf(
                initialDate,
                finalDate,
                statuses);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/downloads/totals-orders", params = {"pdf", "initialDate", "finalDate"})
    public void getAllPurchaseOrdersListReportAsPdf(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            HttpServletResponse response)
            throws ResourceNotFoundException, IOException, SQLException, JRException {
        File file = this.dashboardService.getPurchaseOrdersListReportAsPdf(
                initialDate,
                finalDate,
                ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/downloads/in-transit-orders", params = {"pdf", "initialDate", "finalDate"})
    public void getInTransitPurchaseOrdersListReportAsPdf(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            HttpServletResponse response)
            throws ResourceNotFoundException, IOException, SQLException, JRException {
        File file = this.dashboardService.getPurchaseOrdersListReportAsPdf(
                initialDate,
                finalDate,
                ProcessStatus.IN_TRANSIT);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/downloads/realized-orders", params = {"pdf", "initialDate", "finalDate"})
    public void getRealizedPurchaseOrdersListReportAsPdf(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            HttpServletResponse response)
            throws ResourceNotFoundException, IOException, SQLException, JRException {
        File file = this.dashboardService.getPurchaseOrdersListReportAsPdf(
                initialDate,
                finalDate,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/downloads/totals-orders", params = {"csv", "initialDate", "finalDate"})
    public void getAllPurchaseOrdersListReportAsCsv(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            HttpServletResponse response)
            throws Exception {
        File file = this.dashboardService.getPurchaseOrdersListReportAsCsv(
                initialDate,
                finalDate,
                ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/downloads/in-transit-orders", params = {"csv", "initialDate", "finalDate"})
    public void getInTransitPurchaseOrdersListReportAsCsv(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            HttpServletResponse response)
            throws Exception {
        File file = this.dashboardService.getPurchaseOrdersListReportAsCsv(
                initialDate,
                finalDate,
                ProcessStatus.IN_TRANSIT);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/downloads/realized-orders", params = {"csv", "initialDate", "finalDate"})
    public void getRealizedPurchaseOrdersListReportAsCsv(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            HttpServletResponse response)
            throws Exception {
        File file = this.dashboardService.getPurchaseOrdersListReportAsCsv(
                initialDate,
                finalDate,
                ProcessStatus.PARTIALLY_RECEIVED, ProcessStatus.RECEIVED, ProcessStatus.FINALIZED);

        DownloadFileControllerUtil.appendFile(file, response);
    }

}
