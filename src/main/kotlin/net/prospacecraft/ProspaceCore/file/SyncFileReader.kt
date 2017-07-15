package net.prospacecraft.ProspaceCore.file

import net.prospacecraft.ProspaceCore.Handle
import net.prospacecraft.ProspaceCore.message.PluginMessage
import net.prospacecraft.ProspaceCore.message.PluginMessage.MsgLevel
import net.prospacecraft.ProspaceCore.plugin.ProspaceBundlePlugin

import java.io.File
import java.util.HashSet

open abstract class SyncFileReader : Handle
{
    var activePlugin: ProspaceBundlePlugin? = null; protected set

    var file: File? = null

    val path: String get() = this.file!!.path

    val folder: File get() = this.file!!.parentFile

    @Synchronized open fun refresh() {}

    fun createDataFolder(path: String): Boolean
    {
        if (!File(activePlugin!!.dataFolder, path).exists())
        {
            PluginMessage.log(this.activePlugin!!, MsgLevel.WARNING, "&c\"$path\" directory not found! Creating the directory")
            File(activePlugin!!.dataFolder, path).mkdirs()
        }

        if (!File(activePlugin!!.dataFolder, path).exists())
        {
            return false
        }
        return true
    }

    fun createDataFolders(vararg paths: String): Boolean
    {
        for (path in paths) {
            if (!File(activePlugin!!.dataFolder, path).exists())
            {
                PluginMessage.log(this.activePlugin!!, MsgLevel.WARNING, "&c\"$path\" directory not found! Creating the directory")
                File(activePlugin!!.dataFolder, path).mkdirs()
            }

            if (!File(activePlugin!!.dataFolder, path).exists())
            {
                return false
            }
        }
        return true
    }

    override fun setEnable(plugin: ProspaceBundlePlugin?) : Unit
    {
        this.activePlugin = plugin
        this.enabled = this.activePlugin != null
    }

    override fun isActivated(): Boolean
    {
        SyncFileReader.register.filter { it is SyncFileReader && it == this }.forEach { return true }
        return false
    }

    var enabled: Boolean
        get()
        {
            SyncFileReader.register.filter { it is SyncFileReader && it == this }.forEach { return true }
            return false
        }
        set(enable)
        {
            if(enable)
                if (!this.enabled) named_instance.add(this)
            else
                if (this.enabled) named_instance.remove(this)
        }

    override fun equals(obj: Any?): Boolean
    {
        if (obj == null) return false
        if (obj is SyncFileReader) return this.file === obj.file else if (obj is File) { return this.file === obj }
        return false
    }

    companion object
    {
        @Transient protected val named_instance: MutableSet<Any> = HashSet()
        val register: Set<Any> get() = named_instance
    }
}