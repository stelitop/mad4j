package stelitop.mad4j;

import discord4j.core.GatewayDiscordClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import stelitop.mad4j.listeners.CommandOptionAutocompleteListener;

@Configuration
public class Mad4jAutoConfiguration {

    @Bean
    public SlashCommandRegistrar slashCommandRegistrar(
            GatewayDiscordClient gatewayDiscordClient,
            CommandOptionAutocompleteListener commandOptionAutocompleteListener,
            ApplicationContext applicationContext,
            Environment environment
    ) {
        return new SlashCommandRegistrar(
                gatewayDiscordClient,
                commandOptionAutocompleteListener,
                applicationContext,
                environment
        );
    }
}
