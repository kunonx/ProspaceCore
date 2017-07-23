/*
Copyright (c) 2017 Prospacecraft Network

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package net.prospacecraft.ProspaceCore.command

import net.prospacecraft.ProspaceCore.message.FancyMessage
import net.prospacecraft.ProspaceCore.util.StringUtil
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.lang.reflect.ParameterizedType

typealias PCommandType = ProspaceCommand<*>

/**
 * ProspaceCommand is an abstraction command skeleton that allows you to register a command
 * directly to Bukkit without any setting. This is an extended of original function, which
 * allows the developer to easily skip the complex process of registering commands in the
 * game and do it easily.<br>
 *
 * It uses a self-referencing generic to avoid errors in grammar settings. The developer can
 * determine whether the class is activated by Handle. It cannot be used even if the command
 * is registered with Bukkit when It's deactivated.<br><br>
 *
 * <b>About self-referencing generic class</b>
 * http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#FAQ206
 *
 * @author Kunonx
 * @since 1.0.0-SNAPSHOT
 * @version 1.0.0
 * @param T The class type that inherits from ProspaceCommand, Preventing Inheritance error
 */
@Suppress("UNCHECKED_CAST")
open abstract class ProspaceCommand<T : ProspaceCommand<T>> : CommandExecutable
{
    object CommandColorSet
    {
        const val DEINED_PERM_COLORSET       : String = "&c"

        const val ALLOWED_PERM_COLORSET      : String = "&a"

        const val UNAVAILABLE_COLORSET       : String = "&9"

        const val AVAILABLE_COLORSET         : String = "&b"

        const val PARAM_REQUIREMENT_COLORSET : String = "&7"

        const val PARAM_OPTIONAL_COLORSET    : String = "&8"
    }

    // Contains all currently registered command classes.
    // It uses this to access the command list to indirectly modify or read the information.
    companion object @Transient private
    val registerCommands : MutableList<PCommandType> = ArrayList()

    // This is a generic object of this class. It contains information about the superclass.
    private lateinit var genericClazz : Class<T>
        private get
    init { this.genericClazz = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T> }

    /**
     * Gets the generic type of the command.
     * @return The generic class type
     */
    fun getGenericType(): Class<T> { return this.genericClazz }

    /**
     * Gets an instance of this generic. It cans get the information of the upper command class.
     * @return The generic type
     * @exception InstantiationException Failed to create instance object
     * @exception IllegalAccessException Approaching an unacceptable area
     */
    fun getGenericInstance() : T?
    {
        try
        {
            val generic : T = this.genericClazz.newInstance()
            return generic
        }
        catch(e : InstantiationException) { e.printStackTrace() }
        catch(e : IllegalAccessException) { e.printStackTrace() }
        return null
    }

    internal class CommandMessage(var message : String)
    {
        private val messageBuilder : FancyMessage = FancyMessage(message)
        fun getMessageBuilder() : FancyMessage = messageBuilder

        private val desc : MutableList<String> = ArrayList()
        fun getDescription() : MutableList<String> = desc

        @Suppress("IMPLICIT_CAST_TO_ANY")
        fun addMessage(message : String, index : Int = -1) = when (index) {
                -1   -> desc.add(ChatColor.translateAlternateColorCodes('&', message))
                else -> desc.add(index, message) }

        fun send(sender : CommandSender)
        {
            messageBuilder.tooltip(desc.asIterable())
            messageBuilder.send(sender)
        }
    }

    //TODO
    class Parameter(var param : String, var requirement : Boolean, var allowConsole : Boolean = true, var allowPlayer  : Boolean = true)
    {
        private var permission : String? = null

        private var childParameter : Parameter? = null

        companion object
        {
            val REQUIREMENT_FORMAT : String = CommandColorSet.PARAM_REQUIREMENT_COLORSET + "<%s>"

            val OPTIONAL_FORMAT    : String = CommandColorSet.PARAM_OPTIONAL_COLORSET + "[&s]"
        }

        fun isAllowed(sender : CommandSender) : Boolean = when(sender) {
            is Player -> allowPlayer
            is ConsoleCommandSender -> allowConsole
            else -> false
        }

        fun getParamValue(target : CommandSender) : String = when(this.requirement) {
                true  -> String.format(REQUIREMENT_FORMAT, this.param)
                false -> String.format(OPTIONAL_FORMAT, this.param)
        }
    }

