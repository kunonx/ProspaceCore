package net.prospacecraft.ProspaceCore.message

import net.minecraft.server.v1_12_R1.IChatBaseComponent
import net.minecraft.server.v1_12_R1.PacketPlayOutChat
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

import net.prospacecraft.ProspaceCore.plugin.PluginProperty

import net.prospacecraft.ProspaceCore.util.StringUtil

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class PluginMessage
{
    enum class MsgLevel constructor(val code: Int, val color: String)
    {
        ERROR(0, "&4"),
        DANGER(1, "&c"),
        WARNING(2, "&e"),
        COMMON(3, "&f"),
        MESSAGE(4, "&a"),
        UNKNOWN(5, "&8")
    }

    enum class Status constructor(val statColor: String, val stat: String)
    {
        NONE("", ""),
        CHANGED("%b","CHANGED"),
        FAILED("&c","FAILED")
    }


    var prefix: String? = null

    var suffix: String? = null

    constructor(prefix: String)
    {
        this.prefix = StringUtil.Color(prefix)
    }

    constructor(prefix: Prefix)
    {
        this.prefix = prefix.name
    }

    constructor(prefix: String, suffix: String)
    {
        this.prefix = prefix
        this.suffix = suffix
    }

    fun send(sender: CommandSender, message: String)
    {
        this.send(sender, message, null)
    }

    fun send(sender: CommandSender, message: String, vararg values: Any?)
    {
        var message = message
        if (this.prefix != null) message = prefix!! + message
        if (this.suffix != null) message += suffix
        message = StringUtil.replaceValue(message, values)
        sender.sendMessage(StringUtil.Color(message))
    }

    fun sendToConsole(message: String)
    {
        this.sendToConsole(message, null)
    }

    fun sendToConsole(message: String, vararg values: Any?)
    {
        this.sendToConsole(Status.NONE, message, values)
    }

    inline fun sendToConsole(status: Status = Status.NONE, msg: String, vararg values: Any?)
    {
        var message = msg
        if (this.prefix != null) message = prefix!! + message
        if (this.suffix != null) message += suffix

        Bukkit.getConsoleSender().sendMessage(StringUtil.Color(StringUtil.replaceValue(status.statColor + "[" + status.stat + "] " + message, values)))
    }


    companion object
    {
        fun sendTxt(sender: CommandSender, message: String, vararg values: Any?)
        {
            sender.sendMessage(StringUtil.Color(StringUtil.replaceValue(message, values)))
        }

        inline fun sendTxt(plugin: PluginProperty, sender: CommandSender, message: String, vararg values: Any)
        {
            var message = message
            if (plugin != null) message = plugin.getPluginPrefix()!!.name + message
            sender.sendMessage(StringUtil.Color(StringUtil.replaceValue(message, values)))
        }

        fun log(plugin: JavaPlugin, message: String)
        {
            PluginMessage.log(plugin, MsgLevel.MESSAGE, message)
        }

        fun log(plugin: JavaPlugin, level: MsgLevel, message: String)
        {
            //val file = File(plugin.dataFolder.parentFile + "/DesignFramework_Log/" + ".log")
        }

        fun console(plugin: PluginProperty, message: String, vararg values: Any? = null!!)
        {
            plugin.getPluginMessage()!!.sendToConsole(message, values)
        }

        fun sendActionMessage(p: Player, message: String)
        {
            val icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', message) + "\"}")
            val bar : PacketPlayOutChat = PacketPlayOutChat(icbc)
            (p as CraftPlayer).handle.playerConnection.sendPacket(bar)
        }

        /* For designFramework source code.
           up-to-date.

        fun sendBossBarMessage(player: Player, color: BarColor, style: BarStyle, message: String, process: Double, time: Int)
        {
            val b = Bukkit.createBossBar(message, color, style, *arrayOfNulls<BarFlag>(0))
            b.addPlayer(player)
            val s = BossBarTimer(b, time, process)
            s.runTaskTimer(DesignFramework.getInstance(), 5L, 1L)
        }
*/
        fun sendTitleMessage(p: Player, fadein: Int, time: Int, fadeout: Int, main: String, sub: String)
        {
            title(main, p, fadein, time, fadeout)
            subTitle(sub, p, fadein, time, fadeout)
        }

        fun title(message: String, p: Player, fadein: Int, time: Int, fadeout: Int)
        {
            val title = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"$message\"}"), fadein, time, fadeout)
            (p as CraftPlayer).handle.playerConnection.sendPacket(title)
        }

        fun subTitle(message: String, p: Player, fadein: Int, time: Int, fadeout: Int)
        {
            val subtitle = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"$message\"}"), fadein, time, fadeout)
            (p as CraftPlayer).handle.playerConnection.sendPacket(subtitle)
        }
    }
}