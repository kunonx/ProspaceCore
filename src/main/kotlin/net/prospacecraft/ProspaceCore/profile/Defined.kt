@file:JvmName("Defined")
package net.prospacecraft.ProspaceCore.profile

/**
 * Defined is an object that lists the fixed values used by the plugin.
 * You can also use this object in Java, if you want to change a specific value, see the field value of this class.
 * If it's a frequently used value, A good idea to define a new value for this.
 *
 * @since 1.0.0-SNAPSHOT
 * @author kunonx
 */
object Defined
{
    enum class ConfigFormat constructor(val format: String)
    {
        JSON(".json"),
        YAML(".yml"),
        TXT(".txt"),
        UNKNOWN(null!!)
    }

    const val NATIVE_PROCESSOR_DRIVER_NAME: String = "ProspaceCoreProcessor"

    const val BUNDLE_PACKAGE_NAME: String = "net.prospacecraft.ProspaceCore.bundle"

    const val PLUGIN_CONFIG_FILENAME: String = "config.json"

    const val COMMAND_MAXIMUM_LIST: Int = 7
}