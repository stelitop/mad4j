package net.stelitop.mad4j;

import discord4j.core.GatewayDiscordClient;
import net.stelitop.mad4j.autocomplete.AutocompletionExecutor;
import net.stelitop.mad4j.listeners.CommandOptionAutocompleteListener;
import net.stelitop.mad4j.listeners.ComponentEventListener;
import net.stelitop.mad4j.listeners.SlashCommandListener;
import net.stelitop.mad4j.requirements.CommandRequirementExecutor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@ComponentScan("net.stelitop.mad4j")
@Configuration
public class Mad4jConfig {

}
//@Configuration
//public class Mad4jConfig {
//
//    @Bean
//    public SlashCommandRegistrar slashCommandRegistrar(
//            GatewayDiscordClient gatewayDiscordClient,
//            CommandOptionAutocompleteListener commandOptionAutocompleteListener,
//            ApplicationContext applicationContext,
//            Environment environment
//    ) {
//        return new SlashCommandRegistrar(
//                gatewayDiscordClient,
//                commandOptionAutocompleteListener,
//                applicationContext,
//                environment
//        );
//    }
//
//    @Bean
//    public CommandOptionAutocompleteListener commandOptionAutocompleteListener(
//            GatewayDiscordClient client,
//            List<AutocompletionExecutor> autocompletionExecutors
//    ) {
//        return new CommandOptionAutocompleteListener(
//                client,
//                autocompletionExecutors
//        );
//    }
//
//    @Bean
//    public ComponentEventListener componentEventListener(
//            GatewayDiscordClient client,
//            ApplicationContext applicationContext
//    ) {
//        return new ComponentEventListener(
//                client,
//                applicationContext
//        );
//    }
//
//    @Bean
//    public SlashCommandListener slashCommandListener(
//            ApplicationContext applicationContext,
//            GatewayDiscordClient client,
//            List<CommandRequirementExecutor> possibleRequirements
//    ) {
//        return new SlashCommandListener(
//                applicationContext,
//                client,
//                possibleRequirements
//        );
//    }
//}
