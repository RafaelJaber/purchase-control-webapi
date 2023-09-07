package br.psi.giganet.api.purchase.common.webhooks.services;

import br.psi.giganet.api.purchase.common.messages.service.LogMessageService;
import br.psi.giganet.api.purchase.common.webhooks.model.Webhook;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookServer;
import br.psi.giganet.api.purchase.common.webhooks.model.WebhookType;
import br.psi.giganet.api.purchase.config.project_property.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public abstract class AbstractWebhookService {

    @Autowired
    private RestTemplate http;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private LogMessageService logService;

    /**
     * Receive web hooks handler
     *
     * @param webHook
     * @param signatureKey use header name 'Signature' to get this key
     */
    public void onReceiveWebHookHandler(Webhook webHook, String signatureKey) {
        if (validateReceivedWebHook(webHook, signatureKey)) {
            onReceive(webHook);
        } else {
            throw new IllegalArgumentException("Invalid signature");
        }
    }

    public abstract void onReceive(Webhook webHook);

    /**
     * Send web hooks handler
     *
     * @param webhook     object to send
     * @param destination destination
     * @return true if the request was successfully or false if any error occurred
     */
    protected boolean sendRequest(Webhook webhook, WebhookServer destination) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Signature", generateSendKey(WebhookServer.PURCHASE_API, webhook.getType()));

        HttpEntity<Object> entity = new HttpEntity<>(webhook, headers);
        String url;
        switch (destination) {
            case STOCK_API:
                url = properties.getWebhooks().getStockApi().getUrl();
                break;
            default:
                return false;
        }

        try {
            if (properties.getWebhooks().getEnable()) {
                this.http.postForObject(url, entity, String.class);
            } else {
                System.out.println("===== WEBHOOK ====\n" + new ObjectMapper().writeValueAsString(entity));
            }
            return true;

        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu durante o envio do webhook");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            try {
                logService.send(new ObjectMapper().writeValueAsString(errors));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public void send(Webhook webhook, WebhookServer destination, boolean wait, int attempts) throws InterruptedException {
        Thread t = new Thread(() -> {
            int count = 0;
            while (count < attempts) {
                if (this.sendRequest(webhook, destination)) break;

                count++;
            }
        });

        t.start();
        if (wait) {
            t.join();
        }
    }

    public void send(Webhook webhook, WebhookServer destination, boolean wait) throws InterruptedException {
        final int attempts = 3;
        Thread t = new Thread(() -> {
            int count = 0;
            while (count < attempts) {
                if (this.sendRequest(webhook, destination)) break;

                count++;
            }
        });

        t.start();
        if (wait) {
            t.join();
        }

    }


    private boolean validateReceivedWebHook(Webhook webHook, String key) {
        String generated = generateReceiveKey(webHook.getOrigin(), webHook.getType());
        return generated != null && generated.equals(key);
    }

    private String generateSendKey(WebhookServer origin, WebhookType type) {
        return DigestUtils.sha256Hex(origin.name() + "." + type.name() + "." + properties.getWebhooks().getSecretKey());
    }

    private String generateReceiveKey(WebhookServer origin, WebhookType type) {
        if (origin.equals(WebhookServer.STOCK_API)) {
            String key = properties.getWebhooks().getStockApi().getKey();
            return DigestUtils.sha256Hex(origin.name() + "." + type.name() + "." + key);
        }
        return null;
    }

}
