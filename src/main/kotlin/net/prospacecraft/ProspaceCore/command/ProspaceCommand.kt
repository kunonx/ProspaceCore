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

import org.bukkit.ChatColor as BukkitChatColor
import org.bukkit.command.CommandSender

import java.lang.reflect.ParameterizedType

/**
 * ProspaceCommand is an abstraction command skeleton that allows you to register a command
 * directly to Bukkit without any setting. This is an extended of original function, which
 * allows the developer to easily skip the complex process of registering commands in the
 * game and do it easily.
 *
 * It uses a self-referencing generic to avoid errors in grammar settings. The developer can
 * determine whether the class is activated by Handle. It cannot be used even if the command is
 * registered with Bukkit when It's deactivated.<br><br>
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

    /**
     * Permission is a class created by subdividing the functions of the privileges used in the game.
     * @author Kunonx
     * @since 1.0.0-SNAPSHOT
     */
    @Suppress("UNREACHABLE_CODE")
    class Permission
    {
        var name : String = null!!
            set(value)
            {
                val str = if(value.startsWith('.')) value.substring(1) else value
                this.name = str
            }

        var defaultOP : Boolean = true

        var usableConsole : Boolean = true

        var usablePlayer : Boolean = true

        constructor(name: String, defaultOP : Boolean = true)
        {
            name = name.trimMargin()
            if(name.startsWith('.')) name = name.substring(1)
            this.name = name
            this.defaultOP = defaultOP
        }

        companion object
        {
            val DEINED_PERM_COLORSET  : String = "&c"
            val ALLOWED_PERM_COLORSET : String = "&a"
        }

        fun isDefaultOP() : Boolean = defaultOP

        fun hasPermission(sender : CommandSender) : Boolean = sender.hasPermission(this.name)

        fun getPermissionName(target : CommandSender? = null) : String
        {
            target ?: return name
            target.let {
                val colorSet : String = null!!
                if(target.isOp) colorSet = ALLOWED_PERM_COLORSET
                else colorSet = if(this.hasPermission(target)) ALLOWED_PERM_COLORSET else DEINED_PERM_COLORSET
                return BukkitChatColor.translateAlternateColorCodes('&',colorSet + name)
            }
        }
    }

    // Contains all currently registered command classes.
    // It uses this to access the command list to indirectly modify or read the information.
    companion object @Transient private
    val registerCommands : MutableList<ProspaceCommand<*>> = ArrayList()

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


    private var mainCommand : String = null!!

    private var aliasCommand : MutableList<String> = ArrayList()
    fun hasAliasCommand(alias : String) : Boolean = aliasCommand.contains(alias.toLowerCase())

    /**
     * This is a command for help page.
     * It prints all the sub-commands available in that class.
     * If there is no value in Main Command, return value is null.
     * The output format is as follows: <br>
     * <ROOT_COMMAND> <SUB_MAIN_COMMAND> <SUB_SUB_MAIN_COMMAND> ..... <CURRENT_ALL_COMMAND>
     */
    fun getRelativeCommand(command : ProspaceCommand<*>?, label : String? = null, isMain : Boolean = false) : String?
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
        return if(command.isRoot()) subLabel else this.getRelativeCommand(command.parentCommand, subLabel, false)
    }

    /**
     * This is a command for help page.
     * Returns all commands in the class with commas.
     * The output format is as follows: <br>
     * <MAIN_COMMAND>,<ALIAS_COMMAND>,<ALIAS_COMMAND2>, ... ,<ALIAS_COMMAND>
     */
    fun getAllCommands(): String
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
    private var childCommand : MutableList<ProspaceCommand<*>> = ArrayList()
    fun hasChildCommand() : Boolean = when(childCommand.size) { 0 -> false; else -> true }
    fun getChildCommand(cmd : String) : ProspaceCommand<*>?
    {
        if (this.hasChildCommand()) return null
        return this.childCommand.firstOrNull { it.mainCommand.equals(cmd, true) || it.hasAliasCommand(cmd) }
    }
    fun getChildCommands() : MutableList<ProspaceCommand<*>> = childCommand


    // The parent class of this class.
    // This connects the commands of that class to the parent class in tree form.
    protected var parentCommand : ProspaceCommand<*>? = null
        private set
    fun hasParent() : Boolean = this.parentCommand != null
    fun setParent(parent : ProspaceCommand<*>) { this.parentCommand = parent }
    fun isRoot(): Boolean = this.parentCommand == null

    // The Permission for this command.
    // The Permission value of the child class is used in conjunction with the parent
    // Permission in the parent class.
    private var permission : Permission? = null

    /**
     * Get the Permission for this class.
     * It takes the permission value of this command and is not affected by the parent
     * class's information.
     */
    fun getPermission() : Permission? = this.permission

    /**
     *
     */
    fun getPermissionValue() : String
    {
        return null!!
    }

    /**
     *
     */
    fun hasPermission() : Boolean = this.permission != null

    /**
     * Specifies a permission value. This can be affected by the value of the parent class.
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
     *
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
     * The output of this code will be "prospacecore.run". This shows that the value can vary<br>
     * depending on the parent class.<br>
     * That is, the child information changes automatically according to the parent value and<br>
     * you will need to enter <code>/ps run</code> to use the ChildCommand's command.<br>
     */
    protected fun setPermission(perm : Permission)
    {
        if(this.hasParent())
        {
            if(! this.hasChildCommand())
            {
                this.permission = perm
                return
            }
            else
            {

            }
        }
        else
        {
            if (this.parentCommand!!.permission == null)
            {
                this.permission = perm
                return
            }
            val parentPerm: Permission = this.parentCommand!!.permission!!

            perm.name = parentPerm.name + "." + perm.name
            perm.defaultOP = parentPerm.defaultOP

            this.permission = perm
        }
    }

    /**
     * Specifies a permission value. This can be affected by the value of the parent class.
     * @see setPermission(Permission)
     */
    protected fun setPermission(perm : String)
    {
        return setPermission(Permission(perm, true))
    }

    fun execute(sender: CommandSender, args: MutableList<String>) : Boolean
    {
        return false
    }

    override fun perform(sender: CommandSender, args: MutableList<String>?): Boolean = true
}
