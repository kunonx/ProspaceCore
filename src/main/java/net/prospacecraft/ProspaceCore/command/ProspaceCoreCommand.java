package net.prospacecraft.ProspaceCore.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProspaceCoreCommand extends ProspaceCommand<ProspaceCoreCommand>
{
    public ProspaceCoreCommand()
    {
        this.setAllowUseConsole(false);
        this.setAllowUsePlayer(true);

        this.setPermission("ProspaceCore");

        this.setChildCommand(new ArrayList<>());
        this.setExternalCommand(new ArrayList<>());
        this.setMainCommand("prospacecore");

        this.setAliasCommand(Arrays.asList("ps", "pcore"));

        this.setParameter(new Parameter("param", true, true, true));
    }

    @Override
    public boolean perform(@NotNull CommandSender sender, @Nullable List<String> args)
    {
        if(sender instanceof Player)
        {
            return true;
        }
        return false;
    }
}
