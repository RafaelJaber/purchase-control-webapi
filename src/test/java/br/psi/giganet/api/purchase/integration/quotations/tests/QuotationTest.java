package br.psi.giganet.api.purchase.integration.quotations.tests;

import br.psi.giganet.api.purchase.approvals.model.ApprovalItem;
import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.settings.model.Setting;
import br.psi.giganet.api.purchase.common.settings.model.SettingOptions;
import br.psi.giganet.api.purchase.common.settings.repository.SettingRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.quotations.annotations.RoleTestQuotationRead;
import br.psi.giganet.api.purchase.integration.quotations.annotations.RoleTestQuotationWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.integration.utils.messages.Messages;
import br.psi.giganet.api.purchase.locations.repository.LocationRepository;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.controller.request.UpdateQuotationApprovalRequest;
import br.psi.giganet.api.purchase.quotation_approvals.model.QuotationApproval;
import br.psi.giganet.api.purchase.quotations.controller.request.*;
import br.psi.giganet.api.purchase.quotations.model.Quotation;
import br.psi.giganet.api.purchase.quotations.model.QuotedItem;
import br.psi.giganet.api.purchase.quotations.model.SupplierItemQuotation;
import br.psi.giganet.api.purchase.quotations.model.enums.FreightType;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuotationTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Quotation quotationTest;

    @MockBean
    private SettingRepository settingRepository;

    @Autowired
    public QuotationTest(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PurchaseRequestRepository purchaseRequestRepository,
            ApprovalRepository approvalRepository,
            QuotationRepository quotationRepository,
            UnitRepository unitRepository,
            ProductCategoryRepository productCategoryRepository,
            CostCenterRepository costCenterRepository,
            PaymentConditionRepository paymentConditionRepository,
            TaxRepository taxRepository,
            BranchOfficeRepository branchOfficeRepository,
            ProjectRepository projectRepository,
            LocationRepository locationRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.approvalRepository = approvalRepository;
        this.quotationRepository = quotationRepository;
        this.costCenterRepository = costCenterRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        this.taxRepository = taxRepository;
        this.branchOfficeRepository = branchOfficeRepository;
        this.projectRepository = projectRepository;
        this.locationRepository = locationRepository;

        createCurrentUser();

        quotationTest = createAndSaveQuotation();
        createNotificationPermissions();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/quotations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todas as cotações em ordem decrescente pela data de cadastro"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código da solicitação de compra"),
                                                fieldWithPath("note").description("Observações sobre a cotação"),
                                                fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                                                fieldWithPath("responsible").description("Responsável pela cotação"),
                                                fieldWithPath("branchOffice").description("Filial associada"),
                                                fieldWithPath("description").description("Descrição sobre a cotação"),
                                                fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                                                fieldWithPath("status").description("Status atual da cotação"))
                                        .andWithPrefix("[].branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))
                                        .andWithPrefix("[].responsible.",
                                                fieldWithPath("id").description("Código do responsável"),
                                                fieldWithPath("name").description("Nome do responsável"))));
    }

    @RoleTestRoot
    public void findLastBySupplierAndProduct() throws Exception {
        final SupplierItemQuotation supplier = quotationTest.getItems().get(0).getSuppliers().get(0);
        this.mockMvc.perform(
                get("/quotations/suppliers/{supplier}/products/{product}",
                        supplier.getSupplier().getId(),
                        supplier.getQuotedItem().getProduct().getCode())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("supplier").description(createDescriptionWithNotNull("Código do fornecedor")),
                                        parameterWithName("product").description(createDescriptionWithNotNull("Código do produto, como por exemplo, número serial"))),
                                responseFields(
                                        fieldWithPath("id").description("Código do relacionamento entre o item cotado e o fornecedor cotado"),
                                        fieldWithPath("supplier").description("Fornecedor cotado"),
                                        fieldWithPath("ipi").description("Valor do IPI cotado, em porcentagem"),
                                        fieldWithPath("icms").description("Valor do ICMS cotado, em porcentagem"),
                                        fieldWithPath("quantity").description("Quantidade selecionada durante a ultima cotação"),
                                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                                        fieldWithPath("price").description("Último preço unitário salvo para o item no respectivo fornecedor"))
                                        .andWithPrefix("supplier.",
                                                fieldWithPath("id").description("Código do fornecedor"),
                                                fieldWithPath("name").description("Nome do fornecedor"))
                                        .andWithPrefix("unit.",
                                                fieldWithPath("id").description("Código da unidade cotada"),
                                                fieldWithPath("abbreviation").description("Abreviação do nome da unidade cotada"),
                                                fieldWithPath("name").description("Nome da unidade cotada"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da cotação procurada"))),
                                getQuotationResponse()));
    }

    @RoleTestRoot
    public void findWithAvailableUnitsById() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}", quotationTest.getId())
                .param("withUnits", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(createDescriptionWithNotNull("Código da cotação procurada"))),
                                requestParameters(
                                        parameterWithName("withUnits")
                                                .description(createDescriptionWithNotEmpty(
                                                        "Parametro que determina que este endpoint seja associado",
                                                        "O valor associado é irrelevante, mas é necessário o parametro estar na URL"))),
                                getQuotationWithAvailableUnitsResponse()));
    }

    @RoleTestRoot
    public void getQuotationReport() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/reports/{id}", quotationTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da cotação procurada"))),
                                requestParameters(
                                        parameterWithName("supplier").optional()
                                                .description("Código do fornecedor desejado"))));
    }

    @RoleTestRoot
    public void findSuppliersByQuotation() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}/suppliers", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da cotação procurada"))),
                                responseFields(
                                        fieldWithPath("id").description("Código da cotação"),
                                        fieldWithPath("suppliers").description("Lista com todos os fornecedores presentes na cotação"))
                                        .andWithPrefix("suppliers[].",
                                                fieldWithPath("id").description("Código do fornecedor"),
                                                fieldWithPath("name").description("Nome do fornecedor"),
                                                fieldWithPath("email").description("Email do fornecedor"))));
    }

    @RoleTestRoot
    @Transactional
    public void markQuotationAsFinalized() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/{id}/finalized", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da cotação a ser encaminhada para aprovação")
                                ),
                                getQuotationResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void cancelQuotationById() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/{id}/canceled", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ProcessStatus.CANCELED.name())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da cotação a ser cancelada")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("Código da solicitação de compra"),
                                        fieldWithPath("note").description("Observações sobre a cotação"),
                                        fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                                        fieldWithPath("branchOffice").description("Filial associada"),
                                        fieldWithPath("responsible").description("Responsável pela cotação"),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                                        fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                                        fieldWithPath("status").description("Status atual da cotação"))
                                        .andWithPrefix("branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))
                                        .andWithPrefix("responsible.",
                                                fieldWithPath("id").description("Código do responsável"),
                                                fieldWithPath("name").description("Nome do responsável"))));
    }

    @RoleTestRoot
    public void sendEmailWithQuotation() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/reports/{id}/emails", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createValidSendEmailRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da cotação"))),
                                requestFields(
                                        fieldWithPath("subject").description(createDescriptionWithNotEmpty("Assunto do email", "Será enviado o mesmo para todos os fornecedores")),
                                        fieldWithPath("message").description(createDescriptionWithNotEmpty("Mensagem do email", "Será enviado o mesmo para todos os fornecedores")),
                                        fieldWithPath("suppliers")
                                                .description("Fornecedores selecionados"))
                                        .andWithPrefix("suppliers[].",
                                                fieldWithPath("id").description(createDescriptionWithNotNull("Id do fornecedor")),
                                                fieldWithPath("email").description(createDescriptionWithNotNull("Email de destino a ser enviado")))));
    }


    @RoleTestRoot
    public void findItemsByName() throws Exception {
        this.mockMvc.perform(get("/quotations/items")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(createDescription(
                                                        "Nome do produto a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription(
                                                        "Número da página solicitada",
                                                        "Valor baseado em 0 (ex: pagina inicial: 0)",
                                                        "Valor default: \"0\"")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription(
                                                        "Tamanho da página solicitada",
                                                        "Valor default: \"100\""))
                                ),
                                responseFields(
                                        fieldWithPath("content").description("Lista com todos os itens encontrados referentes aos filtros"),
                                        fieldWithPath("pageable").description("Informações sobre a paginação executada"),
                                        fieldWithPath("totalPages").description("Número total de páginas"),
                                        fieldWithPath("totalElements").description("Número total de elementos"),
                                        fieldWithPath("last").description("Retorna 'true' se a pagina atual é a última"),
                                        fieldWithPath("number").description("Número da página retornada"),
                                        fieldWithPath("size").description("Tamanho da página atual"),
                                        fieldWithPath("sort").description("Informações sobre a ordenação da página"),
                                        fieldWithPath("numberOfElements").description("Número de elementos da página"),
                                        fieldWithPath("first").description("Retorna 'true' se a pagina atual é a primeira"),
                                        fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                                        .andWithPrefix("pageable.",
                                                fieldWithPath("sort").description("Retorna informações sobre a ordenação"),
                                                fieldWithPath("offset").description("Retorna o offset da página atual"),
                                                fieldWithPath("pageNumber").description("Retorna o número da página atual"),
                                                fieldWithPath("pageSize").description("Retorna o tamanho da página solicitada"),
                                                fieldWithPath("paged").description("Retorna 'true' se a paginação está sendo executada"),
                                                fieldWithPath("unpaged").description("Retorna 'true' se a paginação não está sendo executada"))
                                        .andWithPrefix("pageable.sort.",
                                                fieldWithPath("sorted").description("Retorna 'true' se a pagina atual está ordenada"),
                                                fieldWithPath("unsorted").description("Retorna 'true' se a pagina atual está desordenada"),
                                                fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                                        .andWithPrefix("sort.",
                                                fieldWithPath("sorted").description("Retorna 'true' se a pagina atual está ordenada"),
                                                fieldWithPath("unsorted").description("Retorna 'true' se a pagina atual está desordenada"),
                                                fieldWithPath("empty").description("Retorna 'true' se a pagina atual é vazia"))
                                        .andWithPrefix("content[].",
                                                fieldWithPath("id").description("Código do item solicitado, gerado pelo banco de dados"),
                                                fieldWithPath("product").description("Produto solicitado"),
                                                fieldWithPath("quantity").description("Quantidade solicitada"),
                                                fieldWithPath("status").description("Situação do item"),
                                                fieldWithPath("total").description("Valor total do item cotado"),
                                                fieldWithPath("quotation").description("Cotação associada"),
                                                fieldWithPath("lastStage").description("Último estágio encontrado do item no fluxo de compras"),
                                                fieldWithPath("unit").description("Unidade solicitada"))

                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto solicitado, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade padrão do produto"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))

                                        .andWithPrefix("content[].unit.",
                                                fieldWithPath("id").description("Código da unidade solicitada"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))

                                        .andWithPrefix("content[].quotation.",
                                                fieldWithPath("id").description("Código da cotação"),
                                                fieldWithPath("note").description("Observações sobre a cotação"),
                                                fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                                                fieldWithPath("description").description("Descrição sobre a cotação"),
                                                fieldWithPath("branchOffice").description("Filial associada"),
                                                fieldWithPath("responsible").description("Responsável pela cotação"),
                                                fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                                                fieldWithPath("status").description("Status atual da cotação"))

                                        .andWithPrefix("content[].quotation.branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))
                                        .andWithPrefix("content[].quotation.responsible.",
                                                fieldWithPath("id").description("Código do responsável"),
                                                fieldWithPath("name").description("Nome do responsável"))

                                        .andWithPrefix("content[].lastStage.",
                                                fieldWithPath("id").description("Código do respectivo estágio associado"),
                                                fieldWithPath("stage").description(
                                                        createDescription("Nome do estágio.",
                                                                "Pode ser: 'QUOTATION', 'PURCHASE_REQUEST_APPROVAL' ou 'PURCHASE_REQUEST' ")
                                                ),
                                                fieldWithPath("lastModifiedDate").description("Data da ultima modificação do objeto referente ao estado"),
                                                fieldWithPath("status").description("Status do objeto referente ao estado"))));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("note")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description("Observações da cotação"),
                                        fieldWithPath("costCenter").description(createDescriptionWithNotNull("Código do centro de custo")),
                                        fieldWithPath("branchOffice").description(createDescriptionWithNotNull("Código da filial")),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                                        fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                                                .description("Link para algum site externo relacionado com a cotação"),
                                        fieldWithPath("location").optional().type(JsonFieldType.NUMBER)
                                                .description("Código ID da localidade associada a cotação, caso exista"),
                                        fieldWithPath("project").optional().type(JsonFieldType.NUMBER)
                                                .description("Código ID do projeto associado a cotação, caso exista"),
                                        fieldWithPath("dateOfNeed").optional().type(JsonFieldType.STRING)
                                                .description("Data de necessidade para os itens da cotação"),
                                        fieldWithPath("freight").description(createDescriptionWithNotNull("Frete, sendo apenas um por cotação")),
                                        fieldWithPath("paymentCondition").description(createDescriptionWithNotNull("Condição de pagamento cotada")),
                                        fieldWithPath("products").description(createDescriptionWithNotNull("Lista dos produtos cotados")))
                                        .andWithPrefix("freight.",
                                                fieldWithPath("type").description(createDescriptionWithNotNull("Tipo do frete. FOB ou CIF")),
                                                fieldWithPath("price").optional().type(JsonFieldType.NUMBER)
                                                        .description("Preço total do frete. Caso o tipo do frete seja FOB, este campo é obrigatório"))
                                        .andWithPrefix("paymentCondition.",
                                                fieldWithPath("condition").description(createDescriptionWithNotNull("Código da condição de pagamento")),
                                                fieldWithPath("dueDates").description(createDescriptionWithNotEmpty("Datas de vencimento para o pagamento")))
                                        .andWithPrefix("paymentCondition.dueDates[].",
                                                fieldWithPath("dueDate").description(createDescriptionWithNotNull("Data de vencimento do método de pagamento")))
                                        .andWithPrefix("products[].",
                                                fieldWithPath("code")
                                                        .description(createDescriptionWithNotEmpty(
                                                                "Código do produto cotado, como por exemplo o número serial")),
                                                fieldWithPath("quantity")
                                                        .description(createDescriptionWithNotNull(
                                                                "Quantidade cotada para o produto",
                                                                Messages.POSITIVE_NUMBER.getMessage())),
                                                fieldWithPath("approvalItem")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("Codigo do item da aprovação, caso este esteja associado"),
                                                fieldWithPath("unit").description(
                                                        createDescriptionWithNotNull("Unidade desejada para o respectivo produto")),
                                                fieldWithPath("suppliers")
                                                        .description(createDescriptionWithNotNull("Lista dos fornecedores cotados para o respectivo item")))
                                        .andWithPrefix("products[].suppliers[].",
                                                fieldWithPath("supplierId").description(createDescriptionWithNotNull("Código do fornecedor cotado")),
                                                fieldWithPath("quantity").description(createDescriptionWithNotNull("Quantidade selecionada")),
                                                fieldWithPath("unit").description(
                                                        createDescriptionWithNotNull("Unidade cotada no respectivo para o produto desejado")),
                                                fieldWithPath("ipi").description(createDescriptionWithPositiveAndNotNull(
                                                        "IPI para o item no respectivo fornecedor, em porcentagem")),
                                                fieldWithPath("icms").description(createDescriptionWithPositiveAndNotNull(
                                                        "ICMS para o item no respectivo fornecedor, em porcentagem")),
                                                fieldWithPath("price").description(createDescriptionWithNotNull(
                                                        "Preço unitário para o item no respectivo fornecedor")),
                                                fieldWithPath("discount").optional().type(JsonFieldType.NUMBER).description("O valor do desconto em reais, caso exista"),
                                                fieldWithPath("total").description(createDescriptionWithNotNull(
                                                        "Preço total para o item no respectivo fornecedor")),
                                                fieldWithPath("isSelected").description(createDescriptionWithNotNull(
                                                        "Indica se o respectivo fornecedor foi o selecionado para o item cotado"))),
                                getQuotationResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void quoteHandle() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da cotação a ser realizada/atualizada"))),
                                requestFields(
                                        fieldWithPath("id")
                                                .description(createDescriptionWithNotNull(
                                                        "Código da cotação, gerada pelo banco de dados")),
                                        fieldWithPath("note").optional().type(JsonFieldType.STRING)
                                                .description("Observações da cotação"),
                                        fieldWithPath("costCenter").description(createDescriptionWithNotNull("Código do centro de custo")),
                                        fieldWithPath("branchOffice").description(createDescriptionWithNotNull("Código da filial")),
                                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                                        fieldWithPath("location").optional().type(JsonFieldType.NUMBER)
                                                .description("Código ID da localidade associada a cotação, caso exista"),
                                        fieldWithPath("project").optional().type(JsonFieldType.NUMBER)
                                                .description("Código ID do projeto associado a cotação, caso exista"),
                                        fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                                                .description("Link para algum site externo relacionado com a cotação"),
                                        fieldWithPath("dateOfNeed").optional().type(JsonFieldType.STRING)
                                                .description("Data de necessidade para os itens da cotação"),
                                        fieldWithPath("freight").description(createDescriptionWithNotNull("Frete, sendo apenas um por cotação")),
                                        fieldWithPath("paymentCondition").description(createDescriptionWithNotNull("Condição de pagamento cotada")),
                                        fieldWithPath("products").description(createDescriptionWithNotNull("Lista dos produtos cotados")),
                                        fieldWithPath("products").description(createDescriptionWithNotNull("Lista dos produtos cotados")))
                                        .andWithPrefix("freight.",
                                                fieldWithPath("type").description(createDescriptionWithNotNull("Tipo do frete. FOB ou CIF")),
                                                fieldWithPath("price").optional().type(JsonFieldType.NUMBER)
                                                        .description("Preço total do frete. Caso o tipo do frete seja FOB, este campo é obrigatório"))
                                        .andWithPrefix("paymentCondition.",
                                                fieldWithPath("id").description(createDescriptionWithNotNull(
                                                        "Código do relacionamento entre cotação e condição de pagamento")),
                                                fieldWithPath("condition").description(createDescriptionWithNotNull("Código da condição de pagamento")),
                                                fieldWithPath("dueDates").description(createDescriptionWithNotEmpty("Datas de vencimento para o pagamento")))
                                        .andWithPrefix("paymentCondition.dueDates[].",
                                                fieldWithPath("id").description(createDescriptionWithNotNull("Código da data de vencimento do método de pagamento")),
                                                fieldWithPath("dueDate").description(createDescriptionWithNotNull("Data de vencimento do método de pagamento")))
                                        .andWithPrefix("products[].",
                                                fieldWithPath("id")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description(createDescriptionWithNotNull(
                                                                "Código do item de cotação gerada pelo banco de dados",
                                                                "Relacionamento entre cotação e os itens cotados")),
                                                fieldWithPath("code")
                                                        .description(createDescriptionWithNotEmpty(
                                                                "Código do produto cotado, como por exemplo o número serial")),
                                                fieldWithPath("quantity")
                                                        .description(createDescriptionWithNotNull(
                                                                "Quantidade cotada para o produto",
                                                                Messages.POSITIVE_NUMBER.getMessage())),
                                                fieldWithPath("unit").description(
                                                        createDescriptionWithNotNull("Unidade desejada para o respectivo produto")),
                                                fieldWithPath("approvalItem")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description("Codigo do item da aprovação, caso este esteja associado"),
                                                fieldWithPath("suppliers")
                                                        .description(createDescriptionWithNotNull(
                                                                "Lista dos fornecedores cotados para o respectivo item")))
                                        .andWithPrefix("products[].suppliers[].",
                                                fieldWithPath("id")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description(createDescriptionWithNotNull(
                                                                "Código do fornecedor do respectivo item de cotação gerado pelo banco de dados",
                                                                "Relacionamento entre fornecedores e os itens cotados")),
                                                fieldWithPath("supplierId").description(createDescriptionWithNotNull("Código do fornecedor cotado")),
                                                fieldWithPath("quantity").description(createDescriptionWithNotNull("Quantidade selecionada")),
                                                fieldWithPath("unit").description(
                                                        createDescriptionWithNotNull("Unidade cotada no respectivo para o produto desejado")),
                                                fieldWithPath("ipi").description(createDescriptionWithPositiveAndNotNull(
                                                        "IPI para o item no respectivo fornecedor, em porcentagem")),
                                                fieldWithPath("icms").description(createDescriptionWithPositiveAndNotNull(
                                                        "ICMS para o item no respectivo fornecedor, em porcentagem")),
                                                fieldWithPath("price").description(createDescriptionWithNotNull(
                                                        "Preço unitário para o item no respectivo fornecedor")),
                                                fieldWithPath("discount").optional().type(JsonFieldType.NUMBER).description("O valor do desconto em reais, caso exista"),
                                                fieldWithPath("total").description(createDescriptionWithNotNull(
                                                        "Preço total para o item no respectivo fornecedor")),
                                                fieldWithPath("isSelected").description(createDescriptionWithNotNull(
                                                        "Indica se o respectivo fornecedor foi o selecionado para o item cotado"))),
                                getQuotationResponse()));
    }

    private ResponseFieldsSnippet getQuotationResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da cotação"),
                fieldWithPath("responsible")
                        .type(JsonFieldType.OBJECT)
                        .description("Responsável pela cotação"),
                fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("note").description("Observação"),
                fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                fieldWithPath("status").description("Status atual da cotação"),
                fieldWithPath("costCenter").description("Centro de custo da cotação"),
                fieldWithPath("branchOffice")
                        .optional()
                        .type(JsonFieldType.OBJECT)
                        .description(createDescription(
                                "Filial relacionada a cotação",
                                "Note que: este campo pode ser nulo em caso de dados legados",
                                "Para as novas cotações, este campo é obrigatório")),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                        .description("Link para algum site externo relacionado com a cotação"),
                fieldWithPath("location").optional().type(JsonFieldType.OBJECT)
                        .description("Código ID da localidade associada a cotação, caso exista"),
                fieldWithPath("project").optional().type(JsonFieldType.OBJECT)
                        .description("Código ID do projeto associado a cotação, caso exista"),
                fieldWithPath("freight").description("Frete da cotação"),
                fieldWithPath("paymentCondition").description("Condição de pagamento para a cotação"),
                fieldWithPath("items").description("Lista de itens cotados"),
                fieldWithPath("dateOfNeed").optional()
                        .type(JsonFieldType.STRING)
                        .description("Data de necessidade. Exemplo: " + LocalDate.now().toString()))
                .andWithPrefix("freight.",
                        fieldWithPath("type").description("Tipo do frete. FOB ou CIF"),
                        fieldWithPath("price").description("Preço total do frete"))
                .andWithPrefix("paymentCondition.",
                        fieldWithPath("id").description("Código do relacionamento entre a cotação e a condição de pagamento"),
                        fieldWithPath("condition").description("Condição de pagamento selecionada"),
                        fieldWithPath("dueDates").description("Datas dos vencimentos das parcelas"))
                .andWithPrefix("paymentCondition.condition.",
                        fieldWithPath("id").description("Código da condição de pagamento"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                        fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                        fieldWithPath("description").description("Descrição da condição de pagamento"))
                .andWithPrefix("paymentCondition.dueDates[].",
                        fieldWithPath("id").description("Código do relacionemnto entre a condição de pagamento selecionada e a data de vencimento"),
                        fieldWithPath("dueDate").description("Data do vencimento"))
                .andWithPrefix("costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("project.",
                        fieldWithPath("id").description("Código do projeto"),
                        fieldWithPath("name").description("Nome do projeto"),
                        fieldWithPath("description").description("Descrição do projeto"))
                .andWithPrefix("location.",
                        fieldWithPath("id").description("Código da localidade"),
                        fieldWithPath("name").description("Nome da localidade"),
                        fieldWithPath("description").description("Descrição da localidade"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código do relacionamento entre produto e a cotação"),
                        fieldWithPath("product").description("Produto solicitado"),
                        fieldWithPath("quantity").description("Quantidade solicitada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário selecionado para o item"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total selecionado para o item"),
                        fieldWithPath("approval")
                                .optional()
                                .type(JsonFieldType.NUMBER)
                                .description("Codigo da aprovação, caso este esteja associado a alguma"),
                        fieldWithPath("approvalItem")
                                .optional()
                                .type(JsonFieldType.NUMBER)
                                .description("Codigo do item da aprovação, caso este esteja associado"),
                        fieldWithPath("suppliers").description("Lista dos fornecedores cotados para o respectivo item"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"))
                .andWithPrefix("items[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("items[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o respectivo produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade padrão do produto"))
                .andWithPrefix("items[].suppliers[].",
                        fieldWithPath("id").description("Código do relacionamento entre o item cotado e o fornecedor cotado"),
                        fieldWithPath("supplier").description("Fornecedor cotado"),
                        fieldWithPath("quantity").description("Quantidade selecionada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("unit").description("Unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("price").description("Preço unitário para o item no respectivo fornecedor"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total para o item no respectivo fornecedor"),
                        fieldWithPath("isSelected").description("Indica se o respectivo fornecedor foi o selecionado para o item cotado"))
                .andWithPrefix("items[].suppliers[].unit.",
                        fieldWithPath("id").description("Código da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("name").description("Nome da unidade cotada para o produto no respectivo fornecedor"))
                .andWithPrefix("items[].suppliers[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    private ResponseFieldsSnippet getQuotationWithAvailableUnitsResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da cotação"),
                fieldWithPath("responsible")
                        .type(JsonFieldType.OBJECT)
                        .description("Responsável pela cotação"),
                fieldWithPath("date").description("Data da cotação. Exemplo: " + ZonedDateTime.now().toString()),
                fieldWithPath("note").description("Observação"),
                fieldWithPath("total").description("Valor total da cotação, incluindo o valor total de todos os itens e o valor do frete"),
                fieldWithPath("status").description("Status atual da cotação"),
                fieldWithPath("costCenter").description("Centro de custo da cotação"),
                fieldWithPath("branchOffice").description("Filial associada cotação"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição sobre a cotação"),
                fieldWithPath("location").optional().type(JsonFieldType.OBJECT)
                        .description("Código ID da localidade associada a cotação, caso exista"),
                fieldWithPath("project").optional().type(JsonFieldType.OBJECT)
                        .description("Código ID do projeto associado a cotação, caso exista"),
                fieldWithPath("externalLink").optional().type(JsonFieldType.STRING)
                        .description("Link para algum site externo relacionado com a cotação"),
                fieldWithPath("freight").description("Frete da cotação"),
                fieldWithPath("paymentCondition").description("Condição de pagamento para a cotação"),
                fieldWithPath("items").description("Lista de itens cotados"),
                fieldWithPath("dateOfNeed").optional()
                        .type(JsonFieldType.STRING)
                        .description("Data de necessidade. Exemplo: " + LocalDate.now().toString()))
                .andWithPrefix("freight.",
                        fieldWithPath("type").description("Tipo do frete. FOB ou CIF"),
                        fieldWithPath("price").description("Preço total do frete"))
                .andWithPrefix("branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"))
                .andWithPrefix("paymentCondition.",
                        fieldWithPath("id").description("Código do relacionamento entre a cotação e a condição de pagamento"),
                        fieldWithPath("condition").description("Condição de pagamento selecionada"),
                        fieldWithPath("dueDates").description("Datas dos vencimentos das parcelas"))
                .andWithPrefix("paymentCondition.condition.",
                        fieldWithPath("id").description("Código da condição de pagamento"),
                        fieldWithPath("name").description("Nome"),
                        fieldWithPath("numberOfInstallments").description("Número de parcelas do pagamento"),
                        fieldWithPath("daysInterval").description("Intervalo entre as parcelas, em dias"),
                        fieldWithPath("description").description("Descrição da condição de pagamento"))
                .andWithPrefix("paymentCondition.dueDates[].",
                        fieldWithPath("id").description("Código do relacionemnto entre a condição de pagamento selecionada e a data de vencimento"),
                        fieldWithPath("dueDate").description("Data do vencimento"))
                .andWithPrefix("costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").description("Descrição do centro de custo"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("project.",
                        fieldWithPath("id").description("Código do projeto"),
                        fieldWithPath("name").description("Nome do projeto"),
                        fieldWithPath("description").description("Descrição do projeto"))
                .andWithPrefix("location.",
                        fieldWithPath("id").description("Código da localidade"),
                        fieldWithPath("name").description("Nome da localidade"),
                        fieldWithPath("description").description("Descrição da localidade"))
                .andWithPrefix("items[].",
                        fieldWithPath("id").description("Código do relacionamento entre produto e a cotação"),
                        fieldWithPath("item").description("Item cotado"),
                        fieldWithPath("quantity").description("Quantidade solicitada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto, em porcentagem"),
                        fieldWithPath("price").description("Preço unitário selecionado para o item"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total selecionado para o item"),
                        fieldWithPath("approval")
                                .optional()
                                .type(JsonFieldType.NUMBER)
                                .description("Codigo da aprovação, caso este esteja associado a alguma"),
                        fieldWithPath("approvalItem")
                                .optional()
                                .type(JsonFieldType.NUMBER)
                                .description("Codigo do item da aprovação, caso este esteja associado"),
                        fieldWithPath("suppliers").description("Lista dos fornecedores cotados para o respectivo item"),
                        fieldWithPath("unit").description("Unidade selecionada para o produto"))
                .andWithPrefix("items[].unit.",
                        fieldWithPath("id").description("Código da unidade selecionada para o produto"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade selecionada para o produto"),
                        fieldWithPath("name").description("Nome da unidade selecionada para o produto"))
                .andWithPrefix("items[].item.",
                        fieldWithPath("product").description("Objeto contendo as informações do item cotado"),
                        fieldWithPath("availableUnits").description("Lista com as unidades permitidas para o respectivo produto"))
                .andWithPrefix("items[].item.product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"),
                        fieldWithPath("unit").description("Unidade padrão para o respectivo produto"),
                        fieldWithPath("manufacturer").description("Nome do fabricante do produto"))
                .andWithPrefix("items[].item.product.unit.",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade padrão do produto"))
                .andWithPrefix("items[].item.availableUnits[].",
                        fieldWithPath("id").description("Código da unidade padrão do produto"),
                        fieldWithPath("name").description("Nome da unidade padrão do produto"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade padrão do produto"))
                .andWithPrefix("items[].suppliers[].",
                        fieldWithPath("id").description("Código do relacionamento entre o item cotado e o fornecedor cotado"),
                        fieldWithPath("supplier").description("Fornecedor cotado"),
                        fieldWithPath("quantity").description("Quantidade selecionada"),
                        fieldWithPath("ipi").description("Valor do IPI cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("icms").description("Valor do ICMS cotado para o produto no respectivo fornecedor, em porcentagem"),
                        fieldWithPath("unit").description("Unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("price").description("Preço unitário para o item no respectivo fornecedor"),
                        fieldWithPath("discount").description("O valor do desconto em reais, caso exista"),
                        fieldWithPath("total").description("Preço total para o item no respectivo fornecedor"),
                        fieldWithPath("isSelected").description("Indica se o respectivo fornecedor foi o selecionado para o item cotado"))
                .andWithPrefix("items[].suppliers[].unit.",
                        fieldWithPath("id").description("Código da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade cotada para o produto no respectivo fornecedor"),
                        fieldWithPath("name").description("Nome da unidade cotada para o produto no respectivo fornecedor"))
                .andWithPrefix("items[].suppliers[].supplier.",
                        fieldWithPath("id").description("Código do fornecedor"),
                        fieldWithPath("name").description("Nome do fornecedor"));
    }

    @Override
    @RoleTestQuotationRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/quotations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        final SupplierItemQuotation supplier = quotationTest.getItems().get(0).getSuppliers().get(0);
        this.mockMvc.perform(
                get("/quotations/suppliers/{supplier}/products/{product}",
                        supplier.getSupplier().getId(),
                        supplier.getQuotedItem().getProduct().getCode())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/reports/{id}", quotationTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/reports/{id}/emails", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createValidSendEmailRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}/suppliers", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}", quotationTest.getId())
                .param("withUnits", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/quotations/items")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestQuotationWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/{id}/canceled", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/quotations")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        final SupplierItemQuotation supplier = quotationTest.getItems().get(0).getSuppliers().get(0);
        this.mockMvc.perform(
                get("/quotations/suppliers/{supplier}/products/{product}",
                        supplier.getSupplier().getId(),
                        supplier.getQuotedItem().getProduct().getCode())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/reports/{id}", quotationTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/reports/{id}/emails", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createValidSendEmailRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}/suppliers", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotations/{id}", quotationTest.getId())
                .param("withUnits", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/quotations/items")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/quotations/{id}/canceled", quotationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }


    @RoleTestRoot
    @Transactional
    public void validateCancelQuotationFlux() throws Exception {
        // test 1 -> cancel twice
        String quotation1 = this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quotationId1 = objectMapper.readTree(quotation1).get("id").asLong();
        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.CANCELED.name())));

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // test 2 ->  create, update, cancel, try to cancel again and try to mark as finalized
        String quotation2 = this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quotationId2 = objectMapper.readTree(quotation2).get("id").asLong();

        this.mockMvc.perform(put("/quotations/{id}", quotationId2)
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId2)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId2)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/quotations/{id}/finalized", quotationId2)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());


        // test 3 -> create, update, mark as finalize, cancel and try to cancel again
        String quotation3 = this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quotationId3 = objectMapper.readTree(quotation3).get("id").asLong();

        this.mockMvc.perform(put("/quotations/{id}", quotationId3)
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/finalized", quotationId3)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId3)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId3)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());


        // test 4 -> try to cancel a approved quotation
        String quotation4 = this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quotationId4 = objectMapper.readTree(quotation4).get("id").asLong();

        this.mockMvc.perform(put("/quotations/{id}", quotationId4)
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/finalized", quotationId4)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        QuotationApproval approval4 = quotationRepository.findById(quotationId4).get().getApproval();
        UpdateQuotationApprovalRequest evaluateRequest4 = new UpdateQuotationApprovalRequest();
        evaluateRequest4.setId(approval4.getId());
        evaluateRequest4.setEvaluation(ProcessStatus.APPROVED);
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotation-approvals/{id}", approval4.getId())
                .content(objectMapper.writeValueAsString(evaluateRequest4))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId4)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // test 5 -> try to cancel a rejected quotation
        String quotation5 = this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quotationId5 = objectMapper.readTree(quotation5).get("id").asLong();

        this.mockMvc.perform(put("/quotations/{id}", quotationId5)
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/finalized", quotationId5)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        QuotationApproval approval5 = quotationRepository.findById(quotationId5).get().getApproval();
        UpdateQuotationApprovalRequest evaluateRequest5 = new UpdateQuotationApprovalRequest();
        evaluateRequest5.setId(approval5.getId());
        evaluateRequest5.setEvaluation(ProcessStatus.REJECTED);
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotation-approvals/{id}", approval5.getId())
                .content(objectMapper.writeValueAsString(evaluateRequest5))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/quotations/{id}/canceled", quotationId4)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());


    }

    @RoleTestRoot
    @Transactional
    public void checkAutoApproveQuotationByTotalFlux() throws Exception {
        String quotation = this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createValidInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quotationId = objectMapper.readTree(quotation).get("id").asLong();

        this.mockMvc.perform(put("/quotations/{id}", quotationId)
                .content(objectMapper.writeValueAsString(createValidUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Quotation savedQuotation = quotationRepository.findById(quotationId).orElseThrow();
        final String key = SettingOptions.MINIMAL_QUANTITY_TO_AUTO_APPROVE_QUOTATION.name();
        Mockito.when(settingRepository.findByKey(key))
                .thenReturn(Optional.of(new Setting(key, savedQuotation.getTotal().toPlainString())));

        this.mockMvc.perform(post("/quotations/{id}/finalized", quotationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        QuotationApproval approval = quotationRepository.findById(quotationId).get().getApproval();
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/quotation-approvals/{id}", approval.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluation", Matchers.is(ProcessStatus.APPROVED.name())));


    }


    /**
     * This method creates five invalid insert methods to check the validations.
     * All information are the same that createValidInsertQuotation.
     * <ul>
     *     <li>Create a new quotation with items without supplier</li>
     *     <li>Create a new quotation with empty items array</li>
     *     <li>Create a new quotation with valid information but without mark a supplier as selected.</li>
     *     <li>Create a new quotation with invalid freight (Type FOB but without any price) </li>
     *     <li>Create a new quotation without some valid units </li>
     * </ul>
     * <br>
     * Category: INVALID
     */
    @RoleTestRoot
    @Transactional
    public void invalidInsert() throws Exception {
        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createEmptySuppliersInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createEmptyProductsInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createNotSelectedSupplierInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.notNullValue()))
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createInvalidFreightInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.notNullValue()))
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(post("/quotations")
                .content(objectMapper.writeValueAsString(createInvalidUnitsInsertQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));
    }

    /**
     * This method creates two valid quote methods to check the validations.
     * All information are the same that createValidUpdateQuotation.
     * <ul>
     *     <li>Create a new update quotation adding new items only</li>
     *     <li>Create a new update quotation updating existing items only</li>
     * </ul>
     * <br>
     * Category: VALID
     */
    @RoleTestRoot
    @Transactional
    public void validQuoteHandle() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createOnlyAddNewItemsUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createOnlyUpdateItemsUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }


    /**
     * This method creates five invalid insert methods to check the validations.
     * All information are the same that createValidInsertQuotation.
     * <ul>
     *     <li>Create an update quotation with empty items array</li>
     *     <li>Create an update quotation with items without supplier</li>
     *     <li>Create an update quotation with valid information but without mark a supplier as selected.</li>
     *     <li>Create an update quotation with invalid freight (Type FOB but without any price) </li>
     *     <li>Create an update quotation without some valid units </li>
     * </ul>
     * <br>
     * Category: INVALID
     */
    @RoleTestRoot
    @Transactional
    public void invalidQuoteHandle() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createEmptyProductsUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createEmptySuppliersUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createOnlyAddNewItemsWithoutSelectedSupplierUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.notNullValue()))
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createInvalidFreightUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.notNullValue()))
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createMissingBranchOfficeUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/quotations/{id}", quotationTest.getId())
                .content(objectMapper.writeValueAsString(createInvalidUnitsUpdateQuotation()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.notNullValue()))
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));
    }

    private SendEmailWithQuotationRequest createValidSendEmailRequest() {
        SendEmailWithQuotationRequest request = new SendEmailWithQuotationRequest();
        request.setMessage("Mensagem de teste");
        request.setSubject("Assunto da mensagem de teste");
        request.setSuppliers(quotationTest.getItems().get(0).getSuppliers().stream().map(s -> {
            final SupplierEmailDestinyRequest destiny = new SupplierEmailDestinyRequest();
            destiny.setEmail(s.getSupplier().getEmail());
            destiny.setId(s.getSupplier().getId());
            return destiny;
        }).collect(Collectors.toList()));

        return request;
    }

    private InsertQuotationRequest createValidInsertQuotation() {
        InsertQuotationRequest request = new InsertQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());
        request.setDescription("Descrição da cotação");
        request.setExternalLink("https://link-externo.com.br");

        request.setProject(createAndSaveProject().getId());
        request.setLocation(createAndSaveLocation().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new InsertPaymentConditionRequest());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        InsertConditionDateDueRequest dueDate = new InsertConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 2; i++) {
            final InsertQuotedItemRequest item = new InsertQuotedItemRequest();

            Product product;
            if (i < 1) {
                final ApprovalItem approvalItem = quotationTest.getItems()
                        .parallelStream()
                        .filter(quotedItem -> quotedItem.getApprovedItem() != null)
                        .findAny()
                        .get()
                        .getApprovedItem();
                item.setApprovalItem(approvalItem.getId());
                product = approvalItem.getItem().getProduct();
            } else {
                product = createAndSaveProduct();
            }
            item.setCode(product.getCode());

            item.setUnit(product.getUnit().getId());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());

            for (int j = 0; j < 2; j++) {
                final var supplier = new InsertSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());
                supplier.setDiscount(BigDecimal.ZERO);
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }


    private InsertQuotationRequest createInvalidUnitsInsertQuotation() {
        InsertQuotationRequest request = new InsertQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new InsertPaymentConditionRequest());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        InsertConditionDateDueRequest dueDate = new InsertConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 2; i++) {
            final InsertQuotedItemRequest item = new InsertQuotedItemRequest();

            if (i < 1) {
                final ApprovalItem approvalItem = quotationTest.getItems()
                        .parallelStream()
                        .filter(quotedItem -> quotedItem.getApprovedItem() != null)
                        .findAny()
                        .get()
                        .getApprovedItem();
                item.setApprovalItem(approvalItem.getId());
                item.setCode(approvalItem.getItem().getProduct().getCode());
            } else {
                item.setCode(createAndSaveProduct().getCode());
            }

            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());

            for (int j = 0; j < 2; j++) {
                final var supplier = new InsertSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private InsertQuotationRequest createInvalidFreightInsertQuotation() {
        InsertQuotationRequest request = new InsertQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.valueOf(-1));
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new InsertPaymentConditionRequest());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        InsertConditionDateDueRequest dueDate = new InsertConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 2; i++) {
            final InsertQuotedItemRequest item = new InsertQuotedItemRequest();

            Product product;
            if (i < 1) {
                final ApprovalItem approvalItem = quotationTest.getItems()
                        .parallelStream()
                        .filter(quotedItem -> quotedItem.getApprovedItem() != null)
                        .findAny()
                        .get()
                        .getApprovedItem();
                item.setApprovalItem(approvalItem.getId());
                product = approvalItem.getItem().getProduct();
            } else {
                product = createAndSaveProduct();
            }
            item.setCode(product.getCode());

            item.setUnit(product.getUnit().getId());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());

            for (int j = 0; j < 2; j++) {
                final var supplier = new InsertSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private InsertQuotationRequest createNotSelectedSupplierInsertQuotation() {
        InsertQuotationRequest request = new InsertQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new InsertPaymentConditionRequest());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        InsertConditionDateDueRequest dueDate = new InsertConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 2; i++) {
            final InsertQuotedItemRequest item = new InsertQuotedItemRequest();

            Product product;
            if (i < 1) {
                final ApprovalItem approvalItem = quotationTest.getItems()
                        .parallelStream()
                        .filter(quotedItem -> quotedItem.getApprovedItem() != null)
                        .findAny()
                        .get()
                        .getApprovedItem();
                item.setApprovalItem(approvalItem.getId());
                product = approvalItem.getItem().getProduct();
            } else {
                product = createAndSaveProduct();
            }
            item.setCode(product.getCode());

            item.setUnit(product.getUnit().getId());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());

            for (int j = 0; j < 2; j++) {
                final var supplier = new InsertSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }

            request.getProducts().add(item);
        }

        return request;
    }

    private InsertQuotationRequest createEmptySuppliersInsertQuotation() {
        InsertQuotationRequest request = new InsertQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new InsertPaymentConditionRequest());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        InsertConditionDateDueRequest dueDate = new InsertConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 2; i++) {
            final InsertQuotedItemRequest item = new InsertQuotedItemRequest();

            Product product;
            if (i < 1) {
                final ApprovalItem approvalItem = quotationTest.getItems()
                        .parallelStream()
                        .filter(quotedItem -> quotedItem.getApprovedItem() != null)
                        .findAny()
                        .get()
                        .getApprovedItem();
                item.setApprovalItem(approvalItem.getId());
                product = approvalItem.getItem().getProduct();
            } else {
                product = createAndSaveProduct();
            }
            item.setCode(product.getCode());

            item.setUnit(product.getUnit().getId());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());

            request.getProducts().add(item);
        }

        return request;
    }

    private InsertQuotationRequest createEmptyProductsInsertQuotation() {
        InsertQuotationRequest request = new InsertQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new InsertPaymentConditionRequest());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        InsertConditionDateDueRequest dueDate = new InsertConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        return request;
    }

    private UpdateQuotationRequest createValidUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());
        request.setDescription("Descrição da cotação");

        request.setProject(createAndSaveProject().getId());
        request.setLocation(createAndSaveLocation().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());


        for (int i = 0; i < 1; i++) {
            final QuotedItem quotedItem = quotationTest.getItems().get(i);

            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setId(quotedItem.getId());
            item.setApprovalItem(quotedItem.getApprovedItem() != null ? quotedItem.getApprovedItem().getId() : null);
            item.setCode(quotedItem.getProduct().getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(quotedItem.getProduct().getUnit().getId());

            for (SupplierItemQuotation s : quotedItem.getSuppliers()) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setId(s.getId());
                supplier.setSupplierId(s.getSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        for (int i = 0; i < 1; i++) {
            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            Product product = createAndSaveProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (int j = 0; j < 2; j++) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(product.getUnit().getId());

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createMissingBranchOfficeUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setDescription("Descrição da cotação");

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 1; i++) {
            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            Product product = createAndSaveProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (int j = 0; j < 2; j++) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(product.getUnit().getId());

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }


    private UpdateQuotationRequest createInvalidUnitsUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());


        for (int i = 0; i < 1; i++) {
            final QuotedItem quotedItem = quotationTest.getItems().get(i);

            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setId(quotedItem.getId());
            item.setApprovalItem(quotedItem.getApprovedItem() != null ? quotedItem.getApprovedItem().getId() : null);
            item.setCode(quotedItem.getProduct().getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(createAndSaveUnit().getId());

            for (SupplierItemQuotation s : quotedItem.getSuppliers()) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setId(s.getId());
                supplier.setSupplierId(s.getSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        for (int i = 0; i < 1; i++) {
            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setCode(createAndSaveProduct().getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(createAndSaveUnit().getId());

            for (int j = 0; j < 2; j++) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createInvalidFreightUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.valueOf(-1));
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());


        for (int i = 0; i < 1; i++) {
            final QuotedItem quotedItem = quotationTest.getItems().get(i);

            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setId(quotedItem.getId());
            item.setApprovalItem(quotedItem.getApprovedItem() != null ? quotedItem.getApprovedItem().getId() : null);
            Product product = quotedItem.getProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (SupplierItemQuotation s : quotedItem.getSuppliers()) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setId(s.getId());
                supplier.setSupplierId(s.getSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(s.getUnit().getId());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        for (int i = 0; i < 1; i++) {
            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            Product product = createAndSaveProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (int j = 0; j < 2; j++) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createOnlyAddNewItemsUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());

        for (int i = 0; i < 2; i++) {
            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            Product product = createAndSaveProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (int j = 0; j < 2; j++) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createOnlyAddNewItemsWithoutSelectedSupplierUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());


        for (int i = 0; i < 1; i++) {
            final QuotedItem quotedItem = quotationTest.getItems().get(i);

            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setId(quotedItem.getId());
            item.setApprovalItem(quotedItem.getApprovedItem() != null ? quotedItem.getApprovedItem().getId() : null);
            Product product = quotedItem.getProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (SupplierItemQuotation s : quotedItem.getSuppliers()) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setId(s.getId());
                supplier.setSupplierId(s.getSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(s.getUnit().getId());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createOnlyUpdateItemsUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setCostCenter(createAndSaveCostCenter().getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        request.setFreight(new QuotationFreightRequest());
        request.getFreight().setPrice(BigDecimal.TEN);
        request.getFreight().setType(FreightType.FOB);

        request.setPaymentCondition(new UpdatePaymentConditionRequest());
        request.getPaymentCondition().setId(quotationTest.getPaymentCondition().getId());
        request.getPaymentCondition().setCondition(createAndSavePaymentCondition().getId());
        UpdateConditionDateDueRequest dueDate = new UpdateConditionDateDueRequest();
        dueDate.setDueDate(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(3).toString());
        request.getPaymentCondition().setDueDates(Collections.singletonList(dueDate));

        request.setDateOfNeed(LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString());


        for (int i = 0; i < 2; i++) {
            final QuotedItem quotedItem = quotationTest.getItems().get(i);

            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setId(quotedItem.getId());
            item.setApprovalItem(quotedItem.getApprovedItem() != null ? quotedItem.getApprovedItem().getId() : null);
            Product product = quotedItem.getProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (SupplierItemQuotation s : quotedItem.getSuppliers()) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setId(s.getId());
                supplier.setSupplierId(s.getSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setIpi(4f);
                supplier.setIcms(18f);
                supplier.setUnit(item.getUnit());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createEmptySuppliersUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());

        request.setBranchOffice(createAndSaveBranchOffice().getId());

        for (int i = 0; i < 1; i++) {
            final QuotedItem quotedItem = quotationTest.getItems().get(i);

            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            item.setId(quotedItem.getId());
            item.setApprovalItem(quotedItem.getApprovedItem() != null ? quotedItem.getApprovedItem().getId() : null);
            Product product = quotedItem.getProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            request.getProducts().add(item);
        }

        for (int i = 0; i < 1; i++) {
            final UpdateQuotedItemRequest item = new UpdateQuotedItemRequest();
            Product product = createAndSaveProduct();
            item.setCode(product.getCode());
            item.setQuantity((i + 1) * 10d);
            item.setSuppliers(new ArrayList<>());
            item.setUnit(product.getUnit().getId());

            for (int j = 0; j < 2; j++) {
                final var supplier = new UpdateSupplierItemQuotedRequest();
                supplier.setSupplierId(createAndSaveSupplier().getId());
                supplier.setQuantity(item.getQuantity());
                supplier.setPrice(BigDecimal.valueOf(150 * getRandomId() + 1));
                supplier.setTotal(supplier.getPrice().multiply(BigDecimal.valueOf(supplier.getQuantity())));
                supplier.setIsSelected(Boolean.FALSE);

                item.getSuppliers().add(supplier);
            }
            item.getSuppliers().get(0).setIsSelected(Boolean.TRUE);

            request.getProducts().add(item);
        }

        return request;
    }

    private UpdateQuotationRequest createEmptyProductsUpdateQuotation() {
        UpdateQuotationRequest request = new UpdateQuotationRequest();
        request.setNote("Observacao de insercao " + getRandomId());
        request.setProducts(new ArrayList<>());
        request.setId(quotationTest.getId());
        request.setBranchOffice(createAndSaveBranchOffice().getId());

        return request;
    }

}
