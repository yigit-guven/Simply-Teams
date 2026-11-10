package net.yigitguven.simply_teams.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.yigitguven.simply_teams.SimplyTeams;

import java.lang.reflect.Field;
import java.util.Map;

public class VanillaTeamRemover {
    public static void removeVanillaTeam(CommandDispatcher<CommandSourceStack> dispatcher) {
        try {
            RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();

            Field childrenField = CommandNode.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ?> children = (Map<String, ?>) childrenField.get(root);
            if (children != null) {
                children.remove("team");
            }

            Field literalsField = CommandNode.class.getDeclaredField("literals");
            literalsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ?> literals = (Map<String, ?>) literalsField.get(root);
            if (literals != null) {
                literals.remove("team");
            }

            SimplyTeams.LOGGER.debug("Removed vanilla team command node.");
        } catch (Exception e) {
            SimplyTeams.LOGGER.error("Failed to remove vanilla team command node", e);
        }
    }
}
