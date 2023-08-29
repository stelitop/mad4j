package stelitop.mad4j;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.rest.RestClient;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import lombok.AllArgsConstructor;
import lombok.ToString;
import stelitop.mad4j.autocomplete.NullAutocompleteExecutor;
import stelitop.mad4j.commands.CommandParam;
import stelitop.mad4j.commands.CommandParamChoice;
import stelitop.mad4j.commands.SlashCommand;
import stelitop.mad4j.listeners.CommandOptionAutocompleteListener;
import stelitop.mad4j.utils.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>At the start of the program, this component finds all defined slash commands inside of
 * {@link DiscordEventsComponent} classes. Their signatures are parsed and transformed into
 * {@link ApplicationCommandRequest} objects to be sent to discord.</p>
 *
 * <p>During parsing, all commands with Autocomplete specified are registered in the
 * {@link CommandOptionAutocompleteListener} component. This behaviour might be moved to that
 * component instead directly.</p>
 *
 * <p>You can disable the registering of commands by setting the property "slashcommands.update"
 * to false in the application.properties file. This can be used because sometimes discord will
 * "outdate" the commands and require you to wait.</p>
 */
@Component
public class SlashCommandRegistrar implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final GatewayDiscordClient gatewayDiscordClient;
    private final CommandOptionAutocompleteListener commandOptionAutocompleteListener;
    private final ApplicationContext applicationContext;
    private final Environment environment;

    public SlashCommandRegistrar(
            GatewayDiscordClient gatewayDiscordClient,
            CommandOptionAutocompleteListener commandOptionAutocompleteListener,
            ApplicationContext applicationContext,
            Environment environment
    ) {
        this.gatewayDiscordClient = gatewayDiscordClient;
        this.commandOptionAutocompleteListener = commandOptionAutocompleteListener;
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    /**
     * <p>Registers all properly annotated slash commands into the discord bot.</p>
     *
     * <p>First all annotated methods are collected. Then, the names of the commands are
     * used to structure them into a tree hierarchy. The first name of the commands is used
     * as the base command. Finally, each command tree is individually parsed into a single
     * command request, which are then overwritten in bulk.</p>
     *
     * <p>In the environmental variables it can specifically specified to not register
     * the commands and instead use the old ones. This can be used when there aren't any
     * changes and to prevent potential "outdating" of the command signatures.</p>
     *
     * @param args incoming application arguments
     */
    @Override
    public void run(ApplicationArguments args) {

        var slashCommandMethods = applicationContext.getBeansWithAnnotation(DiscordEventsComponent.class).values().stream()
                .map(Object::getClass)
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(SlashCommand.class))
                .toList();

        String missingCommandEventAnnotation = slashCommandMethods.stream()
                .filter(x -> Arrays.stream(x.getParameters()).noneMatch(y -> y.isAnnotationPresent(InteractionEvent.class)))
                .map(Method::getName)
                .collect(Collectors.joining(", "));
        if (!missingCommandEventAnnotation.isBlank()) {
            throw new RuntimeException("Following commands don't have a command event: " + missingCommandEventAnnotation);
        }

        var slashCommandRequests = createCommandRequestsFromMethods(slashCommandMethods);

        String updateCommands = Optional.ofNullable(environment.getProperty("slashcommands.update")).orElse("true");
        if (updateCommands.equalsIgnoreCase("false")) {
            LOGGER.warn("No slash command signatures were updated due to the environment settings!");
            return;
        }

        RestClient restClient = gatewayDiscordClient.getRestClient();
        var applicationService = restClient.getApplicationService();
        Long applicationId = restClient.getApplicationId().block();
        LOGGER.info("Started registering global commands...");
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, slashCommandRequests)
                .doOnNext(c -> LOGGER.info("Successfully registered command " + c.name() + "."))
                .doOnError(e -> LOGGER.error("Failed to register global commands.", e))
                .doOnComplete(() -> LOGGER.info("Finished registering global commands."))
                .subscribe();
    }

    /**
     * Creates the application command requests to send to discord for creating the
     * blueprints of the slash commands. They are created from taking all methods
     * annotated with {@link SlashCommand} and reading their content. From the names
     * of the commands a tree is created to group commands that have the same first
     * names.
     *
     * @param methods List of methods annotated with {@link SlashCommand}.
     * @return A list of application command requests. Every request is about a
     *     different command.
     */
    private List<ApplicationCommandRequest> createCommandRequestsFromMethods(List<Method> methods) {

        List<CommandTreeNode> trees = createCommandNameTrees(methods);

        return trees.stream()
                .map(this::processSlashCommandTree)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Creates a tree hierarchy from slash command methods. The names of the
     * slash commands are space-separated strings, each being a different
     * level of nesting in the final command.
     *
     * @param methods The methods of the commands, each annotated with
     *     {@link SlashCommand}.
     * @return A list of command tree nodes, where each node has a different
     *     first command name.
     */
    private List<CommandTreeNode> createCommandNameTrees(List<Method> methods) {
        CommandTreeNode shadowRoot = new CommandTreeNode("", new ArrayList<>(), null);
        for (var method : methods) {
            SlashCommand scAnnotation = method.getAnnotation(SlashCommand.class);
            if (scAnnotation == null) {
                continue;
            }
            String[] parts = scAnnotation.name().toLowerCase().split(" ");
            if (parts.length == 0) {
                continue;
            }

            CommandTreeNode currentNode = shadowRoot;
            for (String partName : parts) {
                if (currentNode.children.stream().noneMatch(x -> x.name.equals(partName))) {
                    currentNode.children.add(new CommandTreeNode(partName, new ArrayList<>(), null));
                }
                currentNode = currentNode.children.stream().filter(x -> x.name.equals(partName)).findFirst().get();
            }
            currentNode.method = method;
        }

        return shadowRoot.children;
    }

    /**
     * Helper class to represent a node in the tree structure of commands.
     */
    @AllArgsConstructor
    @ToString
    private static class CommandTreeNode {
        /**
         * The name of the current command part. Only a single word.
         */
        public String name;
        /**
         * The children in the command hierarchy.
         */
        public List<CommandTreeNode> children;
        /**
         * The method that would execute the slash command, if this is a leaf.
         */
        public Method method;
    }

    /**
     * Transforms a tree of commands into a single application command request. The
     * tree is recursively parsed to explore all branches.
     *
     * @param tree The slash command tree.
     * @return An application command request that can be directly sent.
     */
    private ApplicationCommandRequest processSlashCommandTree(CommandTreeNode tree) {
        var requestBuilder = ApplicationCommandRequest.builder();
        requestBuilder.name(tree.name);
        if (tree.method != null) {
            var annotation = tree.method.getAnnotation(SlashCommand.class);
            if (annotation == null) {
                return null;
            }
            requestBuilder.description(annotation.description());
            requestBuilder.addAllOptions(getOptionsFromMethod(tree.method));
            return requestBuilder.build();
        }
        requestBuilder.description("Description for " + tree.name);
        tree.children.forEach(child -> requestBuilder.addOption(createOptionFromTreeChild(child)));
        return requestBuilder.build();
    }

    /**
     * Creates a new application command option data from the child node of a tree
     * node. This means that this either comes from a Subcommand or a SubcommandGroup.
     *
     * @param node The command node to transform.
     * @return The ApplicationCommandOptionData.
     */
    private ApplicationCommandOptionData createOptionFromTreeChild(CommandTreeNode node) {
        // this is a method
        var acodBuilder = ApplicationCommandOptionData.builder();
        acodBuilder.name(node.name);
        if (node.method != null) {
            var annotation = node.method.getAnnotation(SlashCommand.class);
            if (annotation == null) {
                return ApplicationCommandOptionData.builder().build();
            }
            acodBuilder.description(annotation.description());
            acodBuilder.type(OptionType.SUB_COMMAND);
            acodBuilder.addAllOptions(getOptionsFromMethod(node.method));
            return acodBuilder.build();
        }
        acodBuilder.description("Description for " + node.name);
        acodBuilder.type(OptionType.SUB_COMMAND_GROUP);
        node.children.forEach(child -> acodBuilder.addOption(createOptionFromTreeChild(child)));
        return acodBuilder.build();
    }

    /**
     * Parses the annotations present on a method's signature and transforms them into
     * {@link ApplicationCommandOptionData} objects for the slash command request. These
     * are the parameters of the slash command.
     *
     * @param method Method to parse.
     * @return A list of {@link ApplicationCommandOptionData} for the annotated methods.
     */
    private List<ApplicationCommandOptionData> getOptionsFromMethod(Method method) {
        List<ApplicationCommandOptionData> ret = new ArrayList<>();
        var parameters = Arrays.stream(method.getParameters())
                .filter(x -> x.isAnnotationPresent(CommandParam.class))
                .toList();

        String commandName = method.getAnnotation(SlashCommand.class).name().toLowerCase();

        for (var parameter : parameters) {
            if (!parameter.isAnnotationPresent(CommandParam.class)) continue;
            CommandParam paramAnnotation = parameter.getAnnotation(CommandParam.class);
            ret.add(parseRegularCommandParam(paramAnnotation, parameter, commandName));
        }
        return ret;
    }

    /**
     * Transforms a parameter annotated with {@link CommandParam} into an
     * {@link ApplicationCommandOptionData} to be used in a slash command signature.
     *
     * @param annotation The {@link CommandParam} annotation.
     * @param parameter The param of the method signature.
     * @param commandName The full name of the command this parameter originates from.
     * @return The {@link ApplicationCommandOptionData}.
     */
    private ApplicationCommandOptionData parseRegularCommandParam(
            CommandParam annotation,
            Parameter parameter,
            String commandName)
    {
        var acodBuilder = ApplicationCommandOptionData.builder()
                .name(annotation.name().toLowerCase())
                .description(annotation.description())
                .required(annotation.required())
                .type(OptionType.getCodeOfClass(parameter.getType()));

        // For Autocomplete:
        // TODO: Check that there are no options available for the command
        // TODO: Check that the type of the input is one of String, Number or Integer
        if (annotation.autocomplete()!= NullAutocompleteExecutor.class) {
            acodBuilder.autocomplete(true);
            String paramName = annotation.name().toLowerCase();
            commandOptionAutocompleteListener.addMapping(commandName, paramName, annotation.autocomplete());
        }

        addChoicesToCommandParam(acodBuilder, annotation.choices());
        return acodBuilder.build();
    }

    /**
     * Adds the specified choices for the value of a command param to the
     * {@link ApplicationCommandOptionData} builder. If there are no such
     * options, nothing happens.
     *
     * @param acodBuilder The builder.
     * @param choices The available choices. Can be empty.
     */
    private void addChoicesToCommandParam(
            ImmutableApplicationCommandOptionData.Builder acodBuilder,
            CommandParamChoice[] choices
    ) {
        if (choices.length == 0) return;
        acodBuilder.addAllChoices(Arrays.stream(choices)
                .map(x -> ApplicationCommandOptionChoiceData.builder()
                        .name(x.name())
                        .value(x.value())
                        .build())
                .map(x -> (ApplicationCommandOptionChoiceData)x)
                .toList());
    }
}
