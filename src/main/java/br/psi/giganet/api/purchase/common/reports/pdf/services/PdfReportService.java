package br.psi.giganet.api.purchase.common.reports.pdf.services;

import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import br.psi.giganet.api.purchase.purchase_order.model.PurchaseOrder;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfReportService {

    @Autowired
    private DataSource dataSource;

    public File getPurchaseRequestReport(PurchaseRequest request) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("request", request.getId());
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header.jasper");
        params.put("items_list", "templates/reports/purchase_request/purchase_request_items.jasper");

        return exportToPdf(
                "purchase_request/",
                "solicitacao_de_compra_" + request.getId(),
                "purchase_request",
                params);
    }

    public File getPurchaseOrderReport(PurchaseOrder order) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("order", order.getId());
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/purchase_order/header_portrait.jasper");
        params.put("items_list", "templates/reports/purchase_order/purchase_order_items.jasper");

        return exportToPdf(
                "purchase_order/",
                "ordem_de_compra_" + order.getId(),
                "purchase_order",
                params);
    }

    public File getPurchaseOrdersListReport(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header_portrait_clean.jasper");
        params.put("initialDate", Date.valueOf(initialDate));
        params.put("finalDate", Date.valueOf(finalDate));
        params.put("statuses", Arrays.stream(statuses).map(ProcessStatus::name).collect(Collectors.toList()));

        return exportToPdf(
                "purchase_order/",
                "ordens_de_compra_" + initialDate.toString() + "_" + finalDate.toString(),
                "purchase_orders_list",
                params);
    }

    public File getPurchaseOrdersListDashboardReport(LocalDate initialDate, LocalDate finalDate, ProcessStatus... statuses) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header_portrait_clean.jasper");
        params.put("initialDate", Date.valueOf(initialDate));
        params.put("finalDate", Date.valueOf(finalDate));
        params.put("statuses", Arrays.stream(statuses).map(ProcessStatus::name).collect(Collectors.toList()));

        return exportToPdf(
                "purchase_order/",
                "ordens_de_compra_" + initialDate.toString() + "_" + finalDate.toString(),
                "purchase_orders_list_dashboard",
                params);
    }

    public File getPurchaseOrdersBySupplierReport(Supplier supplier, LocalDate initialDate, LocalDate finalDate, List<ProcessStatus> statuses) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header_portrait_clean.jasper");
        params.put("supplier", supplier.getId());
        params.put("initialDate", Date.valueOf(initialDate));
        params.put("finalDate", Date.valueOf(finalDate));
        params.put("statuses", statuses.stream().map(ProcessStatus::name).collect(Collectors.toList()));
        params.put("items_list", "templates/reports/purchase_order/purchase_orders_by_supplier_list.jasper");

        return exportToPdf(
                "purchase_order/",
                "ordens_de_compra_por_fornecedor_" + initialDate.toString() + "_" + finalDate.toString(),
                "purchase_orders_by_supplier",
                params);
    }

    public File getPurchaseOrdersListByCompetenceReport(LocalDate date, List<BranchOffice> branchOffices, List<ProcessStatus> statuses) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header_portrait_clean.jasper");
        params.put("initialDate", Date.valueOf(date));
        params.put("finalDate", Date.valueOf(date.plusMonths(1)));
        params.put("statuses", statuses.stream().map(ProcessStatus::name).collect(Collectors.toList()));
        params.put("offices", branchOffices.stream().map(BranchOffice::getId).collect(Collectors.toList()));

        return exportToPdf(
                "purchase_order/",
                "ordens_de_compra_por_competencia_" + date.format(DateTimeFormatter.ofPattern("MM-yyyy")) + "_",
                "purchase_orders_list_by_competence",
                params);
    }

    public File getPurchaseOrdersListByCompetenceAndCostCenterReport(LocalDate date, List<CostCenter> costCenters, List<ProcessStatus> statuses) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header_portrait_clean.jasper");
        params.put("initialDate", Date.valueOf(date));
        params.put("finalDate", Date.valueOf(date.plusMonths(1)));
        params.put("costCenters", costCenters.stream().map(CostCenter::getId).collect(Collectors.toList()));
        params.put("statuses", statuses.stream().map(ProcessStatus::name).collect(Collectors.toList()));

        return exportToPdf(
                "purchase_order/",
                "ordens_de_compra_por_competencia_e_centro_de_custo_" + date.format(DateTimeFormatter.ofPattern("MM-yyyy")) + "_",
                "purchase_orders_list_by_competence_and_cost_center",
                params);
    }

    public File getPurchaseOrdersListByCompetenceAndCostCenterAndBranchOfficeReport(
            LocalDate date,
            List<CostCenter> costCenters,
            List<ProcessStatus> statuses,
            BranchOffice branchOffice) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/commons/header_portrait_clean.jasper");
        params.put("initialDate", Date.valueOf(date));
        params.put("finalDate", Date.valueOf(date.plusMonths(1)));
        params.put("costCenters", costCenters.stream().map(CostCenter::getId).collect(Collectors.toList()));
        params.put("statuses", statuses.stream().map(ProcessStatus::name).collect(Collectors.toList()));
        params.put("officeId", branchOffice.getId());
        params.put("officeName", branchOffice.getName());

        return exportToPdf(
                "purchase_order/",
                "ordens_de_compra_por_competencia_e_centro_de_custo_" + date.format(DateTimeFormatter.ofPattern("MM-yyyy")) + "_",
                "purchase_orders_list_by_competence_and_cost_center_and_branch_office",
                params);
    }

    public File getQuotationReport(Quotation quotation, Supplier supplier) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("quotation", quotation.getId());
        params.put("supplier", supplier.getId());
        params.put("requester", quotation.getResponsible().getName());
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/quotation/header_portrait.jasper");
        params.put("header_report_info", "templates/reports/commons/header_report_info.jasper");
        params.put("items_list", "templates/reports/quotation/quoted_items_by_supplier.jasper");

        return exportToPdf(
                "quotation/",
                "cotacao_" + quotation.getId(),
                "quotation_suppliers",
                params);
    }

    public File getQuotationReport(Quotation quotation) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("quotation", quotation.getId());
        params.put("requester", quotation.getResponsible().getName());
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/quotation/header_portrait.jasper");
        params.put("header_report_info", "templates/reports/commons/header_report_info.jasper");
        params.put("items_list", "templates/reports/quotation/quoted_items.jasper");

        return exportToPdf(
                "quotation/",
                "cotacao_" + quotation.getId(),
                "quotation_resume",
                params);
    }

    public File exportToPdf(String path, String fileName, String template, Map<String, Object> params) throws IOException, JRException, SQLException {
        File temp = File.createTempFile(fileName, ".pdf");
        Connection connection = dataSource.getConnection();
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                loadTemplate(path, template),
                params,
                connection);

        // print report to file
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);
        exporter.setConfiguration(reportConfig);

        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(temp));
        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor("Giganet");
        exporter.setConfiguration(exportConfig);

        exporter.exportReport();

        connection.close();

        return temp;
    }

    private InputStream loadTemplate(String path, String templateName) throws IOException {
        return new ClassPathResource("templates/reports/" + path + templateName + ".jasper")
                .getInputStream();
    }
}
