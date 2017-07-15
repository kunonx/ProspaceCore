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
package net.prospacecraft.ProspaceCore

import net.prospacecraft.ProspaceCore.plugin.ProspaceBundlePlugin

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask

import java.util.HashSet

abstract class ActivateFramework : Handle, Runnable, Listener
{
    var delay  : Long        = 0L
    var period : Long        = 0L
    var isSync : Boolean     = true
    var task   : BukkitTask? = null
        private set
    val taskId : Int
        get() = if (this.task == null) -1 else this.task!!.taskId

    var activePlugin: ProspaceBundlePlugin? = null
        private set

    fun hasActivePlugin(): Boolean = this.activePlugin != null

    fun setPlugin(plugin: ProspaceBundlePlugin)
    {
        if (this.hasActivePlugin()) return
        this.activePlugin = plugin
    }

    override fun setEnable(plugin: ProspaceBundlePlugin?)
    {
        this.activePlugin = plugin
        this.setEnable(plugin != null)
    }

    override fun setEnable(active: Boolean)
    {
        this.preLoad(active)
        this.loadRegisterListener(active)
        this.setActivationTask(active)
        this.finLoad(active)

        if (active)
        {
            if (!this.isActivated()) registeredFramework.add(this)
        }
        else
        {
            if (this.isActivated())  registeredFramework.remove(this)
        }
    }

    override fun equals(obj: Any?): Boolean
    {
        if (obj == null) return false
        if (obj is ActivateFramework)
        {
            val core = obj
            return core.task === this.task && core.activePlugin === this.activePlugin
        }
        return false
    }

    override fun isActivated(): Boolean = ActivateFramework.allFramework.any { it == this }

    fun setActivationTask(active: Boolean)
    {
        if (active)
        {
            if (this.activePlugin!!.isEnabled)
                if (this.isSync)
                {
                    this.task = Bukkit.getScheduler().runTaskTimer(this.activePlugin, this, this.delay, this.period)
                }
                else
                {
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this.activePlugin, this, this.delay, this.period)
                }
        }
        else
        {
            if (this.task != null)
            {
                this.task!!.cancel()
                this.task = null
            }
        }
    }

    fun loadRegisterListener(active: Boolean)
    {
        if (active)
        {
            val plugin : ProspaceBundlePlugin = this.activePlugin!!
            if (plugin.isEnabled) Bukkit.getPluginManager().registerEvents(this, this.activePlugin)
        }
        else
        {
            HandlerList.unregisterAll(this)
        }
    }

    protected fun preLoad(active: Boolean) {}


    protected fun finLoad(active: Boolean) {}

    override fun run() {}

    /**
     * Call the action method synchronously.
     */
    @Synchronized fun sync() = this.run()

    companion object
    {
        private val registeredFramework = HashSet<ActivateFramework>()
        val allFramework: Set<ActivateFramework>
            get() = registeredFramework
    }
}