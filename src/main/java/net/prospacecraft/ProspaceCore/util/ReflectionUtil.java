package net.prospacecraft.ProspaceCore.util;

import java.lang.reflect.*;

/**
 *
 */
public class ReflectionUtil
{
    public static <T> T getInstance(Class<?> clazz)
    {
        Method get = getMethod(clazz, "getInstance");
        T ret = invokeMethod(get, null);
        if (ret == null) throw new NullPointerException("The instance was null: " + clazz);
        if ( ! clazz.isAssignableFrom(ret.getClass())) throw new IllegalStateException("The instance was not of same or sub-class: " + clazz);
        return ret;
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterType)
    {
        try
        {
            Method ret = clazz.getDeclaredMethod(name, parameterType);
            makeAccessible(ret);
            return ret;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Method method, Object target, Object... arguments)
    {
        try
        {
            return (T) method.invoke(target, arguments);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeMethod(Method method, Object target, Object argument)
    {
        return invokeMethod(method, target, new Object[]{argument});
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Method method, Object target)
    {
        return (T) invokeMethod(method, target, new Object[]{});
    }

    public static void makeAccessible(Object object)
    {
        try
        {
            if(object instanceof Method)
            {
                Method method = (Method)object;
                method.setAccessible(true);
            }
            else if(object instanceof Constructor<?>)
            {
                Constructor<?> c = (Constructor<?>)object;
                c.setAccessible(true);

            }
            else if(object instanceof Field)
            {
                Field MODIFIER_FIELD = Field.class.getDeclaredField("modifiers");
                MODIFIER_FIELD.setAccessible(true);

                MODIFIER_FIELD.setInt(object, ((Field) object).getModifiers() & ~0x00000010);
            }
            else
            {
                makeAccessible(object);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void makeAccessible(AccessibleObject object)
    {
        object.setAccessible(true);
    }
}