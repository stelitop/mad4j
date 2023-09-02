# mad4j

Mad4j (Method-Annotated Discord4J) is a library for creating discord bots using Java. It builds on top of an already existing library 
[Discord4j](https://github.com/Discord4J/Discord4J) and simplifies the developing process using an annotation-based system. It is also integrated 
using Java Spring for its convenient features. This library is aimed at creating both small- and large-scale discord bots.

# Setup

Currently, the project is not yet available on mavencentral, so the jar package has to be manually downloaded from the 
[package section]{https://github.com/stelitop/mad4j/packages} in GitHub. Look for the -plain.jar version. Then, the package can be added to the project as follows:

For Gradle:
```gradle
implementation files('path/to/file/mad4j-VERSION-plain.jar')
```

For Maven:
```maven
To be done
```

# Configuration

Since the library is based on Java Spring, it contains components that have to be included in your Spring context. There are two suggested ways to do this:
- The first is to directly import the Mad4jConfig to the main class of the Spring application:
  ```java
  import net.stelitop.mad4j.Mad4jConfig;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.context.annotation.Import;

  @SpringBootApplication
  @Import(Mad4jConfig.class)
  public class DiscordBotApplication {
    public static void main(String[] args) {
      SpringApplication.run(DiscordBotApplication.class, args);
    }
  }
  ```
- The second is almost the same, except we import the Mad4jConfig in a separate configuration component. This is the recommended way, as this way you have
  more control over the library. It is also easier to disable when testing needs to be done on other parts of the bot, such as testing JPA repositories.
  ```java
  import net.stelitop.mad4j.Mad4jConfig;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.context.annotation.Import;

  @Import(Mad4jConfig.class)
  @Configuration
  public class CustomMad4jConfig {
  }
  ```

The other thing that needs to be configured is the discord client bean. A bean of type discord4j.core.GatewayDiscordClient must be declared that starts
the discord bot. The simplest such configuration is as follows:
```java
@Bean
public GatewayDiscordClient gatewayDiscordClient() {
  String tokenStr = "DISCORD_BOT_TOKEN";

  return DiscordClientBuilder.create(tokenStr).build()
      .gateway()
      .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.playing("Bot is online!")))
      .login()
      .block();
}
```

Being able to declare your own bean gives the developer more flexibility in how the bot can be set up, along with tying some functionality to outside configurations.

# Features

## @DiscordEventsComponent
Most features of mad4j can be accessed through the special Spring components @DiscordEventsComponent. Inside those components, you can create
methods, that then use other mad4j annotations.

## @SlashCommand
@SlashCommand methods represent global slash commands of the discord bot. When the application loads, all such methods are parsed and corresponding command signatures
are created. The arguments of the method also use annotations to specify the parameters of the slash command. An example of a simple command is:
```java
@SlashCommand(
    name = "add",
    description = "Adds up two numbers"
) public Mono<Void> addTwoNumbersSlashCommand(
    @InteractionEvent ChatInputInteractionEvent event,
    @CommandParam(name = "x", description = "The first number.") long x,
    @CommandParam(name = "y", description = "The second number.") long y,
) {
    return event.reply(x + y);
}
```

To explain each annotation one by one:
- In the @SlashCommand annotation we give the metadata of the slash command, such as its name and description. The command name can be a space-separated
  list of words, e.g. "add numbers". In this case, a command group with sub-commands will be created. You can then add another command that starts with
  the first command name and it will be automatically added to the group, e.g. "add vectors".
- The @InteractionEvent injects the event that corresponds to the interaction type into the method. These are the event types that originate from Discord4j.
  In the case of slash commands, this is ChatInputInteractionEvent. To properly execute the slash command we must return the reply of the event. This is why
  this method returns an object of type Mono<Void>.
- The @CommandParam annotations are used for the different inputs of the slash command (if there are any). When a command is invoked, the values from Discord
  are automatically injected into the method. This annotation has many optional fields for special cases.

In contrast to using only regular Discord4j, it is first recommended to make a separate JSON file that contains the slash command signature. Then, when we create
a ChatInputInteractionListener we must first verify this is the specific slash command by performing tedious checks. Next, we have to extract the parameters from
the event which is not always trivial and THEN finally proceed with the actual logic. This way of development does not hold well when changes occur and can
lead to many problems down the line. You can see an example [here](https://docs.discord4j.com/interactions/application-commands/#simplifying-the-lifecycle).

## Message Component Interactions
To be done.

## Command Option Autocompletion
To be done.
