package net.yigitguven.simply_teams.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ColorArgumentHelper {
    private static final List<String> COLOR_NAMES = Arrays.stream(ChatFormatting.values())
            .filter(ChatFormatting::isColor)
            .map(ChatFormatting::getName)
            .collect(Collectors.toList());

    public static SuggestionProvider<CommandSourceStack> SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(COLOR_NAMES, builder);

    public static ChatFormatting parseColor(String input) {
        if (input == null) return null;
        return ChatFormatting.getByName(input.toLowerCase());
    }

    public static List<String> getColorNames() {
        return COLOR_NAMES;
    }
}

