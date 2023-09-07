package br.psi.giganet.api.purchase.config.filter;

import br.psi.giganet.api.purchase.common.messages.service.LogMessageService;
import br.psi.giganet.api.purchase.common.utils.model.WebRequestProjection;
import br.psi.giganet.api.purchase.config.exception.response.SimpleErrorResponse;
import br.psi.giganet.api.purchase.config.project_property.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class RemoteApiAuthenticationFilter implements Filter {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private LogMessageService logService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        final String securityPath = "/basic/";
        if (!request.getRequestURI().contains(securityPath)) {
            chain.doFilter(req, resp);
            return;
        }

        List<ApplicationProperties.WebHook> apps = Arrays.asList(properties.getWebhooks().getStockApi());

        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            String token = authorization.replaceAll("Basic ", "");
            String[] data = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8).split(":");

            if (data.length != 2) {
                unauthorizedHandler(request, response);

            } else if (apps.stream().anyMatch(app -> app.getName().equals(data[0]) && app.checkCredentials(data[1]))) {
                chain.doFilter(req, resp);
                return;

            }
        }
        unauthorizedHandler(request, response);
    }

    private void unauthorizedHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), new SimpleErrorResponse("Acesso não autorizado"));

        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Acesso não autorizado");
        errors.put("description", "Usuário/Senha incorretos para autenticação básica");
        errors.put("request", new WebRequestProjection(request));
        logService.send(mapper.writeValueAsString(errors));
    }

}