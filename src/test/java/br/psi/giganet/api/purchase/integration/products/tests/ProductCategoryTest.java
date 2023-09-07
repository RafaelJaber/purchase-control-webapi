package br.psi.giganet.api.purchase.integration.products.tests;

import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.products.annotations.RoleTestProductsRead;
import br.psi.giganet.api.purchase.integration.products.annotations.RoleTestProductsWrite;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.products.categories.controller.request.CategoryRequest;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductCategoryTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private Category categoryTest;

    @Autowired
    public ProductCategoryTest(
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            ProductCategoryRepository productCategoryRepository) {

        this.productCategoryRepository = productCategoryRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;

        categoryTest = createAndSaveCategory();
    }

    @RoleTestRoot
    public void findAll() throws Exception {
        this.mockMvc.perform(get("/products/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[]").description("Lista de categorias encontradas"))
                                .andWithPrefix("[].",
                                        fieldWithPath("id").description("Código da categoria"),
                                        fieldWithPath("name").description("Nome"),
                                        fieldWithPath("pattern").description("Padrão de código o qual representa a categoria"),
                                        fieldWithPath("description").description("Descrição da categoria"))
                ));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/products/categories/{id}", categoryTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da categoria buscada")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código da categoria"),
                                fieldWithPath("name").description("Nome"),
                                fieldWithPath("pattern").description("Padrão de código o qual representa a categoria"),
                                fieldWithPath("description").description("Descrição da categoria")
                        )
                ));
    }

    @RoleTestRoot
    @Transactional
    public void insert() throws Exception {
        this.mockMvc.perform(post("/products/categories")
                .content(new ObjectMapper().writeValueAsString(createCategoryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
                                fieldWithPath("pattern").description(
                                        createDescriptionWithNotEmpty("Padrão da categoria",
                                                "Os campos a serem substituidos devem ser informados com X")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Descrição da categoria"))
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código da categoria"),
                                fieldWithPath("name").description("Nome da categoria"),
                                fieldWithPath("pattern").description("Padrão de código o qual representa a categoria"),
                                fieldWithPath("description").description("Descrição da categoria")
                        )));
    }

    @RoleTestRoot
    @Transactional
    public void update() throws Exception {
        this.mockMvc.perform(put("/products/categories/{id}", categoryTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCategoryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da categoria a ser atualizada")
                        ),
                        requestFields(
                                fieldWithPath("name").description(
                                        createDescriptionWithNotEmpty("Nome")),
                                fieldWithPath("pattern").description(
                                        createDescriptionWithNotEmpty("Padrão da categoria",
                                                "Os campos a serem substituidos devem ser informados com X")),
                                fieldWithPath("description")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                                        .description(createDescription("Descrição da categoria"))
                        ),
                        responseFields(
                                fieldWithPath("id").description("Código da categoria"),
                                fieldWithPath("name").description("Nome da categoria"),
                                fieldWithPath("pattern").description("Padrão de código o qual representa a categoria"),
                                fieldWithPath("description").description("Descrição da categoria")
                        )));
    }

    @Override
    @RoleTestProductsRead
    public void readAuthorized() throws Exception {
        this.mockMvc.perform(get("/products/categories"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/products/categories/{id}", categoryTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        this.mockMvc.perform(get("/products/categories"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/products/categories/{id}", categoryTest.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestProductsWrite
    public void writeAuthorized() throws Exception {
        this.mockMvc.perform(post("/products/categories")
                .content(new ObjectMapper().writeValueAsString(createCategoryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        this.mockMvc.perform(put("/products/categories/{id}", categoryTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCategoryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        this.mockMvc.perform(post("/products/categories")
                .content(new ObjectMapper().writeValueAsString(createCategoryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(put("/products/categories/{id}", categoryTest.getId())
                .content(new ObjectMapper().writeValueAsString(createCategoryRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private CategoryRequest createCategoryRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Categoria");
        request.setPattern("1XXX");
        request.setDescription("Descrição de teste");
        return request;
    }

}
