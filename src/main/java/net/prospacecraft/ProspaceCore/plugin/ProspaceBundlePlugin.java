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
package net.prospacecraft.ProspaceCore.plugin;

import net.prospacecraft.ProspaceCore.ClassProcessor;
import net.prospacecraft.ProspaceCore.Handle;
import net.prospacecraft.ProspaceCore.bundle.Bundle;
import net.prospacecraft.ProspaceCore.command.ProspaceCommand;
import net.prospacecraft.ProspaceCore.config.DataConfiguration;
import net.prospacecraft.ProspaceCore.data.MultipleHashMap;
import net.prospacecraft.ProspaceCore.data.MultipleMap;
import net.prospacecraft.ProspaceCore.profile.BundleClasses;
import net.prospacecraft.ProspaceCore.message.PluginMessage;
import net.prospacecraft.ProspaceCore.message.Prefix;
import net.prospacecraft.ProspaceCore.util.ReflectionUtil;
import net.prospacecraft.ProspaceCore.util.StringUtil;
import static net.prospacecraft.ProspaceCore.profile.Defined.NATIVE_PROCESSOR_DRIVER_NAME;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ProspaceBundlePlugin adds functionality from existing plugin bases and helps you manage plugins more efficiently.
 * A plugin class based on this can have a return value of Bundle, which can be very useful when you need to pass
 * certain values while using the plugin.<br>
 * <br>
 * If you want to write a main class based on this, Inherit this, passing the base property to the child class and
 * passing the information of the child class through <code>OnEnableInner(Object)</code> to the method of
 * the parent class.<br>
 *
 * @since 1.0.0-SNAPSHOT
 * @author kunonx
 * @see net.prospacecraft.ProspaceCore.bundle.Bundle#getBundle(Object...)
 * @see net.prospacecraft.ProspaceCore.plugin.PluginProperty
 */
public abstract class ProspaceBundlePlugin extends JavaPlugin implements Bundle, PluginProperty
{
    // Based on the bundle class, the handle object is stored in this list when activated through some plugin.
    // This indirectly stores information about the handle object.
    private static transient final MultipleMap<ProspaceBundlePlugin, Class<?>>
            pluginHandledClasses = new MultipleHashMap<>();

    private static final MultipleHashMap<ProspaceBundlePlugin, String> pluginNativeLibraries = new MultipleHashMap<>();

    /**
     * Returns all handle objects registered in a plugin based on ProspaceBundlePlugin.
     * @return All registered handle objects
     */
    public static MultipleMap<ProspaceBundlePlugin, Class<?>> getPluginHandledClasses() { return pluginHandledClasses; }

    /**
     * Returns the handle object indirectly.
     * @param plugin The plugin to get the handle object
     * @return Registered handle object collection
     */
    @Nullable
    public static Collection<Class<?>> getHandledClasses(ProspaceBundlePlugin plugin)
    {
        Collection<Class<?>> unmodifiableColl = pluginHandledClasses.get(plugin);
        if (unmodifiableColl != null)
        {
            unmodifiableColl = Collections.unmodifiableCollection(unmodifiableColl);
        }
        return unmodifiableColl;
    }

    /**
     * Import the native module.
     * If there is no critical module, the plugin will not work properly, debugging and determine the cause.
     */
    protected void installDLL()
    {
        List<String> loadedNativeLibrary = pluginNativeLibraries.get(this);
        if(!loadedNativeLibrary.contains(NATIVE_PROCESSOR_DRIVER_NAME + ".dll"))
        {
            System.loadLibrary(new File(this.getDataFolder(), NATIVE_PROCESSOR_DRIVER_NAME + ".dll").toString());
        }
    }

    // The first time you activated the plugin.
    // If the value is -1, it means that it is not activated normally.
    private long SystemEnabledMillis = -1L;
    @Override public long getSystemEnableMills()      { return this.SystemEnabledMillis; }

    // The first time you deactivated the plugin.
    // If the value is -1, it means that it is not deactivated normally.
    private long SystemDisabledMillis = -1L;
    @Override public long getSystemDisableMills()     { return this.SystemDisabledMillis; }

    // The prefix for this plugin.
    // This is automatically changed by the child class.
    private Prefix prefix;
    @Nullable @Override
    public Prefix getPluginPrefix()                   { return this.prefix; }
    protected void setPluginPrefix(Prefix prefix)     { this.prefix = prefix; }

    // Use this plugin to send messages to a game or console window.
    // It gets the information from the child class and is finally finalized.
    // The object is created by the base plugin and can not be modified externally.
    private PluginMessage pluginMessage;
    @Nullable @Override
    public PluginMessage getPluginMessage()           { return this.pluginMessage; }

    // Hooks and fetches information about child classes.
    // This will be required for the plugin to work properly.
    // To use this value normally, use super.onEnableInner(this) in your child class to send the value.
    private Bundle subPluginBundler = null;
    public Bundle getPluginBundler()                  { return subPluginBundler; }

    private DataConfiguration pluginConfiguration = null;
    public DataConfiguration getPluginConfiguration() { return this.pluginConfiguration; }

