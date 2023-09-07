package br.psi.giganet.api.purchase.integration.commons;

import br.psi.giganet.api.purchase.config.filter.RemoteApiAuthenticationFilter;
import br.psi.giganet.api.purchase.config.project_property.ApplicationProperties;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.config.security.service.AbstractUserService;
import br.psi.giganet.api.purchase.config.token.filter.RefreshTokenCookiePreProcessorFilter;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationDocs extends BuilderIntegrationTest {

    @MockBean
    private AbstractUserService userService;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private RemoteApiAuthenticationFilter remoteApiAuthenticationFilter;

    @Autowired
    private RefreshTokenCookiePreProcessorFilter refreshTokenCookiePreProcessorFilter;

    @Autowired
    public AuthenticationDocs(
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
    }


    @Override
    @BeforeEach
    public void mockMvcConfig(WebApplicationContext webApplicationContext,
                              RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .addFilter(refreshTokenCookiePreProcessorFilter)
                .addFilter(springSecurityFilterChain)
                .addFilter(remoteApiAuthenticationFilter)
                .addFilter(((req, res, filterChain) -> {
                    req.setCharacterEncoding("UTF-8");
                    filterChain.doFilter(req, res);
                }))
                .build();

    }

    @Test
    @Transactional
    public void basicAuthentication() throws Exception {
        Product product = createAndSaveProduct();

        this.mockMvc.perform(get("/basic/products/code/generate")
                .param("category", product.getCategory().getId().toString())
                .with(httpBasic(
                        properties.getWebhooks().getStockApi().getName(),
                        properties.getWebhooks().getStockApi().getKey()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.not(Matchers.empty())))
                .andDo(MockMvcResultHandlers.print())
                .andDo(
                        document("{class_name}/{method_name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())));
    }

    @Test
    @Transactional
    public void login() throws Exception {
        String username = "lucas@lucas.com";
        String password = "QWERTY1234";
        Employee e = createAndSaveEmployee(username);

        Mockito.when(userService.remoteAuthenticateHandler(username, password))
                .thenReturn(Optional.of(e));

        this.mockMvc.perform(post("/oauth/token")
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
                .with(httpBasic(
                        properties.getUiCredentials().getCLIENT_ID(),
                        properties.getUiCredentials().getCLIENT_PASSWORD()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("username").description("Username (email) do usuário"),
                                parameterWithName("password").description("Senha"),
                                parameterWithName("grant_type").description(
                                        createDescription("Grant Type (OAuth 2)", "Utilize como constante para realizar o login"))),
                        getResponse()));

    }

    @Test
    @Transactional
    public void refreshToken() throws Exception {
        String username = "lucas@lucas.com";
        String password = "QWERTY1234";
        Employee e = createAndSaveEmployee(username);

        Mockito.when(userService.remoteAuthenticateHandler(username, password))
                .thenReturn(Optional.of(e));

        Mockito.when(userService.handleLoginCheck(username))
                .thenReturn(Optional.of(e));

        MockHttpServletResponse response = this.mockMvc.perform(post("/oauth/token")
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
                .with(httpBasic(
                        properties.getUiCredentials().getCLIENT_ID(),
                        properties.getUiCredentials().getCLIENT_PASSWORD()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse();

        this.mockMvc.perform(post("/oauth/token")
                .param("grant_type", "refresh_token")
                .cookie(response.getCookies())
                .with(httpBasic(
                        properties.getUiCredentials().getCLIENT_ID(),
                        properties.getUiCredentials().getCLIENT_PASSWORD()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("grant_type").description(
                                        createDescription(
                                                "Grant Type (OAuth 2)",
                                                "Utilize como constante para renovar o token"))),
                        getResponse()));

    }

    private ResponseFieldsSnippet getResponse() {
        return responseFields(
                fieldWithPath("access_token").description("Token de acesso (OAuth 2)"),
                fieldWithPath("employee").description("Usuário logado"),
                fieldWithPath("expires_in").description("Tempo de expiração do token, em segundos (OAuth 2)"),
                fieldWithPath("jti").description("Json Token Identifier ou ID único para o token (OAuth 2)"),
                fieldWithPath("scope").description("Escopo do token (OAuth 2)"),
                fieldWithPath("token_type").description("Tipo do token (OAuth 2)"))
                .andWithPrefix("employee.",
                        fieldWithPath("id").description("Id do usuário"),
                        fieldWithPath("email").description("Email do usuário logado"),
                        fieldWithPath("name").description("Nome do usuário logado"))
                .andWithPrefix("info.",
                        fieldWithPath("name").description("Nome da aplicação"),
                        fieldWithPath("version").description("Versão atual da aplicação"));
    }

}
