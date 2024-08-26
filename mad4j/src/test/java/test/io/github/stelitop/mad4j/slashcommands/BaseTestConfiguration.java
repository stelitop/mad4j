package test.io.github.stelitop.mad4j.slashcommands;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootConfiguration
@ComponentScan("io.github.stelitop.mad4j")
public class BaseTestConfiguration {

    public static long TEST_APPLICATION_ID = 1L;

    @Bean
    public GatewayDiscordClient gatewayDiscordClient(DiscordClient clientMock) {
        GatewayDiscordClient gatewayDiscordClientMock = mock(GatewayDiscordClient.class);
        when(gatewayDiscordClientMock.getRestClient()).thenReturn(clientMock);
        when(gatewayDiscordClientMock.rest()).thenReturn(clientMock);
        when(gatewayDiscordClientMock.on(any(), any())).thenReturn(Flux.empty());
        return gatewayDiscordClientMock;
    }

    @Bean
    public DiscordClient client(ApplicationService applicationServiceMock) {
        DiscordClient clientMock = mock(DiscordClient.class);
        when(clientMock.getApplicationId()).thenReturn(Mono.just(TEST_APPLICATION_ID));
        when(clientMock.getApplicationService()).thenReturn(applicationServiceMock);
        return clientMock;
    }

    @Bean
    ApplicationService applicationService() {
        ApplicationService applicationServiceMock = mock(ApplicationService.class);
        when(applicationServiceMock.bulkOverwriteGlobalApplicationCommand(eq(TEST_APPLICATION_ID), any())).thenReturn(Flux.empty());
        return applicationServiceMock;
    }
}