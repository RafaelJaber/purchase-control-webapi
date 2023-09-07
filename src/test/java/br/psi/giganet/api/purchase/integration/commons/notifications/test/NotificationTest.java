package br.psi.giganet.api.purchase.integration.commons.notifications.test;

import br.psi.giganet.api.purchase.approvals.repository.ApprovalRepository;
import br.psi.giganet.api.purchase.common.notifications.controller.request.MarkAsReadRequest;
import br.psi.giganet.api.purchase.common.notifications.controller.security.RoleNotificationsRead;
import br.psi.giganet.api.purchase.common.notifications.model.Notification;
import br.psi.giganet.api.purchase.common.notifications.repository.NotificationRepository;
import br.psi.giganet.api.purchase.config.security.repository.PermissionRepository;
import br.psi.giganet.api.purchase.cost_center.repository.CostCenterRepository;
import br.psi.giganet.api.purchase.delivery_addresses.service.DeliveryAddressesService;
import br.psi.giganet.api.purchase.employees.model.Employee;
import br.psi.giganet.api.purchase.employees.repository.EmployeeRepository;
import br.psi.giganet.api.purchase.integration.commons.notifications.annotations.RoleTestNotificationsRead;
import br.psi.giganet.api.purchase.integration.utils.BuilderIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.RolesIntegrationTest;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestAdmin;
import br.psi.giganet.api.purchase.integration.utils.annotations.RoleTestRoot;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.api.purchase.purchase_requests.repository.PurchaseRequestRepository;
import br.psi.giganet.api.purchase.quotation_approvals.repository.QuotationApprovalRepository;
import br.psi.giganet.api.purchase.quotations.repository.QuotationRepository;
import br.psi.giganet.api.purchase.suppliers.repository.SupplierRepository;
import br.psi.giganet.api.purchase.suppliers.taxes.repository.TaxRepository;
import br.psi.giganet.api.purchase.units.repository.UnitRepository;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationTest extends BuilderIntegrationTest implements RolesIntegrationTest {

    private final Notification notificationTest;

    @Autowired
    public NotificationTest(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PermissionRepository permissionRepository,
            PurchaseRequestRepository purchaseRequestRepository,
            ApprovalRepository approvalRepository,
            QuotationRepository quotationRepository,
            QuotationApprovalRepository quotationApprovalRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            UnitRepository unitRepository,
            ProductCategoryRepository productCategoryRepository,
            CostCenterRepository costCenterRepository,
            PaymentConditionRepository paymentConditionRepository,
            DeliveryAddressesService addressService,
            TaxRepository taxRepository,
            NotificationRepository notificationRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.approvalRepository = approvalRepository;
        this.quotationRepository = quotationRepository;
        this.quotationApprovalRepository = quotationApprovalRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.costCenterRepository = costCenterRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
        this.paymentConditionRepository = paymentConditionRepository;
        this.addressService = addressService;
        this.taxRepository = taxRepository;
        this.notificationRepository = notificationRepository;

        createCurrentUser();

        notificationTest = createAndSaveNotification(Collections.singletonList(currentLoggedUser));
    }

    @RoleTestRoot
    public void findById() throws Exception {
        this.mockMvc.perform(get("/notifications/{id}", notificationTest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da notificação buscada")
                        ),
                        responseFields(
                                getNotificationResponse())));
    }

    @RoleTestRoot
    public void findAllByCurrentEmployee() throws Exception {
        this.mockMvc.perform(get("/notifications/me")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("limit").description(
                                        createDescription("Quantidade limite das notificações a serem retornadas",
                                                "Valor default: 50"))
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com todas as notificações do usuário logado, lidas e não lidas"))
                                .andWithPrefix("[].",
                                        getNotificationResponse())));
    }

    @RoleTestRoot
    public void findAllUnreadByCurrentEmployee() throws Exception {
        this.mockMvc.perform(get("/notifications/me/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]").description("Lista com todas as notificações não lidas do usuário logado"))
                                .andWithPrefix("[].",
                                        getNotificationResponse())));
    }


    @RoleTestRoot
    public void markAllAsViewed() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        MarkAsReadRequest request = createMarkAsReadRequest(
                Stream.of(createAndSaveNotification(list), createAndSaveNotification(list))
                        .map(Notification::getId)
                        .collect(Collectors.toList()));

        this.mockMvc.perform(post("/notifications/view")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("notifications").description(
                                        "Lista com os IDs das notificações a serem marcadas como visualizadas")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("Lista com as notificações atualizadas"))
                                .andWithPrefix("[].",
                                        getNotificationResponse())));
    }

    @RoleTestRoot
    public void markAsViewed() throws Exception {
        Notification notification = createAndSaveNotification(Collections.singletonList(currentLoggedUser));
        this.mockMvc.perform(post("/notifications/{id}/view", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andDo(document("{class_name}/{method_name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Código da notificação a ser definida como visualizada")
                        ),
                        responseFields(
                                getNotificationResponse())));
    }

    @Override
    @RoleTestNotificationsRead
    public void readAuthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        Notification notification = createAndSaveNotification(list);
        createAndSaveNotification(list);
        createAndSaveNotification(list);

        this.mockMvc.perform(get("/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/notifications/me")
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/notifications/me/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Override
    @RoleNotificationsRead
    public void writeAuthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        MarkAsReadRequest request = createMarkAsReadRequest(
                Stream.of(createAndSaveNotification(list), createAndSaveNotification(list))
                        .map(Notification::getId)
                        .collect(Collectors.toList()));

        this.mockMvc.perform(post("/notifications/view")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].viewed", Matchers.is(Boolean.TRUE)))
                .andExpect(jsonPath("$[1].viewed", Matchers.is(Boolean.TRUE)));

        Notification notification = createAndSaveNotification(list);
        this.mockMvc.perform(post("/notifications/{id}/view", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewed", Matchers.is(Boolean.TRUE)));
    }

    @Override
    @RoleTestAdmin
    public void readUnauthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        Notification notification = createAndSaveNotification(list);
        createAndSaveNotification(list);
        createAndSaveNotification(list);

        this.mockMvc.perform(get("/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/notifications/me")
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        this.mockMvc.perform(get("/notifications/me/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Override
    @RoleTestAdmin
    public void writeUnauthorized() throws Exception {
        List<Employee> list = Collections.singletonList(currentLoggedUser);
        MarkAsReadRequest request = createMarkAsReadRequest(
                Stream.of(createAndSaveNotification(list), createAndSaveNotification(list))
                        .map(Notification::getId)
                        .collect(Collectors.toList()));

        this.mockMvc.perform(post("/notifications/view")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());

        Notification notification = createAndSaveNotification(list);
        this.mockMvc.perform(post("/notifications/{id}/view", notification.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    private MarkAsReadRequest createMarkAsReadRequest() {
        return createMarkAsReadRequest(Stream.of(
                createAndSaveNotification(),
                createAndSaveNotification(),
                createAndSaveNotification())
                .map(Notification::getId)
                .collect(Collectors.toList()));
    }

    private MarkAsReadRequest createMarkAsReadRequest(List<Long> notifications) {
        MarkAsReadRequest request = new MarkAsReadRequest();
        request.setNotifications(notifications);
        return request;
    }

    private FieldDescriptor[] getNotificationResponse() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Código da notificação"),
                fieldWithPath("title").description("Título da notificação"),
                fieldWithPath("description").description("Descrição notificação"),
                fieldWithPath("date").description("Data de criação da notificação"),
                fieldWithPath("type").description("Tipo da notificação. Normalmente é de acordo com o evento associado"),
                fieldWithPath("data").optional().type(JsonFieldType.STRING)
                        .description("Campo opcional de dado. É definido de acordo com o tipo da notificação"),
                fieldWithPath("viewed").description("Informa se o usuário visualizou ou não a notificação"),
                fieldWithPath("viewedDate").optional().type(JsonFieldType.STRING)
                        .description("Data da visualização pelo usuário atual, caso exista"),
        };
    }
}
