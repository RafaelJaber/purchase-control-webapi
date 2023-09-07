package br.psi.giganet.api.purchase.integration.purchase_requests.tests;

import br.psi.giganet.api.purchase.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.purchase_requests.annotations.RoleTestPurchaseRequestWrite;
import br.psi.giganet.api.purchase.integration.purchase_requests.annotations.RoleTestPurchaseRequestsRead;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.integration.utils.messages.Messages;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.InsertPurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.InsertPurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.UpdatePurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.controller.request.UpdatePurchaseRequestItem;
import br.psi.giganet.api.purchase.purchase_requests.model.PurchaseRequest;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PurchaseRequestTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private PurchaseRequest purchaseRequestTest;

    @Autowired
    public PurchaseRequestTest(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PurchaseRequestRepository purchaseRequestRepository,
            UnitRepository unitRepository,
            CostCenterRepository costCenterRepository,
            ProductCategoryRepository productCategoryRepository,
            TaxRepository taxRepository,
            BranchOfficeRepository branchOfficeRepository) {

        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.unitRepository = unitRepository;
        this.costCenterRepository = costCenterRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.taxRepository = taxRepository;
        this.branchOfficeRepository = branchOfficeRepository;

        createCurrentUser();

        purchaseRequestTest = createAndSavePurchaseRequest();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/purchase-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todas as solicitações de compra em ordem decrescente pela data de cadastro"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código da solicitação de compra"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("branchOffice").description("Filial associada"),
                                                fieldWithPath("createdDate").description("Data de criação da solicitação"),
                                                fieldWithPath("dateOfNeed").description("Data de necessidade"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("status").description("Status atual da solicitação"))
                                        .andWithPrefix("[].requester.",
                                                fieldWithPath("id").description("Código do solicitante"),
                                                fieldWithPath("name").description("Nome do solicitante"))
                                        .andWithPrefix("[].branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))));
    }

    @RoleTestRoot
    public void findItemsByName() throws Exception {
        this.mockMvc.perform(get("/purchase-requests/items")
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
                                                fieldWithPath("purchaseRequest").description("Solicitação associada"),
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

                                        .andWithPrefix("content[].purchaseRequest.",
                                                fieldWithPath("id").description("Código da solicitação de compra"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("branchOffice").description("Filial associada"),
                                                fieldWithPath("createdDate").description("Data de criação da solicitação"),
                                                fieldWithPath("dateOfNeed").description("Data de necessidade"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("status").description("Status atual da solicitação"))
                                        .andWithPrefix("content[].purchaseRequest.requester.",
                                                fieldWithPath("id").description("Código do solicitante"),
                                                fieldWithPath("name").description("Nome do solicitante"))
                                        .andWithPrefix("content[].purchaseRequest.branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))

                                        .andWithPrefix("content[].lastStage.",
                                                fieldWithPath("id").description("Código do respectivo estágio associado"),
                                                fieldWithPath("stage").description(
                                                        createDescription("Nome do estágio.",
                                                                "Pode ser: 'PURCHASE_ORDER', 'QUOTATION_APPROVAL', 'QUOTATION', 'PURCHASE_REQUEST_APPROVAL' ou 'PURCHASE_REQUEST' ")
                                                ),
                                                fieldWithPath("lastModifiedDate").description("Data da ultima modificação do objeto referente ao estado"),
                                                fieldWithPath("status").description("Status do objeto referente ao estado"))));
    }

    @RoleTestRoot
    public void findByStatus() throws Exception {
        this.mockMvc.perform(get("/purchase-requests/statuses")
                .param("status", ProcessStatus.PENDING.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("status")
                                                .description(String.join(". ",
                                                        "Status pesquisado",
                                                        Messages.NOT_NULL.getMessage()))),
                                responseFields(fieldWithPath("[]")
                                        .description("Lista de todas as solicitações de compra em ordem decrescente pela data de cadastro filtrados pelo status"))
                                        .andWithPrefix("[].",
                                                fieldWithPath("id").description("Código da solicitação de compra"),
                                                fieldWithPath("requester").description("Nome do solicitante"),
                                                fieldWithPath("branchOffice").description("Filial associada"),
                                                fieldWithPath("createdDate").description("Data de criação da solicitação"),
                                                fieldWithPath("dateOfNeed").description("Data de necessidade"),
                                                fieldWithPath("description")
                                                        .optional()
                                                        .type(JsonFieldType.STRING)
                                                        .description(createDescription("Descrição da solicitação, caso exista")),
                                                fieldWithPath("status").description("Status atual da solicitação"))
                                        .andWithPrefix("[].requester.",
                                                fieldWithPath("id").description("Código do solicitante"),
                                                fieldWithPath("name").description("Nome do solicitante"))
                                        .andWithPrefix("[].branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/purchase-requests/{id}", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(String.join(". ",
                                                "Código da solicitação procurada",
                                                Messages.NOT_NULL.getMessage()))),
                                getPurchaseRequestResponse()));
    }


    @RoleTestRoot
    public void getPurchaseRequestReport() throws Exception {
        this.mockMvc.perform(get("/purchase-requests/reports/{id}", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description(
                                                createDescriptionWithNotNull("Código da solicitação de compra procurada")))));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createValidInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("responsible").description(createDescriptionWithNotNull("Código do responsável pela solicitação")),
                                        fieldWithPath("reason").description(createDescriptionWithNotEmpty("Motivo da solicitação")),
                                        fieldWithPath("description")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Descrição da solicitação, caso exista")),
                                        fieldWithPath("costCenter").description(createDescriptionWithNotNull("Centro de custo")),
                                        fieldWithPath("branchOffice").description(createDescriptionWithNotNull("Filial associada")),
                                        fieldWithPath("dateOfNeed")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription(
                                                        "Data de necessidade, caso exista, no formato \"YYYY-MM-DDThh:mm:ss.SSSZ\". Exemplo: " +
                                                                ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString() + " (ISO 8601)")),
                                        fieldWithPath("note")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Observações, caso existam")),
                                        fieldWithPath("products").description(createDescriptionWithNotNull("Lista de produtos solicitados"))).
                                        andWithPrefix("products[].",
                                                fieldWithPath("product").description(createDescriptionWithNotNull("Código do produto, gerado pelo banco de dados")),
                                                fieldWithPath("unit").description(createDescriptionWithNotNull("Unidade desejada")),
                                                fieldWithPath("quantity")
                                                        .description(createDescription(
                                                                "Quantidade solicitada para o respectivo produto",
                                                                Messages.NOT_NULL.getMessage(),
                                                                Messages.POSITIVE_NUMBER.getMessage()))),
                                responseFields(
                                        fieldWithPath("id").description("Código da solicitação de compra"),
                                        fieldWithPath("requester").description("Nome do solicitante"),
                                        fieldWithPath("branchOffice").description("Filial associada"),
                                        fieldWithPath("createdDate").description("Data de criação da solicitação"),
                                        fieldWithPath("dateOfNeed")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description("Data de necessidade"),
                                        fieldWithPath("description")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Descrição da solicitação, caso exista")),
                                        fieldWithPath("status").description("Status atual da solicitação"))
                                        .andWithPrefix("requester.",
                                                fieldWithPath("id").description("Código do solicitante"),
                                                fieldWithPath("name").description("Nome do solicitante"))
                                        .andWithPrefix("branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da solicitação de compra a ser alterada")
                                ),
                                requestFields(
                                        fieldWithPath("responsible")
                                                .description(createDescription("Código do responsável pela solicitação")),
                                        fieldWithPath("reason")
                                                .description(createDescription("Motivo da solicitação")),
                                        fieldWithPath("costCenter")
                                                .description(createDescription("Centro de custo")),
                                        fieldWithPath("branchOffice")
                                                .description(createDescription("Filial associada")),
                                        fieldWithPath("dateOfNeed")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription(
                                                        "Data de necessidade, caso exista no formato \"YYYY-MM-DDThh:mm:ss.SSSZ\". Exemplo: " +
                                                                ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString() + " (ISO 8601)")),
                                        fieldWithPath("note")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Observações, caso existam")),
                                        fieldWithPath("description")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Descrição da solicitação, caso exista")),
                                        fieldWithPath("products")
                                                .description(createDescriptionWithNotNull(
                                                        "Lista de produtos solicitados. Os produtos anteriores serão substituídos ou atualizados " +
                                                                "pelos produtos presentes nesta lista")))
                                        .andWithPrefix("products[].",
                                                fieldWithPath("id")
                                                        .optional()
                                                        .type(JsonFieldType.NUMBER)
                                                        .description(createDescription(
                                                                "Código do respectivo relacionamento entre o produto e a " +
                                                                        "solicitação de compra")),
                                                fieldWithPath("product")
                                                        .description(createDescriptionWithNotNull("Código do produto, gerado pelo banco de dados")),
                                                fieldWithPath("unit")
                                                        .description(createDescriptionWithNotNull("Unidade desejada")),
                                                fieldWithPath("quantity")
                                                        .description(createDescription(
                                                                "Quantidade solicitada para o respectivo produto",
                                                                Messages.NOT_NULL.getMessage(),
                                                                Messages.POSITIVE_NUMBER.getMessage()))),
                                getPurchaseRequestResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void sendPurchaseRequestToApproval() throws Exception {
        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da solicitação de compra a ser encaminhada para aprovação")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("Código da solicitação de compra"),
                                        fieldWithPath("requester").description("Nome do solicitante"),
                                        fieldWithPath("createdDate").description("Data de criação da solicitação"),
                                        fieldWithPath("dateOfNeed").description("Data de necessidade"),
                                        fieldWithPath("description")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Descrição da solicitação, caso exista")),
                                        fieldWithPath("status").description("Status atual da solicitação"))
                                        .andWithPrefix("requester.",
                                                fieldWithPath("id").description("Código do solicitante"),
                                                fieldWithPath("name").description("Nome do solicitante"))
                                        .andWithPrefix("branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))));
    }

    @RoleTestRoot
    @Transactional
    public void cancelPurchaseRequest() throws Exception {
        this.mockMvc.perform(post("/purchase-requests/{id}/canceled", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", Matchers.is(ProcessStatus.CANCELED.name())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("Código da solicitação de compra a ser cancelada")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("Código da solicitação de compra"),
                                        fieldWithPath("requester").description("Nome do solicitante"),
                                        fieldWithPath("createdDate").description("Data de criação da solicitação"),
                                        fieldWithPath("dateOfNeed").description("Data de necessidade"),
                                        fieldWithPath("description")
                                                .optional()
                                                .type(JsonFieldType.STRING)
                                                .description(createDescription("Descrição da solicitação, caso exista")),
                                        fieldWithPath("status").description("Status atual da solicitação"))
                                        .andWithPrefix("requester.",
                                                fieldWithPath("id").description("Código do solicitante"),
                                                fieldWithPath("name").description("Nome do solicitante"))
                                        .andWithPrefix("branchOffice.",
                                                fieldWithPath("id").description("Código da filial"),
                                                fieldWithPath("name").description("Nome da filial"),
                                                fieldWithPath("shortName").description("Nome abreviado da filial"))));
    }

    @Override
    @RoleTestPurchaseRequestsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/purchase-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-requests/{id}", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-requests/statuses")
                .param("status", ProcessStatus.PENDING.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-requests/reports/{id}", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/purchase-requests/items")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "teste_read_all@teste.com", authorities = {"ROLE_PURCHASE_REQUESTS_READ_ALL", "ROLE_ADMIN"})
    public void findAllUsingOnlyReadAllPurchaseRequestsPermission() throws Exception {
        Employee e = createAndSaveEmployee("teste_read_all@teste.com");
        e.getPermissions().clear();
        e.getPermissions().add(createAndSavePermission("ROLE_ADMIN"));
        e.getPermissions().add(createAndSavePermission("ROLE_PURCHASE_REQUESTS_READ_ALL"));
        employeeRepository.save(e);

        this.mockMvc.perform(get("/purchase-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestPurchaseRequestWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createValidInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/purchase-requests/{id}/canceled", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/purchase-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-requests/{id}", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-requests/statuses")
                .param("status", ProcessStatus.PENDING.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-requests/reports/{id}", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_PDF))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/purchase-requests/items")
                .param("name", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createValidInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/purchase-requests/{id}/approvals", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/purchase-requests/{id}/canceled", purchaseRequestTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @RoleTestRoot
    @Transactional
    public void invalidInsert() throws Exception {
        this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createEmptyProductsInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(post("/purchase-requests")
                .content(objectMapper.writeValueAsString(createInvalidUnitInsertPurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));
    }

    @RoleTestRoot
    @Transactional
    public void invalidUpdate() throws Exception {
        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createEmptyProductsUpdatePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createInvalidResponsibleUpdatePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createInvalidUnitUpdatePurchaseRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", Matchers.not(Matchers.empty())));
    }

    @RoleTestRoot
    @Transactional
    public void invalidUpdateStatusesItems() throws Exception {
        final var purchaseRequestTest = createAndSavePurchaseRequest();
        purchaseRequestTest.setStatus(ProcessStatus.PARTIALLY_APPROVED);
        purchaseRequestTest.getItems().get(0).setStatus(ProcessStatus.APPROVED);
        purchaseRequestTest.getItems().get(1).setStatus(ProcessStatus.APPROVED);
        purchaseRequestTest.getItems().get(2).setStatus(ProcessStatus.REJECTED);
        purchaseRequestRepository.save(purchaseRequestTest);

        this.mockMvc.perform(put("/purchase-requests/{id}", purchaseRequestTest.getId())
                .content(objectMapper.writeValueAsString(createValidUpdatePurchaseRequest(purchaseRequestTest)))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[?(@.id == " + purchaseRequestTest.getItems().get(0).getId() + ")].status",
                        Matchers.hasItem(ProcessStatus.APPROVED.toString())))
                .andExpect(jsonPath("$.products[?(@.id == " + purchaseRequestTest.getItems().get(1).getId() + ")].status",
                        Matchers.hasItem(ProcessStatus.APPROVED.toString())))
                .andExpect(jsonPath("$.products[?(@.id == " + purchaseRequestTest.getItems().get(2).getId() + ")].status",
                        Matchers.hasItem(ProcessStatus.REJECTED.toString())));
    }

    /**
     * Description: Create a valid insert purchase
     * <br>
     * Category: VALID
     */
    private InsertPurchaseRequest createValidInsertPurchaseRequest() {
        InsertPurchaseRequest request = new InsertPurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(
                productRepository.findAll()
                        .stream()
                        .map(product -> new InsertPurchaseRequestItem(
                                product.getId(),
                                getRandomId() * 1.5,
                                product.getUnit().getId()))
                        .collect(Collectors.toList())
        );
        return request;
    }

    /**
     * Description: Create an invalid insert purchase. The items dont have a valid unit
     * <br>
     * Category: INVALID
     */
    private InsertPurchaseRequest createInvalidUnitInsertPurchaseRequest() {
        InsertPurchaseRequest request = new InsertPurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(
                productRepository.findAll()
                        .stream()
                        .map(product -> new InsertPurchaseRequestItem(
                                product.getId(),
                                getRandomId() * 1.5,
                                (long) (getRandomId() * (Math.random() > 0.5 ? 1 : -1))))
                        .collect(Collectors.toList())
        );
        return request;
    }

    /**
     * Description: Create an invalid purchase request. The requested products is empty
     * <br>
     * Category: INVALID
     */
    private InsertPurchaseRequest createEmptyProductsInsertPurchaseRequest() {
        InsertPurchaseRequest request = new InsertPurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(Collections.emptyList());
        return request;
    }

    /**
     * Description: Create a valid update purchase request. It is used alone
     * <br>
     * Category: VALID
     */
    private UpdatePurchaseRequest createValidUpdatePurchaseRequest() {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(
                purchaseRequestTest
                        .getItems()
                        .stream()
                        .map(item -> new UpdatePurchaseRequestItem(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity() + 10,
                                item.getProduct().getUnit().getId()))
                        .collect(Collectors.toList()));
        request.getProducts().addAll(
                productRepository.findAll()
                        .subList(0, 2)
                        .stream()
                        .map(product -> new UpdatePurchaseRequestItem(
                                null,
                                product.getId(),
                                getRandomId() * 1.5,
                                product.getUnit().getId()))
                        .collect(Collectors.toList())
        );
        return request;
    }

    /**
     * Description:  Create an invalid update purchase. The items dont have a valid unit
     * <br>
     * Category: INVALID
     */
    private UpdatePurchaseRequest createInvalidUnitUpdatePurchaseRequest() {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(
                purchaseRequestTest
                        .getItems()
                        .stream()
                        .map(item -> new UpdatePurchaseRequestItem(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity() + 10,
                                (long) (getRandomId() * (Math.random() > 0.5 ? 100 : -1))))
                        .collect(Collectors.toList()));
        request.getProducts().addAll(
                productRepository.findAll()
                        .stream()
                        .map(product -> new UpdatePurchaseRequestItem(
                                null,
                                product.getId(),
                                getRandomId() * 1.5,
                                null))
                        .collect(Collectors.toList())
        );
        return request;
    }

    /**
     * Description: Create a valid update purchase request, but is used to test not allowed updates because of status.
     * <br>
     * Category: VALID
     *
     * @params purchaseRequestTest: the purchase request to test using its products
     */
    private UpdatePurchaseRequest createValidUpdatePurchaseRequest(final PurchaseRequest purchaseRequestTest) {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(
                purchaseRequestTest
                        .getItems()
                        .stream()
                        .map(item -> new UpdatePurchaseRequestItem(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity() + 10,
                                item.getUnit().getId()))
                        .collect(Collectors.toList()));
        request.getProducts().addAll(
                productRepository.findAll()
                        .stream()
                        .map(product -> new UpdatePurchaseRequestItem(
                                null, product.getId(),
                                getRandomId() * 1.5,
                                product.getUnit().getId()))
                        .collect(Collectors.toList())
        );
        return request;
    }

    /**
     * Description: Create an invalid update purchase request with empty products list
     * <br>
     * Category: INVALID
     */
    private UpdatePurchaseRequest createEmptyProductsUpdatePurchaseRequest() {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest();
        request.setResponsible(purchaseRequestTest.getResponsible().getId());
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(new ArrayList<>());
        return request;
    }

    /**
     * Description: Create an invalid update purchase request with invalid responsible
     * <br>
     * Category: INVALID
     */
    private UpdatePurchaseRequest createInvalidResponsibleUpdatePurchaseRequest() {
        UpdatePurchaseRequest request = new UpdatePurchaseRequest();
        request.setResponsible(-1L);
        request.setDateOfNeed(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toString());
        request.setReason("Reposição de estoque");
        request.setCostCenter(purchaseRequestTest.getCostCenter().getId());
        request.setBranchOffice(purchaseRequestTest.getBranchOffice().getId());
        request.setProducts(
                purchaseRequestTest
                        .getItems()
                        .stream()
                        .map(item -> new UpdatePurchaseRequestItem(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity() + 10,
                                item.getUnit().getId()))
                        .collect(Collectors.toList()));
        request.getProducts().addAll(
                productRepository.findAll()
                        .stream()
                        .map(product -> new UpdatePurchaseRequestItem(
                                null,
                                product.getId(),
                                getRandomId() * 1.5,
                                product.getUnit().getId()))
                        .collect(Collectors.toList())
        );
        return request;
    }

    private ResponseFieldsSnippet getPurchaseRequestResponse() {
        return responseFields(
                fieldWithPath("id").description("Código da solicitação de compra"),
                fieldWithPath("requester").type(JsonFieldType.OBJECT).description("Solicitante"),
                fieldWithPath("responsible").type(JsonFieldType.OBJECT).description("Responsável pela solicitação"),
                fieldWithPath("branchOffice").description("Filial associada"),
                fieldWithPath("dateOfNeed").description("Data de necessidade"),
                fieldWithPath("reason").description("Motivo da solicitação"),
                fieldWithPath("note").description("Observação"),
                fieldWithPath("description").optional().type(JsonFieldType.STRING).description(createDescription("Descrição da solicitação, caso exista")),
                fieldWithPath("status").description("Status atual da solicitação"),
                fieldWithPath("costCenter").description("Centro de custo do solicitação"),
                fieldWithPath("products").description("Lista de produtos solicitados"))
                .andWithPrefix("requester.",
                        fieldWithPath("id").description("Código do solicitante"),
                        fieldWithPath("name").description("Nome do solicitante"))
                .andWithPrefix("responsible.",
                        fieldWithPath("id").description("Código do responsável"),
                        fieldWithPath("name").description("Nome do responsável"))
                .andWithPrefix("products[].",
                        fieldWithPath("id").description("Código do relacionamento entre produto e solicitação"),
                        fieldWithPath("product").description("Produto solicitado"),
                        fieldWithPath("quantity").description("Quantidade solicitada"),
                        fieldWithPath("unit").description("Unidade solicitada"),
                        fieldWithPath("status").description("Status de aprovação do produto"))
                .andWithPrefix("products[].product.",
                        fieldWithPath("id").description("Código do produto oriundo do banco de dados"),
                        fieldWithPath("code").description("Código de identificação do produto como por exemplo código serial"),
                        fieldWithPath("name").description("Nome do produto"))
                .andWithPrefix("products[].unit.",
                        fieldWithPath("id").description("Código da unidade"),
                        fieldWithPath("abbreviation").description("Abreviação do nome da unidade"),
                        fieldWithPath("name").description("Nome da unidade"))
                .andWithPrefix("costCenter.",
                        fieldWithPath("id").description("Código do centro de custo"),
                        fieldWithPath("name").description("Nome do centro de custo"),
                        fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Descrição do centro de custo"))
                .andWithPrefix("branchOffice.",
                        fieldWithPath("id").description("Código da filial"),
                        fieldWithPath("name").description("Nome da filial"),
                        fieldWithPath("shortName").description("Nome abreviado da filial"));
    }

}
