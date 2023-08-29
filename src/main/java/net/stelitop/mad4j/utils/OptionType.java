package net.stelitop.mad4j.utils;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.HashMap;
import java.util.Map;

public class OptionType {
    public static final int SUB_COMMAND	= ApplicationCommandOption.Type.SUB_COMMAND.getValue();
    public static final int SUB_COMMAND_GROUP = ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue();
    public static final int STRING = ApplicationCommandOption.Type.STRING.getValue();
    public static final int INTEGER = ApplicationCommandOption.Type.INTEGER.getValue();
    public static final int BOOLEAN = ApplicationCommandOption.Type.BOOLEAN.getValue();
    public static final int USER = ApplicationCommandOption.Type.USER.getValue();
    public static final int CHANNEL = ApplicationCommandOption.Type.CHANNEL.getValue();
    public static final int ROLE = ApplicationCommandOption.Type.ROLE.getValue();
    public static final int MENTIONABLE = ApplicationCommandOption.Type.MENTIONABLE.getValue();
    public static final int NUMBER = ApplicationCommandOption.Type.NUMBER.getValue();
    public static final int ATTACHMENT = ApplicationCommandOption.Type.ATTACHMENT.getValue();

    private static final Map<Class<?>, Integer> paramClassToCode = new HashMap<>() {
        {
            put(long.class, OptionType.INTEGER);
            put(Long.class, OptionType.INTEGER);
            put(Integer.class, OptionType.INTEGER);
            put(int.class, OptionType.INTEGER);
            put(boolean.class, OptionType.BOOLEAN);
            put(Boolean.class, OptionType.BOOLEAN);
            put(String.class, OptionType.STRING);
            put(User.class, OptionType.USER);
            put(MessageChannel.class, OptionType.CHANNEL);
            put(Role.class, OptionType.ROLE);
            put(double.class, OptionType.NUMBER);
            put(Double.class, OptionType.NUMBER);
            put(Float.class, OptionType.NUMBER);
        }
    };

    public static int getCodeOfClass(Class<?> clazz) {
        return paramClassToCode.get(clazz);
    }
}
