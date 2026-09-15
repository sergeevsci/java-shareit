package ru.practicum.shareit.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.client.BookingClient;
import ru.practicum.shareit.gateway.client.ItemClient;
import ru.practicum.shareit.gateway.client.ItemRequestClient;
import ru.practicum.shareit.gateway.client.UserClient;

@Configuration
public class ClientConfiguration {
    @Value("${shareit.server.url}")
    private String serverUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @Bean
    public UserClient userClient(RestTemplate restTemplate) {
        return new UserClient(serverUrl, restTemplate);
    }

    @Bean
    public ItemClient itemClient(RestTemplate restTemplate) {
        return new ItemClient(serverUrl, restTemplate);
    }

    @Bean
    public BookingClient bookingClient(RestTemplate restTemplate) {
        return new BookingClient(serverUrl, restTemplate);
    }

    @Bean
    public ItemRequestClient itemRequestClient(RestTemplate restTemplate) {
        return new ItemRequestClient(serverUrl, restTemplate);
    }
}
