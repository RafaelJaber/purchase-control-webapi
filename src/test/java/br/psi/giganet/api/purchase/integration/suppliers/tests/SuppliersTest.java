package br.psi.giganet.api.purchase.integration.suppliers.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.suppliers.annotations.RoleTestSuppliersRead;
import br.psi.giganet.api.purchase.integration.suppliers.annotations.RoleTestSuppliersWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.integration.utils.messages.Messages;
import br.psi.giganet.api.purchase.suppliers.controller.request.SupplierAddress;
import br.psi.giganet.api.purchase.suppliers.controller.request.SupplierRequest;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SuppliersTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Supplier supplierTest;

    @Autowired
    public SuppliersTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            SupplierRepository supplierRepository,
            TaxRepository taxRepository) {

        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.supplierRepository = supplierRepository;
        this.taxRepository = taxRepository;
        createCurrentUser();

        supplierTest = createAndSaveSupplier();
    }

    @RoleTestRoot
    public void findByName() throws Exception {
        this.mockMvc.perform(get("/suppliers")
                .param("name", supplierTest.getName().substring(0, 3))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(String.join(". ",
                                                        "Nome do fornecedor a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription("Pagina desejada", "Valor default: 0")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription("Tamanho da pagina desejada", "Valor default: 100"))),
                                responseFields(
                                        fieldWithPath("content").description("Lista com todos os fornecedores encontrados referentes aos filtros"),
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
                                                fieldWithPath("id").description("Código do fornecedor"),
                                                fieldWithPath("name").description("Nome do fornecedor"),
                                                fieldWithPath("email").description("Email para contato"))));
    }

    @RoleTestRoot
    public void findByNameWithCPFAndCNPJ() throws Exception {
        this.mockMvc.perform(get("/suppliers")
                .param("name", supplierTest.getName().substring(0, 3))
                .param("withCPFAndCNPJ", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("name")
                                                .optional()
                                                .description(String.join(". ",
                                                        "Nome do fornecedor a ser filtrado",
                                                        "Valor default: \"\"")),
                                        parameterWithName("withCPFAndCNPJ")
                                                .optional()
                                                .description(createDescription("Flag que indica que o tipo de retorno")),
                                        parameterWithName("page")
                                                .optional()
                                                .description(createDescription("Pagina desejada", "Valor default: 0")),
                                        parameterWithName("pageSize")
                                                .optional()
                                                .description(createDescription("Tamanho da pagina desejada", "Valor default: 100"))),
                                responseFields(
                                        fieldWithPath("content").description("Lista com todos os fornecedores encontrados referentes aos filtros"),
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
                                                fieldWithPath("id").description("Código do fornecedor"),
                                                fieldWithPath("name").description("Nome do fornecedor"),
                                                fieldWithPath("cpf").optional().type(JsonFieldType.STRING).description("CPF do fornecedor, caso exista"),
                                                fieldWithPath("cnpj").optional().type(JsonFieldType.STRING).description("CNPJ do fornecedor, caso exista"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/suppliers/{id}", supplierTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do fornecedor buscado")),
                        getResponse()));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/suppliers")
                .content(new ObjectMapper().writeValueAsString(createSupplierRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        getRequest(),
                        getProjection()));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/suppliers/{id}", supplierTest.getId())
                .content(new ObjectMapper().writeValueAsString(createSupplierRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do fornecedor a ser atualizado")),
                        getRequest(),
                        getProjection()));
    }

    @Override
    @RoleTestSuppliersRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/suppliers/{id}", supplierTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/suppliers")
                .param("name", supplierTest.getName().substring(0, 3)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/suppliers")
                .param("name", supplierTest.getName().substring(0, 3))
                .param("withCPFAndCNPJ", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())));
    }

    @Override
    @RoleTestSuppliersWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/suppliers")
                .content(new ObjectMapper().writeValueAsString(createSupplierRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/suppliers/{id}", supplierTest.getId())
                .content(new ObjectMapper().writeValueAsString(createSupplierRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/suppliers/{id}", supplierTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/suppliers")
                .param("name", supplierTest.getName().substring(0, 3)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/suppliers")
                .param("name", supplierTest.getName().substring(0, 3))
                .param("withCPFAndCNPJ", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/suppliers")
                .content(new ObjectMapper().writeValueAsString(createSupplierRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/suppliers/{id}", supplierTest.getId())
                .content(new ObjectMapper().writeValueAsString(createSupplierRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private RequestFieldsSnippet getRequest() {
        return requestFields(
                fieldWithPath("name").description(
                        createDescriptionWithNotEmpty("Nome")),
                fieldWithPath("email").description(
                        createDescription(
                                "Email de contato",
                                Messages.NOT_EMPTY.getMessage(),
                                Messages.EMAIL.getMessage())),
                fieldWithPath("cnpj").description(
                        createDescription(
                                "CNPJ do fornecedor, em caso de pessoa jurídica")),
                fieldWithPath("cpf").description(
                        createDescription(
                                "CPF do fornecedor, em caso de pessoa física")),
                fieldWithPath("municipalRegistration")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description(
                                createDescription("Inscrição Municipal")),
                fieldWithPath("stateRegistration")
                        .optional()
                        .type(JsonFieldType.STRING)
                        .description(createDescription("Inscrição Estadual")),
                fieldWithPath("cellphone").description(
                        createDescriptionWithNotEmpty("Celular de contato")),
                fieldWithPath("telephone")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Telefone de contato"),
                fieldWithPath("description")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .description("Uma breve descrição do produto"),
                fieldWithPath("address")
                        .type(JsonFieldType.OBJECT)
                        .description(createDescriptionWithNotNull("Endereço")))
                .andWithPrefix("address.",
                        fieldWithPath("street").description(
                                createDescriptionWithNotEmpty("Rua")),
                        fieldWithPath("postalCode").description(
                                createDescriptionWithNotEmpty("CEP", "Deve ser informado apenas os 8 números")),
                        fieldWithPath("complement")
                                .optional()
                                .type(JsonFieldType.STRING)
                                .description("Complemento"),
                        fieldWithPath("number").description(
                                createDescriptionWithNotEmpty("Número")),
                        fieldWithPath("district").description(
                                createDescriptionWithNotEmpty("Bairro")),
                        fieldWithPath("city").description(
                                createDescriptionWithNotEmpty("Cidade")),
                        fieldWithPath("state").description(
                                createDescriptionWithNotEmpty("Estado")));
    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do fornecedor"),
                fieldWithPath("tax").description("Impostos relacionados ao fornecedor"),
                fieldWithPath("name").description("Nome"),
                fieldWithPath("email").description("Email de contato"),
                fieldWithPath("cnpj").description("CNPJ do fornecedor, em caso de pessoa jurídica"),
                fieldWithPath("cpf").description("CPF do fornecedor, em caso de pessoa física"),
                fieldWithPath("municipalRegistration").optional()
                        .type(JsonFieldType.STRING)
                        .description(createDescription("Inscrição Municipal")),
                fieldWithPath("stateRegistration").type(JsonFieldType.STRING)
                        .optional()
                        .description(createDescription("Inscrição Estadual")),
                fieldWithPath("cellphone").description("Celular de contato"),
                fieldWithPath("telephone").description("Telefone de contato"),
                fieldWithPath("description").type(JsonFieldType.STRING)
                        .optional()
                        .description("Uma breve descrição do produto"),
                fieldWithPath("address").type(JsonFieldType.OBJECT)
                        .description("Endereço"))
                .andWithPrefix("address.",
                        fieldWithPath("complement").description("Complemento"),
                        fieldWithPath("postalCode").description("CEP"),
                        fieldWithPath("street").description("Rua"),
                        fieldWithPath("number").description("Número"),
                        fieldWithPath("district").description("Bairro"),
                        fieldWithPath("city").description("Cidade"),
                        fieldWithPath("state").description("Estado"))
                .andWithPrefix("tax.",
                        fieldWithPath("id").description("Código do relacionamento"),
                        fieldWithPath("to").description("Estado de destino"),
                        fieldWithPath("from").description("Estado de origem"),
                        fieldWithPath("icms").description("ICMS associado a operação"));
    }

    private ResponseFieldsSnippet getProjection() {
        return responseFields(
                fieldWithPath("id").description("Código do fornecedor"),
                fieldWithPath("name").description("Nome do fornecedor"));
    }

    private SupplierRequest createSupplierRequest() {
        SupplierRequest request = new SupplierRequest();
        request.setName("Fornecedor");
        request.setEmail("email@email.com");
        request.setCellphone("31987746818");
        request.setTelephone("3138268975");
        request.setCnpj("55191816000131");
        request.setStateRegistration("3130010611100");
        request.setMunicipalRegistration("55191816000131");
        request.setDescription("Descrição de teste");
        request.setAddress(new SupplierAddress());
        request.getAddress().setStreet("Rua teste");
        request.getAddress().setNumber("120");
        request.getAddress().setDistrict("Horto");
        request.getAddress().setCity("Ipatinga");
        request.getAddress().setState("MG");
        request.getAddress().setPostalCode("35162201");
        request.getAddress().setComplement("Complement");
        return request;
    }
}
