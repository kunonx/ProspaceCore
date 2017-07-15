package net.prospacecraft.ProspaceCore.entity;

import net.prospacecraft.ProspaceCore.Handle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityBase<T> implements Handle
{
    protected final Map<String, EntityBase<?>> entityBases = new ConcurrentHashMap<>();
}
