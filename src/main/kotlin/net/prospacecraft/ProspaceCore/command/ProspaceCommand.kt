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
@file:Suppress("UNREACHABLE_CODE")
package net.prospacecraft.ProspaceCore.command

import net.prospacecraft.ProspaceCore.Handle
import net.prospacecraft.ProspaceCore.Permission
import net.prospacecraft.ProspaceCore.message.Prefix
import net.prospacecraft.ProspaceCore.plugin.ProspaceBundlePlugin

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
open abstract class ProspaceCommand<T : ProspaceCommand<T>> : Handle, CommandExecutable
{

    // Contains all currently registered command classes.
    // It uses this to access the command list to indirectly modify or read the information.
    companion object @Transient private
    val registerCommands : MutableList<ProspaceCommand<*>> = ArrayList()

    // This is a generic object of this class. It contains information about the superclass.
    private var genericClazz : Class<T> = null!!
        private get

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
        catch(e : InstantiationException)
        {
            e.printStackTrace()
            return null
        }
        catch(e : IllegalAccessException)
        {
            e.printStackTrace()
            return null
        }
    }

    protected var prefix : Prefix? = null
    fun hasPrefix() : Boolean = this.prefix != null

    private var permission : Permission? = null
        get
    fun setPermission(value : String)
    {
        this.permission = Permission(value)
    }



    @Suppress("UNCHECKED_CAST", "ConvertSecondaryConstructorToPrimary")
    constructor()
    {
        this.genericClazz = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    }

    override fun isActivated(): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.t
    }

    override fun setEnable(plugin: Boolean)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun perform(sender: CommandSender?, args: MutableList<String>?): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setEnable(plugin: ProspaceBundlePlugin?)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}