    /**
     * Process enabling the plugin first before activating them.
     * You can override this method to create the desired action in the child class.
     * @param bundleInstance The value that various information to base class
     * @return Returns true if the plugin has been successfully activated. Otherwise, returns false.
     */
    protected boolean onEnableInner(Object bundleInstance)
    {
        if(bundleInstance != null)
        {
            this.getPluginMessage().sendToConsole("&eCalling ProspaceBundlePluginLoader v{0}", this.getDescription().getVersion());
            if(bundleInstance instanceof JavaPlugin)
            {
                if(bundleInstance.getClass().isAssignableFrom(BundleClasses.Companion.getBundleClass()))
                {
                    this.getPluginMessage().sendToConsole("&eDetected BundlePluginLoader");
                    // Import the plugin bundle into a list and process it in the class(ProspaceBundlePlugin).
                    this.subPluginBundler = (Bundle)bundleInstance;
                    List<?> bundledList = (List<?>) this.subPluginBundler.getBundle();
                    this.processBundleList(bundledList);
                }
            }
        }
        return true;
    }

    /**
     * Allows the base plugin to process bundle values.
     * This can be overridden to analyze various information.
     * @param bundledList The bundle value received from child base plugin
     */
    protected void processBundleList(List<?> bundledList)
    {
        for (int i = 0; i < bundledList.size(); i++)
        {
            Object bundled = bundledList.get(i);
            if(bundled instanceof Prefix)
            {
                Prefix prefix = (Prefix) bundled;
                this.getPluginMessage().sendToConsole(PluginMessage.Status.CHANGED,
                                            "&Prefix change detected : {0} &f-> {1}", this.prefix.getName(), prefix.getName());
                this.prefix = prefix;
            }
        }
    }

    /**
     * The first method to run before deactivating the plugin.
     * @return The method is successfully executed
     */
    protected boolean onDisableInner()
    {
        return true;
    }

    /**
     * Run when the plugin is first activated.
     */
    private synchronized void onEnableInit()
    {
        this.getDataFolder().mkdir();
    }

    /**
     * Returns a config file based on JavaPlugin. <b>Not recommand.</b>
     * @return The config file type of the plugin
     * @deprecated The method's not used by this plugin, Not implemented. Use {{@link #getPluginConfiguration()}} instead.
     */
    @Override
    public FileConfiguration getConfig() { return super.getConfig(); }

    /**
     * Hooks the handle object to the plug-in to manage it, and activates it.
     * This is done by getting an instance of that class and invoking the method.
     * @param handleObjects
     */
    @SuppressWarnings("ConstantConditions")
    public void setActivateHandle(@NotNull Object... handleObjects)
    {
        int i = 0;
        for(Object o : handleObjects)
        {
            if(o instanceof Handle)
            {
                Handle handle = (Handle)o;
                handle.setEnable(this);
                getPluginHandledClasses().putSingleFirst(this, handle.getClass());
            }
            else
            {
                Class<?> clazz = (Class<?>)o;
                if(! Handle.class.isAssignableFrom(clazz))
                {
                    throw new IllegalArgumentException(clazz.getName() + " isn't Handle object. Ignoring it");
                }
                else
                {
                    Object instance = ReflectionUtil.getInstance(clazz);
                    Handle handle = (Handle)instance;
                    handle.setEnable(this);
                    if(ProspaceCommand.class.isAssignableFrom(clazz)) i++;
                }
            }
        }
        if(i != 0) this.getPluginMessage().sendToConsole("&e{0} Command classes has been registered", i);
        this.getPluginMessage().sendToConsole("&aRegistered class activation " + handleObjects.length + " core(s)");
    }

    /**
     * Reload the plugin. This can be hooked through an EventHandler.
     * @see org.bukkit.event.EventHandler
     */
    public synchronized boolean reload()
    {
        return true;
    }

    @Override
    public final void onEnable()
    {
        this.SystemEnabledMillis = System.currentTimeMillis();

        // Activates a processor that imports external modules and efficiently processes information from classes.
        // Ignore module if the wrong type of external module or fail to read it. However, The plugin may not work properly.
        this.installDLL();

        // Activate the ClassProcessor through the module.
        // The ClassProcessor is not a required module and does not need to have a corresponding JNI library.
        ClassProcessor.enableClassProcessor();

        // Set default plugin property. This can be changed by the child bundle.
        // You can create a bundle class in your child class and pass the value to the superclass.
        this.prefix = new Prefix(StringUtil.Companion.getColorHash(this.getDescription().getName()) + "[" + this.getDescription().getName() + "] ");
        this.pluginMessage = new PluginMessage(this.prefix);

        if(this.isEnableFirst()) this.onEnableInit();

        this.onEnableInner(null);

        this.finLoad();
    }

    @Override
    public final void onDisable()
    {
        this.SystemDisabledMillis = System.currentTimeMillis();
        ClassProcessor.disableClassProcessor();
        this.onDisableInner();
        this.finUnload();
    }

    /**
     * Gets the plugin default configuration filepath.
     * @return The object that config file type
     */
    public File getConfigFile() { return this.getPluginConfiguration().getFileType(); }

    /**
     * Checks to see if the plugin is disabled.
     * @return true if the plugin is disabled, Otherwise false
     */
    public boolean isDisabled() { return !this.isEnabled(); }

    /**
     *
     * @return
     */
    public boolean isEnableFirst()
    {
        return !this.getDataFolder().exists();
    }

    // It's not implemented at all because it is not currently used.
    // The following methods can be overridden if necessary.

    /**
     * Execute immediately before activating.
     * In other words, when the method is done, the plugin will be activated normally.
     */
    protected void finLoad() {}

    /**
     * Run it just before deactivating the plugin.
     * In other words, when the method is done, the plugin will be deactivated.
     */
    protected void finUnload() {}
}