    //
    protected var mainCommand : String               = null!!

    //
    protected var aliasCommand : MutableList<String> = ArrayList()
    fun hasAliasCommand(alias : String) : Boolean    = aliasCommand.contains(alias.toLowerCase())

    //
    private var messageBuilder : CommandMessage?     = null

    //
    private var usableConsole : Boolean              = true

    /**
     * Decide if you want to allow the player to use this command.
     * This can also affect child commands.
     */
    fun setAllowUseConsole(usable : Boolean) { this.usableConsole = usable }

    //
    private var usablePlayer : Boolean = true

    /**
     * Decide if you want to allow the player to use this command.
     * This can also affect child commands.
     */
    fun setAllowUsePlayer(usable : Boolean) { this.usablePlayer = usable }

    private var parameter : MutableList<Parameter> = ArrayList()
    fun getParameter()    : MutableList<Parameter> { return this.parameter }
    fun hasParameter()    : Boolean = this.parameter.isEmpty()
    fun hasRequireParam() : Boolean = if(this.parameter.isEmpty()) false else this.parameter[0].requirement

    /**
     * This is a command for help page.
     * It prints all the sub-commands available in that class.
     * If there is no value in Main Command, return value is null.
     * The output format is as follows: <br>
     * <ROOT_COMMAND> <SUB_MAIN_COMMAND> <SUB_SUB_MAIN_COMMAND> ..... <CURRENT_ALL_COMMAND>
     */
    protected fun getRelativeCommand(command : PCommandType?, label : String? = null, isMain : Boolean = false, target : CommandSender? = null) : String?
    {
        var subLabel: String? = label
        command ?: throw RuntimeException("command cannot be null")
        if(command.isRoot())
            subLabel = if(label == null) command.getAllCommands() else command.getAllCommands() + " " + label
        else
            if(command.hasChildCommand())
            {
                if(isMain) subLabel = if(label == null) command.getAllCommands() else command.getAllCommands() + " " + label
                else subLabel = if(label == null) command.mainCommand else command.mainCommand + " " + label
            }
        return if(command.isRoot()) subLabel else this.getRelativeCommand(command.parentCommand, subLabel, false, target)
    }

    /**
     * This is a command for help page.
     * Returns all commands in the class with commas.
     * The output format is as follows: <br>
     * <MAIN_COMMAND>,<ALIAS_COMMAND>,<ALIAS_COMMAND2>, ... ,<ALIAS_COMMAND>
     */
    protected fun getAllCommands(target: CommandSender? = null): String
    {
        var s : String = ""
        val iter : Iterator<String> = this.aliasCommand.iterator()
        while(iter.hasNext())
        {
            val s2 = iter.next()
            s += s2
            if(iter.hasNext()) s += ","
        }
        return s
    }

    // Saves subclasses that operate on this command.
    protected var childCommand : MutableList<PCommandType> = ArrayList()
    fun hasChildCommand() : Boolean = when(childCommand.size) { 0 -> false; else -> true }
    fun getChildCommand(cmd : String) : PCommandType?
    {
        if (this.hasChildCommand()) return null
        return this.childCommand.firstOrNull { it.mainCommand.equals(cmd, true) || it.hasAliasCommand(cmd) }
    }
    fun getChildCommands() : MutableList<PCommandType> = childCommand

    protected var externalCommand : MutableList<PCommandType> = ArrayList()
    fun addExternalCommand(command: PCommandType) { this.externalCommand.add(command)}
    fun getExternalCommands() : MutableList<PCommandType> = externalCommand


    // The parent class of this class.
    // This connects the commands of that class to the parent class in tree form.
    private var parentCommand : PCommandType? = null
    fun hasParent() : Boolean = this.parentCommand != null
    private fun setParent(parent : PCommandType) { this.parentCommand = parent }
    fun isRoot(): Boolean = this.parentCommand == null

    /**
     * Permission is a class created by subdividing the functions of the privileges used in the game.
     * @author Kunonx
     * @since 1.0.0-SNAPSHOT
     */
    @Suppress("UNREACHABLE_CODE")
    class Permission(var name: String, var defaultOP: Boolean = true)
    {
        fun isDefaultOP() : Boolean = defaultOP

        fun hasPermission(sender : CommandSender) : Boolean = sender.hasPermission(this.name)

