package net.stelitop.mad4j;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Configuration for mad4j.</p>
 *
 * <p>This loads all components part of the package when used into another project.</p>
 */
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
