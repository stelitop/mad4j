package net.stelitop.mad4j.commands.autocomplete;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class InputSuggestion {

    private final @NotNull String name;
    private final @NotNull Object value;

    private InputSuggestion() {
        this("Default", "Default");
    }

    private InputSuggestion(@NotNull String name, @NotNull Object value) {
        this.name = name;
        this.value = value;
    }

    public static InputSuggestion create(@NotNull String name, @NotNull Object value) {
        return new InputSuggestion(name, value);
    }
}
