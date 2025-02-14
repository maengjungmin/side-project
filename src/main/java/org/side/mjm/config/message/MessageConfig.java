package org.side.mjm.config.message;

import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

@Configuration
public class MessageConfig {
    private final Properties msgProp;

    public MessageConfig() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("message/message.properties")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(input), StandardCharsets.UTF_8));
            this.msgProp = new Properties();
            msgProp.load(reader);
        }
    }

    /**
     * 메시지 키를 전달받아 메시지를 리턴
     */
    public String getMsg(String key) {
        return msgProp.getProperty(key);
    }

    /**
     * 코드 키를 전달 받아 코드를 리턴
     */
    public String getCode(String key) {
        return msgProp.getProperty(key);
    }
}