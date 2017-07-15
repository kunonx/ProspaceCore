package net.prospacecraft.ProspaceCore.profile

import net.prospacecraft.ProspaceCore.bundle.Bundle
import net.prospacecraft.ProspaceCore.plugin.PluginProperty

/**
 *
 */
sealed class BundleClasses
{
    companion object
    {
        inline fun getPropertyClass() : Class<*> = PluginProperty::class.java

        inline fun getBundleClass() : Class<*> = Bundle::class.java
    }
}