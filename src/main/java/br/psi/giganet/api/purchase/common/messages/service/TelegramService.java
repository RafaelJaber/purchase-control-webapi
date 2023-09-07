package br.psi.giganet.api.purchase.common.messages.service;

import br.psi.giganet.api.purchase.config.project_property.ApplicationProperties;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService implements LogMessageService {

    private final boolean IS_DEV;
    private final String APP_NAME;

    private final String TELEGRAM_TOKEN = "1058985717:AAGPRg9PVHtlMz9KXX1xkwzMeZpNS2Vg0jA";
    private final String TELEGRAM_URL = "https://api.telegram.org/bot";

    private static final String LUCAS_NUMBER = "899814954";

    @Autowired
    public TelegramService(ApplicationProperties properties) {
        IS_DEV = properties.getIsDev();
        APP_NAME = properties.getAppName();
    }

    @Override
    public void send(String message) {
        this.sendMessageToLucas(APP_NAME + "\n" + message);
    }

    public void sendMessageToLucas(String content) {
        sendMessage(LUCAS_NUMBER, content);
    }

    public void sendMessage(String chat, String content) {
        try {

            new Thread(() -> {
                try {
                    if (IS_DEV) {
                        System.out.println("Telegram \t chat " + chat + "\n" + content);
                    } else {
                        HttpClientBuilder
                                .create()
                                .build()
                                .execute(new HttpGet(
                                        TELEGRAM_URL + TELEGRAM_TOKEN + "/sendMessage?chat_id=" + chat + "&text="
                                                + URLEncoder.encode(content, StandardCharsets.UTF_8)
                                ));
                    }

                } catch (Exception ignored) {

                }
            }).start();

        } catch (Exception ignored) {

        }
    }

}
