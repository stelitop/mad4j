package net.stelitop.mad4j.interactions;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import net.stelitop.mad4j.commands.components.ComponentInteraction;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventResponse {

    private enum ResponseContent {
        PLAINTEXT,
        EMBED,
        UI
    }
    private enum ResponseAction {
        CREATE,
        EDIT,
        REPLY
    }

    private ResponseAction actionType;
    private ResponseContent contentType;

    private String plaintextContent;
    private EmbedCreateSpec embedContent;
    // private UI uiContent;

    private boolean ephemereal = false;
    private LayoutComponent[] components = new LayoutComponent[0];

    private EventResponse(ResponseAction actionType, ResponseContent contentType) {
        this.actionType = actionType;
        this.contentType = contentType;
    }

    public static EventResponse createPlaintext(String message) {
        EventResponse ret = new EventResponse(ResponseAction.CREATE, ResponseContent.PLAINTEXT);
        ret.plaintextContent = message;
        return ret;
    }
    public static EventResponse replyPlaintext(String message) {
        EventResponse ret = new EventResponse(ResponseAction.REPLY, ResponseContent.PLAINTEXT);
        ret.plaintextContent = message;
        return ret;
    }
    public static EventResponse editPlaintext(String message) {
        EventResponse ret = new EventResponse(ResponseAction.EDIT, ResponseContent.PLAINTEXT);
        ret.plaintextContent = message;
        return ret;
    }
    public static EventResponse createEmbed(EmbedCreateSpec embed) {
        EventResponse ret = new EventResponse(ResponseAction.CREATE, ResponseContent.EMBED);
        ret.embedContent = embed;
        return ret;
    }
    public static EventResponse replyEmbed(EmbedCreateSpec embed) {
        EventResponse ret = new EventResponse(ResponseAction.REPLY, ResponseContent.EMBED);
        ret.embedContent = embed;
        return ret;
    }
    public static EventResponse editEmbed(EmbedCreateSpec embed) {
        EventResponse ret = new EventResponse(ResponseAction.EDIT, ResponseContent.EMBED);
        ret.embedContent = embed;
        return ret;
    }
    public static EventResponse createUI(/*Custom UI Object*/) {
        throw new UnsupportedOperationException();
    }
    public static EventResponse replyUI(/*Custom UI Object*/) {
        throw new UnsupportedOperationException();
    }
    public static EventResponse editUI(/*Custom UI Object*/) {
        throw new UnsupportedOperationException();
    }

    public EventResponse ephemeral() {
        this.ephemereal = true;
        return this;
    }
    public EventResponse ephemeral(boolean value) {
        this.ephemereal = value;
        return this;
    }
    public EventResponse components(LayoutComponent... components) {
        this.components = components;
        return this;
    }

    public Mono<Void> respond(InteractionCreateEvent event) {
        if (event instanceof ChatInputInteractionEvent ciie) return respondToSlashCommand(ciie);
        else if (event instanceof ComponentInteractionEvent cie) return respondToComponentInteraction(cie);
        throw new UnsupportedOperationException("This type of responses is not yet handled!");
    }

    private Mono<Void> respondToSlashCommand(ChatInputInteractionEvent event) {
        if (actionType != ResponseAction.CREATE && actionType != ResponseAction.REPLY) {
            throw new IllegalStateException("You can only create or reply to a command!");
        }

        InteractionApplicationCommandCallbackReplyMono replyMono = switch (contentType) {
            case PLAINTEXT -> event.reply(plaintextContent);
            case EMBED -> event.reply().withEmbeds(embedContent);
            default -> throw new UnsupportedOperationException("This type of responses is not yet handled!");
        };
        return replyMono.withEphemeral(ephemereal).withComponents(components);
    }

    private Mono<Void> respondToComponentInteraction(ComponentInteractionEvent event) {
        return (switch (actionType) {
            case CREATE, REPLY -> switch (contentType) {
                case PLAINTEXT -> event.reply(plaintextContent);
                case EMBED -> event.reply().withEmbeds(embedContent);
                default -> throw new UnsupportedOperationException("This type of responses is not yet handled!");
            };
            case EDIT -> switch (contentType) {
                case PLAINTEXT -> event.edit(plaintextContent);
                case EMBED -> event.edit().withEmbeds(embedContent);
                default -> throw new UnsupportedOperationException("This type of responses is not yet handled!");
            };
        });
    }
}
