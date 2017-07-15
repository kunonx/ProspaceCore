package net.prospacecraft.ProspaceCore.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public abstract class JsonConfiguration implements JsonSerializer, JsonDeserializer {
    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context)
    {
        return null;
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return null;
    }
}
