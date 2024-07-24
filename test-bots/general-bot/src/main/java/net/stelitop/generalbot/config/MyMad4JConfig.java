package net.stelitop.generalbot.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Configuration
public class MyMad4JConfig {
    @Bean
    public GatewayDiscordClient gatewayDiscordClient() throws FileNotFoundException {
        File file = new File(getClass().getResource("/testbotconfig.txt").getFile());
        Scanner scanner = new Scanner(file);

        String tokenStr = scanner.nextLine();
        scanner.close();

        return DiscordClientBuilder.create(tokenStr).build()
            .gateway()
            .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.playing("Bot is online!")))
            .login()
            .block();
    }
}