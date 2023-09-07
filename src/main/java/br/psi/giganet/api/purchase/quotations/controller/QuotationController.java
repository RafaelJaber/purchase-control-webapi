package br.psi.giganet.api.purchase.quotations.controller;

import br.psi.giganet.api.purchase.common.utils.controller.DownloadFileControllerUtil;
import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.quotations.adapter.QuotationAdapter;
import br.psi.giganet.api.purchase.quotations.controller.request.InsertQuotationRequest;
import br.psi.giganet.api.purchase.quotations.controller.request.SendEmailWithQuotationRequest;
import br.psi.giganet.api.purchase.quotations.controller.request.UpdateQuotationRequest;
import br.psi.giganet.api.purchase.quotations.controller.response.*;
import br.psi.giganet.api.purchase.quotations.controller.security.RoleQuotationsRead;
import br.psi.giganet.api.purchase.quotations.controller.security.RoleQuotationsWrite;
import br.psi.giganet.api.purchase.quotations.service.QuotationService;
import br.psi.giganet.api.purchase.suppliers.adapter.SupplierAdapter;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quotations")
public class QuotationController {

    @Autowired
    private QuotationService quotations;
    @Autowired
    private QuotationAdapter adapter;

    @Autowired
    private SupplierAdapter supplierAdapter;

    @Autowired
    private ProductAdapter productAdapter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleQuotationsWrite
    public QuotationResponse insert(@RequestBody @Valid InsertQuotationRequest request) {
        return this.quotations.insert(adapter.transform(request))
                .map(adapter::transformToFullResponse)
                .get();
    }

    @PutMapping("/{id}")
    @RoleQuotationsWrite
    public QuotationResponse quoteHandle(
            @PathVariable final Long id,
            @RequestBody @Valid UpdateQuotationRequest request) {
        return this.quotations.quoteHandle(id, adapter.transform(request))
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }

    @GetMapping
    @RoleQuotationsRead
    public List<QuotationProjection> findAll() {
        return this.quotations.findAllWithFetchEager()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleQuotationsRead
    public QuotationResponse findById(@PathVariable final Long id) {
        return this.quotations.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }

    @GetMapping(path = "/{id}", params = {"withUnits"})
    @RoleQuotationsRead
    public QuotationResponse findWithAvailableUnitsById(@PathVariable final Long id) {
        return this.quotations.findById(id)
                .map(i -> adapter.transformToFullResponse(i, QuotationAdapter.QuotedItemType.WITH_AVAILABLE_UNITS))
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }

    @GetMapping("/{id}/suppliers")
    @RoleQuotationsRead
    public QuotationSuppliersResponse findSuppliersByQuotation(@PathVariable final Long id) {
        return this.quotations.findById(id)
                .map(adapter::transformToQuotationSuppliersResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }

    @GetMapping("/items")
    @RoleQuotationsRead
    public Page<QuotedItemWithStageTraceResponse> findItemsByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer pageSize) {
        return this.quotations.findItemsByName(name, page, pageSize)
                .map(adapter::transform);
    }

    @PostMapping("/{id}/finalized")
    @RoleQuotationsRead
    public QuotationResponse markQuotationAsFinalized(@PathVariable final Long id) {
        return this.quotations.markQuotationAsFinalized(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }

    @GetMapping("/reports/{id}")
    @RoleQuotationsRead
    public void getQuotationReport(
            @PathVariable final Long id, @RequestParam(required = false) Long supplier,
            HttpServletResponse response)
            throws ResourceNotFoundException, SQLException, IOException, JRException {
        File file = supplier != null ?
                this.quotations.getQuotationReport(id, supplier) :
                this.quotations.getQuotationReport(id);

        DownloadFileControllerUtil.appendFile(file, response);
    }

    @PostMapping("/reports/{id}/emails")
    @RoleQuotationsRead
    public ResponseEntity<Object> sendEmailWithQuotation(
            @PathVariable final Long id, @RequestBody @Valid SendEmailWithQuotationRequest request)
            throws ResourceNotFoundException {

        final SecurityContext context = SecurityContextHolder.getContext();
        request.getSuppliers().forEach(supplier -> new Thread(() -> {
            try {
                SecurityContextHolder.setContext(context);
                quotations.sendEmailWithQuotation(id, supplierAdapter.transform(supplier), request.getSubject(), request.getMessage());
            } catch (SQLException | MessagingException | JRException | IOException | MailException ignored) { }
        }).start());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suppliers/{supplier}/products/{product}")
    @RoleQuotationsRead
    public LastQuotedItemResponse findLastBySupplierAndProduct(
            @PathVariable final Long supplier,
            @PathVariable final String product
    ) throws ResourceNotFoundException {
        return this.quotations
                .findLastBySupplierAndProduct(supplierAdapter.create(supplier), product)
                .map(adapter::transformToLastQuotedItemResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }


    @PostMapping("/{id}/canceled")
    @RoleQuotationsWrite
    public QuotationProjection cancelQuotationById(@PathVariable final Long id) {
        return this.quotations.cancelQuotationById(id)
                .map(adapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Cotação não encontrada"));
    }

}
