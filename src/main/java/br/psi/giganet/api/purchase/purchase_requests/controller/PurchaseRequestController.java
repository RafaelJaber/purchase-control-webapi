package br.psi.giganet.api.purchase.purchase_requests.controller;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.employees.service.EmployeeService;
import br.psi.giganet.api.purchase.purchase_requests.adapter.PurchaseRequestAdapter;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.InsertPurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.UpdatePurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.response.PurchaseRequestProjection;
import br.psi.giganet.api.purchase.purchase_requests.controller.response.PurchaseRequestResponse;
import br.psi.giganet.api.purchase.purchase_requests.controller.response.RequestedItemWithTraceResponse;
import br.psi.giganet.api.purchase.purchase_requests.controller.security.RolePurchaseRequestRead;
import br.psi.giganet.api.purchase.purchase_requests.controller.security.RolePurchaseRequestWrite;
import br.psi.giganet.api.purchase.purchase_requests.service.PurchaseRequestService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("purchase-requests")
public class PurchaseRequestController {

    @Autowired
    private PurchaseRequestService purchaseRequests;

    @Autowired
    private PurchaseRequestAdapter adapter;

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolePurchaseRequestWrite
    public Optional<PurchaseRequestProjection> insert(@RequestBody @Valid InsertPurchaseRequest purchaseRequest) {
        return this.purchaseRequests.insert(adapter.transform(purchaseRequest))
                .map(adapter::transform);
    }

    @PutMapping("/{id}")
    @RolePurchaseRequestWrite
    public PurchaseRequestResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePurchaseRequest purchaseRequest) {
        return this.purchaseRequests.update(id, adapter.transform(purchaseRequest))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
    }

    @PostMapping("/{id}/approvals")
    @RolePurchaseRequestWrite
    public PurchaseRequestProjection sendPurchaseRequestToApproval(@PathVariable final Long id) {
        return this.purchaseRequests.sendPurchaseRequestToApproval(id)
                .map(adapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

    @PostMapping("/{id}/canceled")
    @RolePurchaseRequestWrite
    public PurchaseRequestProjection cancelPurchaseRequest(@PathVariable final Long id) {
        return this.purchaseRequests.cancelPurchaseRequestById(id)
                .map(adapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole({ 'ROLE_PURCHASE_REQUESTS_READ', 'ROLE_PURCHASE_REQUESTS_READ_ALL', 'ROLE_PURCHASE_REQUESTS_WRITE', 'ROLE_ROOT' })")
    public List<PurchaseRequestProjection> findAllFilteringByCurrentUser() {
        return this.purchaseRequests.findAllFilteringByCurrentUser()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/items")
    @RolePurchaseRequestRead
    public Page<RequestedItemWithTraceResponse> findItemsByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer pageSize) {
        return this.purchaseRequests.findItemsByName(name, page, pageSize)
                .map(adapter::transform);
    }

    @GetMapping("/statuses")
    @RolePurchaseRequestRead
    public List<PurchaseRequestProjection> findByStatus(@RequestParam ProcessStatus status) {
        return this.purchaseRequests.findByStatus(status)
                .stream()
                .filter(request -> request.getRequester().isCurrentUserLogged() || employeeService.isUserRootCurrentLogged())
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RolePurchaseRequestRead
    public PurchaseRequestResponse findById(@PathVariable Long id) throws ResourceNotFoundException {
        return this.purchaseRequests.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação de compra não encontrada"));
    }

    @GetMapping("/reports/{id}")
    @RolePurchaseRequestRead
    public void getPurchaseRequestReport(
            @PathVariable final Long id, HttpServletResponse response)
            throws ResourceNotFoundException, SQLException, IOException, JRException {
        File file = this.purchaseRequests.getPurchaseRequestReport(id);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.join("; ", "attachment", "filename=" + file.getName()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        response.setContentType(Files.probeContentType(file.toPath()));

        Files.copy(file.toPath(), response.getOutputStream());
    }

}
