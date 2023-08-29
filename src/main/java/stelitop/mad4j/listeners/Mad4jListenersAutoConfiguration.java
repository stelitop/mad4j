package stelitop.mad4j.listeners;

import discord4j.core.GatewayDiscordClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import stelitop.mad4j.autocomplete.AutocompletionExecutor;
import stelitop.mad4j.requirements.CommandRequirementExecutor;

import java.util.List;

@Configuration
public class Mad4jListenersAutoConfiguration {

    @Bean
    public SlashCommandListener slashCommandListener(
            ApplicationContext applicationContext,
            GatewayDiscordClient client,
            List<CommandRequirementExecutor> possibleRequirements
    ) {
        return new SlashCommandListener(
                applicationContext,
                client,
                possibleRequirements
        );
    }

    @Bean
    public CommandOptionAutocompleteListener commandOptionAutocompleteListener(
            GatewayDiscordClient client,
            List<AutocompletionExecutor> autocompletionExecutors
    ) {
        return new CommandOptionAutocompleteListener(client, autocompletionExecutors);
    }

    @Bean
    public ComponentEventListener componentEventListener(
            GatewayDiscordClient client,
            ApplicationContext applicationContext
    ) {
        return new ComponentEventListener(client, applicationContext);
    }
}
