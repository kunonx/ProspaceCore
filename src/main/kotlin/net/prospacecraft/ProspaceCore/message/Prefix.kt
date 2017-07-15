package net.prospacecraft.ProspaceCore.message

import net.prospacecraft.ProspaceCore.security.IntegrityChecker
import net.prospacecraft.ProspaceCore.util.StringUtil
import org.bukkit.ChatColor

import java.security.NoSuchAlgorithmException

data class Prefix(val str: String? = null)
{
    operator fun plus(str2 : String?) : Prefix
    {
        return Prefix(str + str2)
    }


    var name: String? = null
    fun hasName(): Boolean
    {
        return this.name != null
    }

    fun hasColor(): Boolean
    {
        return name!!.equals(ChatColor.stripColor(name), ignoreCase = true)
    }

    init
    {
        this.name = str
    }

    fun getString(message: String): String
    {
        return StringUtil.Color(this.name!!) + message
    }

    override fun toString(): String
    {
        try
        {
            return this.name + "@" + IntegrityChecker.sha1(this.name)
        }
        catch (e: NoSuchAlgorithmException)
        {
            return null!!
        }
    }
}