package br.psi.giganet.api.purchase.integration.products.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.products.annotations.RoleTestProductsRead;
import br.psi.giganet.api.purchase.integration.products.annotations.RoleTestProductsWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.controller.request.ProductRequest;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductsTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Product productTest;

    @Autowired
    public ProductsTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            UnitRepository unitRepository) {

        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.unitRepository = unitRepository;
        createCurrentUser();

        productTest = createAndSaveProduct();
    }

    @RoleTestRoot
    public void findByNameContaining() throws Exception {
        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3))
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
                                        fieldWithPath("content").description("Lista com todos os produtos encontrados referentes aos filtros"),
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
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].unit.",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void findByNameAndCodeContaining() throws Exception {
        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3))
                .param("code", productTest.getCode().substring(0, 3))
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
                                        parameterWithName("code")
                                                .optional()
                                                .description(createDescription(
                                                        "Codigo do produto a ser filtrado",
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
                                        fieldWithPath("content").description("Lista com todos os produtos encontrados referentes aos filtros"),
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
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].unit.",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void findByCodeContaining() throws Exception {
        this.mockMvc.perform(get("/products")
                .param("code", productTest.getCode().substring(0, 3))
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
                                        parameterWithName("code")
                                                .optional()
                                                .description(createDescription(
                                                        "Codigo do produto a ser filtrado",
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
                                        fieldWithPath("content").description("Lista com todos os produtos encontrados referentes aos filtros"),
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
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].unit.",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void findProductWithUnitsByName() throws Exception {
        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3))
                .param("withUnits", "")
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
                                        parameterWithName("withUnits")
                                                .description(createDescriptionWithNotEmpty(
                                                        "Parametro que determina que este endpoint seja associado",
                                                        "O valor associado é irrelevante, mas é necessário o parametro estar na URL")),
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
                                        fieldWithPath("content").description("Lista com todos os produtos encontrados referentes aos filtros"),
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
                                                fieldWithPath("product").description("Produto encontrado"),
                                                fieldWithPath("availableUnits[]").description("Lista com todas as unidades possíveis associadas ao produto em questão"))
                                        .andWithPrefix("content[].product.",
                                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                                fieldWithPath("name").description("Nome do produto"),
                                                fieldWithPath("code").description("Código interno do produto"),
                                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                                fieldWithPath("unit").description("Unidade padrão"))
                                        .andWithPrefix("content[].product.unit.",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))
                                        .andWithPrefix("content[].availableUnits[].",
                                                fieldWithPath("id").description("Código da unidade"),
                                                fieldWithPath("name").description("Nome da unidade"),
                                                fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/products/{id}", productTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do produto buscado, gerado pelo banco de dados")
                        ),
                        getProductResponse()));
    }

    @RoleTestRoot
    public void generateByCategory() throws Exception {
        Product product = createAndSaveProduct();
        product.setCode("1010");
        productRepository.saveAndFlush(product);

        this.mockMvc.perform(get("/products/code/generate")
                .param("category", product.getCategory().getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("category").description("Código da categoria desejada")
                        ),
                        responseFields(fieldWithPath("code")
                                .description("Código gerado para o próximo produto da categoria desejada"))));
    }

    @RoleTestRoot
    public void findByCode() throws Exception {
        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("code").description("Código do produto buscado, como por exemplo o número serial do produto")
                        ),
                        getProductResponse()));
    }

    @RoleTestRoot
    public void findProductWithUnitsByCode() throws Exception {
        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode())
                .param("withUnits", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("code").description("Código do produto buscado, como por exemplo o número serial do produto")
                        ),
                        requestParameters(
                                parameterWithName("withUnits")
                                        .description(createDescriptionWithNotEmpty(
                                                "Parametro que determina que este endpoint seja associado",
                                                "O valor associado é irrelevante, mas é necessário o parametro estar na URL"))),
                        responseFields(
                                fieldWithPath("product").description("Produto encontrado"),
                                fieldWithPath("availableUnits[]").description("Lista com todas as unidades possíveis associadas ao produto em questão"))
                                .andWithPrefix("product.",
                                        fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                        fieldWithPath("name").description("Nome do produto"),
                                        fieldWithPath("code").description("Código interno do produto"),
                                        fieldWithPath("manufacturer").description("Nome do fabricante"),
                                        fieldWithPath("unit").description("Unidade padrão"))
                                .andWithPrefix("product.unit.",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome da unidade"),
                                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))
                                .andWithPrefix("availableUnits[].",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome da unidade"),
                                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void insert() throws Exception {
        this.mockMvc.perform(post("/products")
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name")
                                        .description(createDescriptionWithNotEmpty("Nome do produto")),
                                fieldWithPath("code")
                                        .description(createDescriptionWithNotEmpty("Código interno do produto")),
                                fieldWithPath("category")
                                        .description(createDescriptionWithNotEmpty("Código da categoria do produto")),
                                fieldWithPath("unit")
                                        .description(createDescriptionWithNotEmpty("Código da unidade do produto")),
                                fieldWithPath("manufacturer")
                                        .description(createDescriptionWithNotEmpty("Nome do fabricante")),
                                fieldWithPath("description")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("Uma breve descrição do produto")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                fieldWithPath("name").description("Nome do produto"),
                                fieldWithPath("code").description("Código interno do produto"),
                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                fieldWithPath("unit").description("Unidade padrão"))
                                .andWithPrefix("unit.",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome da unidade"),
                                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void update() throws Exception {
        this.mockMvc.perform(put("/products/{id}", productTest.getId())
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(productTest.getId().intValue())))
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do produto a ser atualizado, gerado pelo banco de dados")
                        ),
                        requestFields(
                                fieldWithPath("name")
                                        .description(createDescriptionWithNotEmpty("Nome do produto")),
                                fieldWithPath("code")
                                        .description(createDescriptionWithNotEmpty("Código interno do produto")),
                                fieldWithPath("category")
                                        .description(createDescriptionWithNotNull("Código da categoria do produto")),
                                fieldWithPath("unit")
                                        .description(createDescriptionWithNotNull("Código da unidade do produto")),
                                fieldWithPath("manufacturer")
                                        .description(createDescriptionWithNotEmpty("Nome do fabricante")),
                                fieldWithPath("description")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("Uma breve descrição do produto")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                fieldWithPath("name").description("Nome do produto"),
                                fieldWithPath("code").description("Código interno do produto"),
                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                fieldWithPath("unit").description("Unidade padrão"))
                                .andWithPrefix("unit.",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome da unidade"),
                                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void basicInsert() throws Exception {
        this.mockMvc.perform(post("/basic/products")
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name")
                                        .description(createDescriptionWithNotEmpty("Nome do produto")),
                                fieldWithPath("code")
                                        .description(createDescriptionWithNotEmpty("Código interno do produto")),
                                fieldWithPath("category")
                                        .description(createDescriptionWithNotEmpty("Código da categoria do produto")),
                                fieldWithPath("unit")
                                        .description(createDescriptionWithNotEmpty("Código da unidade do produto")),
                                fieldWithPath("manufacturer")
                                        .description(createDescriptionWithNotEmpty("Nome do fabricante")),
                                fieldWithPath("description")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("Uma breve descrição do produto")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                fieldWithPath("name").description("Nome do produto"),
                                fieldWithPath("code").description("Código interno do produto"),
                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                fieldWithPath("unit").description("Unidade padrão"))
                                .andWithPrefix("unit.",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome da unidade"),
                                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void basicUpdate() throws Exception {
        this.mockMvc.perform(put("/basic/products/{id}", productTest.getId())
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(productTest.getId().intValue())))
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código do produto a ser atualizado, gerado pelo banco de dados")
                        ),
                        requestFields(
                                fieldWithPath("name")
                                        .description(createDescriptionWithNotEmpty("Nome do produto")),
                                fieldWithPath("code")
                                        .description(createDescriptionWithNotEmpty("Código interno do produto")),
                                fieldWithPath("category")
                                        .description(createDescriptionWithNotNull("Código da categoria do produto")),
                                fieldWithPath("unit")
                                        .description(createDescriptionWithNotNull("Código da unidade do produto")),
                                fieldWithPath("manufacturer")
                                        .description(createDescriptionWithNotEmpty("Nome do fabricante")),
                                fieldWithPath("description")
                                        .type(JsonFieldType.STRING)
                                        .optional()
                                        .description("Uma breve descrição do produto")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                                fieldWithPath("name").description("Nome do produto"),
                                fieldWithPath("code").description("Código interno do produto"),
                                fieldWithPath("manufacturer").description("Nome do fabricante"),
                                fieldWithPath("unit").description("Unidade padrão"))
                                .andWithPrefix("unit.",
                                        fieldWithPath("id").description("Código da unidade"),
                                        fieldWithPath("name").description("Nome da unidade"),
                                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"))));
    }

    @RoleTestRoot
    public void basicGenerateByCategory() throws Exception {
        Product product = createAndSaveProduct();
        product.setCode("20100");
        productRepository.saveAndFlush(product);

        this.mockMvc.perform(get("/basic/products/code/generate")
                .param("category", product.getCategory().getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("category").description("Código da categoria desejada")
                        ),
                        responseFields(fieldWithPath("code")
                                .description("Código gerado para o próximo produto da categoria desejada"))));
    }

    @Override
    @RoleTestProductsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/products/{id}", productTest.getId()))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode()))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3))
                .param("withUnits", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode())
                .param("withUnits", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestProductsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/products")
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/products/{id}", productTest.getId())
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(productTest.getId().intValue())));
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/products/{id}", productTest.getId()))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode()))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())));

        this.mockMvc.perform(get("/products"))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/products")
                .param("name", productTest.getName().substring(0, 3))
                .param("withUnits", "")
                .param("page", "0")
                .param("pageSize", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/products/code/{code}", productTest.getCode())
                .param("withUnits", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/products")
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/products/{id}", productTest.getId())
                .content(new ObjectMapper().writeValueAsString(createProductRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private ResponseFieldsSnippet getProductResponse() {
        return responseFields(
                fieldWithPath("id").description("Código do produto, gerado pelo banco de dados"),
                fieldWithPath("name").description("Nome do produto"),
                fieldWithPath("code").description("Código interno do produto"),
                fieldWithPath("category").description("Categoria do produto"),
                fieldWithPath("manufacturer").description("Nome do fabricante"),
                fieldWithPath("description").type(JsonFieldType.STRING)
                        .optional()
                        .description("Uma breve descrição do produto"),
                fieldWithPath("unit").description("Unidade padrão"))
                .andWithPrefix("unit.",
                        fieldWithPath("id").description("Código da unidade"),
                        fieldWithPath("name").description("Nome da unidade"),
                        fieldWithPath("abbreviation").description("Abreviação utilizada para a unidade"),
                        fieldWithPath("description").description("Descrição da unidade"),
                        fieldWithPath("conversions[]").type(JsonFieldType.ARRAY)
                                .optional()
                                .description("Lista com todas as conversões cadastradas para esta unidade"))
                .andWithPrefix("unit.conversions[].",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código do registro"),
                        fieldWithPath("to").type(JsonFieldType.OBJECT)
                                .optional()
                                .description("Unidade de destino"),
                        fieldWithPath("conversion").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Fator de conversão da presente unidade para a unidade destino. Mais informações na sessão Unidades"))
                .andWithPrefix("unit.conversions[].to.",
                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                .optional()
                                .description("Código da unidade"),
                        fieldWithPath("name").type(JsonFieldType.STRING)
                                .optional()
                                .description("Nome da unidade"),
                        fieldWithPath("abbreviation").type(JsonFieldType.STRING)
                                .optional()
                                .description("Abreviação utilizada para a unidade"))
                .andWithPrefix("category.",
                        fieldWithPath("id").description("Código da categoria"),
                        fieldWithPath("name").description("Nome da categoria"),
                        fieldWithPath("description").description("Descrição da categoria"));
    }

    private ProductRequest createProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Product");
        request.setCode(productTest.getCode() + getRandomId());
        request.setCategory(createAndSaveCategory().getId());
        request.setUnit(createAndSaveUnit().getId());
        request.setManufacturer("Fabricante");
        request.setDescription("Produto de teste");
        return request;
    }

}
