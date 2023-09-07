package br.psi.giganet.api.purchase.purchase_order.controller;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.common.utils.controller.DownloadFileControllerUtil;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.cost_center.adapter.CostCenterAdapter;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.api.purchase.purchase_order.controller.request.UpdatePurchaseOrderCompetenciesRequest;
import br.psi.giganet.api.purchase.purchase_order.controller.request.UpdatePurchaseOrderRequest;
import br.psi.giganet.api.purchase.purchase_order.controller.response.*;
import br.psi.giganet.api.purchase.purchase_order.controller.security.RolePurchaseOrdersCompetenciesWrite;
import br.psi.giganet.api.purchase.purchase_order.controller.security.RolePurchaseOrdersRead;
import br.psi.giganet.api.purchase.purchase_order.controller.security.RolePurchaseOrdersWrite;
import br.psi.giganet.api.purchase.purchase_order.service.PurchaseOrderService;
import br.psi.giganet.api.purchase.suppliers.adapter.SupplierAdapter;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService orders;

    @Autowired
    private PurchaseOrderAdapter adapter;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private CostCenterAdapter costCenterAdapter;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    @Autowired
    private SupplierAdapter supplierAdapter;

    @GetMapping(params = {"withoutApproval"})
    @RolePurchaseOrdersRead
    public List<PurchaseOrderProjectionWithoutApproval> findAllWithoutApproval() {
        return orders.findAll()
                .stream()
                .map(adapter::transformToProjectionWithoutApproval)
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"withQuotation"})
    @RolePurchaseOrdersRead
    public List<PurchaseOrderProjectionWithQuotation> findAllWithQuotation() {
        return orders.findAllWithQuotation()
                .stream()
                .map(adapter::transformToProjectionWithQuotation)
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"withQuotationAndCompetencies"})
    @RolePurchaseOrdersRead
    public List<OrderProjectionWithQuotationAndCompetencies> findAllWithQuotationAndCompetencies() {
        return orders.findAllWithQuotationAndCompetencies()
                .stream()
                .map(adapter::transformToProjectionWithQuotationAndCompetencies)
                .collect(Collectors.toList());
    }

    @GetMapping
    @RolePurchaseOrdersRead
    public List<PurchaseOrderProjection> findAll() {
        return orders.findAll()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"advanced"})
    @RolePurchaseOrdersRead
    public Page<AdvancedOrderProjection> findAllByAdvancedQueries(
            @RequestParam(defaultValue = "", name = "search") List<String> queries,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize
    ) {
        return orders.findAllAdvanced(queries, page, pageSize)
                .map(adapter::transformToAdvancedOrderProjection);
    }

    @GetMapping("/items")
    @RolePurchaseOrdersRead
    public Page<OrderItemProjection> findAllItems(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return orders.findAllByStatusNotCanceled(page, pageSize)
                .map(adapter::transformToOrderItemProjection);
    }

    @GetMapping("/items/quotation-approvals/{approval}")
    @RolePurchaseOrdersRead
    public List<PurchaseOrderItemResponse> findAllLastItemsByQuotationApproval(@PathVariable Long approval) {
        return orders.findAllLastPurchaseByQuotationApproval(approval)
                .stream()
                .map(adapter::transformWithDetails)
                .collect(Collectors.toList());
    }

    @GetMapping("/items/last-purchases")
    @RolePurchaseOrdersRead
    public List<PurchaseOrderItemResponse> findAllLastPurchaseByProducts(@RequestParam List<Long> products) {
        return orders.findAllLastPurchaseByProducts(
                products.stream().map(productAdapter::create)
                        .collect(Collectors.toList()))
                .stream()
                .map(adapter::transformWithDetails)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RolePurchaseOrdersRead
    public PurchaseOrderResponse findById(@PathVariable Long id) {
        return this.orders.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de compra não encontrada"));
    }

    @GetMapping("/{id}/pending")
    @RolePurchaseOrdersRead
    public PurchaseOrderResponse findByIdFilteringPendingItems(@PathVariable Long id) {
        return this.orders.findById(id)
                .map(order -> adapter.transformToFullResponse(order, true))
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de compra não encontrada"));
    }

    @PutMapping("/{id}")
    @RolePurchaseOrdersWrite
    public PurchaseOrderResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePurchaseOrderRequest request) {
        return this.orders.update(id, adapter.transform(request))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de compra não encontrada"));
    }

    @PutMapping("/{id}/competencies")
    @RolePurchaseOrdersCompetenciesWrite
    public PurchaseOrderResponse updatePurchaseOrderCompetencies(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePurchaseOrderCompetenciesRequest request) {
        return this.orders.updatePurchaseOrderCompetencies(id, adapter.transform(request))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de compra não encontrada"));
    }

    @GetMapping("/products/{product}")
    @RolePurchaseOrdersRead
    public LastPurchaseOrderItemResponse findLastByProduct(@PathVariable String product) throws ResourceNotFoundException {
        return this.orders
                .findLastByProduct(product)
                .map(adapter::transformToLastItemResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum registro de compra para este item foi encontrado"));
    }

    @GetMapping("/reports/{id}")
    @RolePurchaseOrdersRead
    public void getPurchaseOrderReport(
            @PathVariable final Long id, HttpServletResponse response)
            throws ResourceNotFoundException, SQLException, IOException, JRException {
        File file = this.orders.getPurchaseOrderReport(id);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping("/reports/competencies/{competence}")
    @RolePurchaseOrdersRead
    public void getPurchaseOrdersListByCompetenceReport(
            @PathVariable final LocalDate competence,
            @RequestParam List<Long> offices,
            HttpServletResponse response
    ) throws ResourceNotFoundException, SQLException, IOException, JRException {
        File file = this.orders.getPurchaseOrdersListByCompetenceReport(
                competence,
                offices.stream().map(branchOfficeAdapter::create).collect(Collectors.toList()),
                Arrays.asList(
                        ProcessStatus.IN_TRANSIT,
                        ProcessStatus.PARTIALLY_RECEIVED,
                        ProcessStatus.RECEIVED,
                        ProcessStatus.FINALIZED));

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/reports/competencies/{competence}", params = {"costCenters"})
    @RolePurchaseOrdersRead
    public void getPurchaseOrdersListByCompetenceAndCostCenterReport(
            @PathVariable final LocalDate competence,
            @RequestParam final List<Long> costCenters,
            @RequestParam(required = false) final Long office,
            HttpServletResponse response)
            throws ResourceNotFoundException, SQLException, IOException, JRException {

        File file = office != null ?
                this.orders.getPurchaseOrdersListByCompetenceAndCostCenterAndBranchOfficeReport(
                        competence,
                        costCenters.stream().map(costCenterAdapter::create).collect(Collectors.toList()),
                        Arrays.asList(
                                ProcessStatus.IN_TRANSIT,
                                ProcessStatus.PARTIALLY_RECEIVED,
                                ProcessStatus.RECEIVED,
                                ProcessStatus.FINALIZED
                        ),
                        branchOfficeAdapter.create(office)) :

                this.orders.getPurchaseOrdersListByCompetenceAndCostCenterReport(
                        competence,
                        costCenters.stream().map(costCenterAdapter::create).collect(Collectors.toList()),
                        Arrays.asList(
                                ProcessStatus.IN_TRANSIT,
                                ProcessStatus.PARTIALLY_RECEIVED,
                                ProcessStatus.RECEIVED,
                                ProcessStatus.FINALIZED
                        ));

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @GetMapping(path = "/reports/suppliers/{supplier}")
    @RolePurchaseOrdersRead
    public void getPurchaseOrdersBySupplierReport(
            @PathVariable final Long supplier,
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            @RequestParam List<ProcessStatus> statuses,
            HttpServletResponse response)
            throws ResourceNotFoundException, SQLException, IOException, JRException {

        File file = this.orders.getPurchaseOrdersBySupplierReport(
                supplierAdapter.create(supplier),
                initialDate,
                finalDate,
                statuses);

        DownloadFileControllerUtil.appendFile(file, response);
    }


}
