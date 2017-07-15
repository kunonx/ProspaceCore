package net.prospacecraft.ProspaceCore.plugin

import net.prospacecraft.ProspaceCore.message.PluginMessage
import net.prospacecraft.ProspaceCore.message.Prefix

/**
 * PluginProperty can indirectly refer to the properties of plugins based on the ProspaceBundlePlugin class.
 * If you decide that it does not matter which plugin property is exposed to the outside world, add a method to this interface.
 * @since 1.0.0-SNAPSHOT
 * @author kunonx
 */
interface PluginProperty
{
    fun getPluginPrefix() : Prefix?

    fun getSystemEnableMills() : Long

    fun getSystemDisableMills() : Long

    fun getPluginMessage() : PluginMessage?

    fun reload() : kotlin.Boolean

    fun setActivateHandle(vararg handleObject: kotlin.Any?) : Unit
}