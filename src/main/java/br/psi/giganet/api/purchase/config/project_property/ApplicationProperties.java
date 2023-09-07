package br.psi.giganet.api.purchase.config.project_property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app")
public class ApplicationProperties {

    @Value("${info.app.name}")
    private String apiTitle;
    @Value("${info.app.version}")
    private String apiVersion;

    private String appName = "Giganet - Purchase Control API";
    private Boolean isDev = Boolean.TRUE;
    private String authorizationApiUrl = "http://api-dev.smartnet.giganet.psi.br";
    private String apiUrl = "http://localhost:8080";
    private String apiDomain = "localhost:8080";
    private Boolean enableMail = Boolean.TRUE;
    private final TokenProperties tokenProperties = new TokenProperties();
    private final Credentials uiCredentials = new Credentials();
    private final SecurityUtils securityUtils = new SecurityUtils();

    private final WebhooksConfig webhooks = new WebhooksConfig();

    @Data
    public class TokenProperties {

        private boolean enableHttps = false;
        
        private int cookieExpirationTime = 86400; // Default: one day
        private int tokenExpirationTime = 3600; // one hour

    }

    @Data
    public class WebhooksConfig {
        private Boolean enable = Boolean.TRUE;
        private String secretKey;
        private final WebHook stockApi = new WebHook();
    }

    @Data
    public class WebHook {
        private String name;
        private String key;
        private String url;

        public boolean checkCredentials(String key) {
            return this.key.equals(key);
        }
    }

    @Data
    public class Credentials{
        private String url = "*";
        private String CLIENT_ID = "angular";
        private String CLIENT_PASSWORD = "7EP!AqZ36U&+PuSg";
    }

    @Data
    public class SecurityUtils{

        private String CREATE_ADMIN_USER_KEY = "qq82XvQ+v%qS9fP#cA3t!UMcSyWTca#F4#WSQfg!LVY=K%?N2MYjPe?Vb_zR";
        private String signingKey = "nHNp7j4yHsbwTK5DqsAmbMQRPBePC5LMVAqqLqxF3m7kmuGH6rqV6y3waDvf9wmK5rjF6Qag2wJcnGEqpgJT8zwh9sqcXEZpv6UdTCygP7xqZqpzrQjL2sPJwTn4qZVt";

    }

}
