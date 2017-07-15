package net.prospacecraft.ProspaceCore;

import net.prospacecraft.ProspaceCore.message.Prefix;
import net.prospacecraft.ProspaceCore.plugin.ProspaceBundlePlugin;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProspaceCore extends ProspaceBundlePlugin
{
    private transient List<Object> pluginBundleList = new ArrayList<>();
    @Override @Nullable
    public Object getBundle(@Nullable Object... arguments) { return this.pluginBundleList; }

    @Override
    protected boolean onEnableInner(Object bundleInstance)
    {
        super.onEnableInner(this);
        this.setPluginPrefix(new Prefix("&f[&bProspaceCore&f] "));
        return true;
    }

    @Override
    protected boolean onDisableInner()
    {
        return super.onDisableInner();
    }
}