        fun getPermissionName(target : CommandSender? = null) : String
        {
            target ?: return this.name
            target.let {
                val colorSet : String = null!!
                if(target.isOp) if(this.isDefaultOP()) colorSet = CommandColorSet.ALLOWED_PERM_COLORSET
                else colorSet = if(this.hasPermission(target)) CommandColorSet.ALLOWED_PERM_COLORSET else CommandColorSet.DEINED_PERM_COLORSET
                return ChatColor.translateAlternateColorCodes('&',colorSet + this.name)
            }
        }

        init
        {
            name = name.trimMargin()
            if(name.startsWith('.')) name = name.substring(1)
        }
    }

    // The Permission for this command.
    // The Permission value of the child class is used in conjunction with the parent
    // Permission in the parent class.
    private var permission : Permission? = null

    /**
     * Get the Permission for this class.
     * It takes the permission value of this command and is not affected by the parent
     * class's information.
     * @return
     */
    fun getPermission() : Permission? = this.permission

    /**
     * Gets the Permission value.
     * This is affected by the parent Permission value and it can colorize the string according to the target.
     * @param senderTarget
     * @return
     */
    fun getPermissionValue(senderTarget : CommandSender? = null) : String?
    {
        this.permission ?: return null
        this.parentCommand ?: return this.permission!!.getPermissionName(senderTarget)

        var parentsCmd : PCommandType? = this.parentCommand
        var perm : String = this.permission!!.name

        while(parentsCmd != null)
        {
            perm = parentsCmd.getPermission()!!.name + "." + perm
            parentsCmd = parentsCmd.parentCommand!!
        }
        return if(senderTarget == null) perm else Permission(perm, this.permission!!.defaultOP).getPermissionName(senderTarget)
    }

    /**
     * Check for Permission value. If not, this command will work without any separate permissions.
     * @return true If the permission is null, otherwise false
     */
    fun hasPermission() : Boolean = this.permission != null

    /**
     * Specifies a permission value. This can be affected by the value of the parent class.<br>
     * For example, Here is the code:<br>
     * <pre>
     * <code>
     * class ParentCommand : ProspaceCommand<ParentCommand>
     * {
     *   init
     *   {
     *       this.setCommand("ps")
     *       this.setPermission("prospacecore")
     *       this.addChildCommand(ChildCommand())
     *   }
     * }
     * class ChildCommand : ProspaceCommand<ChildCommand>
     * {
     *   init
     *   {
     *      setCommand("run")
     *      setPermission("child")
     *   }
     * }
     * ...
     * println(new ParentCommand().getChildCommand("run").getPermissionValue())
     * </code>
     * </pre>
     * The output of this code will be <code>"prospacecore.run"</code>. This shows that the value
     * can vary depending on the parent class.<br>
     * That is, the child information changes automatically according to the parent value and
     * need to enter <code>/ps run</code> to use the ChildCommand's command.<br>
     */
    protected fun setPermission(perm : Permission) { this.permission = permission }

    /**
     * Specifies a permission value. This can be affected by the value of the parent class.
     * @see setPermission(Permission)
     */
    protected fun setPermission(perm : String)     { return setPermission(Permission(perm, true)) }

    val MAX_PAGE_SIZE  : Short = 7

    val HEADER_MESSAGE : String = "&e====&f [&b Help commands for &e\"{0}\" &a1/{1} &bpage(s) &f] &e===="

    protected fun sendHelpPage(sender: CommandSender)
    {

        val commands : MutableList<PCommandType> = ArrayList()
        commands.addAll(this.externalCommand)
        commands.addAll(this.childCommand)

        if(commands.size != 0)
        {
            val commandTexts : MutableList<CommandMessage> = ArrayList()
            val max_page = if(sender is ConsoleCommandSender) 1 else (commands.size / (MAX_PAGE_SIZE - 1)) + 1

            // Create a header message.
            val headerMessage : CommandMessage = CommandMessage(StringUtil.replaceValue(HEADER_MESSAGE, this.mainCommand, max_page))
            commandTexts.add(headerMessage)

            // Creates a message to output the information of the command registered in this class.
            var mainCommand : String? = this.getRelativeCommand(this, null, true, sender)
        }
        else
        {

        }
    }

    override fun perform(sender: CommandSender, args: List<String>?) = false
}
