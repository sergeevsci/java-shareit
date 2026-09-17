package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.client.BookingClient;
import ru.practicum.shareit.gateway.client.ItemClient;
import ru.practicum.shareit.gateway.client.ItemRequestClient;
import ru.practicum.shareit.gateway.client.UserClient;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ClientConfiguration.class, properties = "shareit.server.url=http://localhost:9090")
class ClientConfigurationTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private ItemRequestClient itemRequestClient;

    @Test
    void createsGatewayClientsAndPatchCapableRestTemplate() {
        assertInstanceOf(HttpComponentsClientHttpRequestFactory.class, restTemplate.getRequestFactory());
        assertNotNull(userClient);
        assertNotNull(itemClient);
        assertNotNull(bookingClient);
        assertNotNull(itemRequestClient);
    }
}
