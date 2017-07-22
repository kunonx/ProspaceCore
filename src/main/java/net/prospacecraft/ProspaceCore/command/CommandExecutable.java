package net.prospacecraft.ProspaceCore.command;

import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandExecutable
{
    /**
     *
     * The perform function is a function that is executed when the command is run. This is equivalent to the role of OnCommand.
     * This is implemented by overriding it in the subclass. If you don't need to do, you don't have to it.
     *
     * @param sender Source of the command
     * @param args Passed command arguments, List type
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(CommandSender, org.bukkit.command.Command, String, String[])
     * @see net.prospacecraft.ProspaceCore.command.ProspaceCommand#execute(CommandSender, java.util.List)
     * @since 1.0.0
     * @author Kunonx
     */
    boolean perform(@NotNull CommandSender sender, @Nullable List<String> args);
}