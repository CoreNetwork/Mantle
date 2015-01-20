package us.corenetwork.mantle.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Matej on 23.2.2014.
 */
public class ReflectionUtils {
    public static Object get(Object obj, String field)
    {
        try {
            Class cls = obj.getClass();

            Field fieldObj = cls.getDeclaredField(field);
            fieldObj.setAccessible(true);
            return fieldObj.get(obj);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Object get(Class cls, Object obj, String field)
    {
        try {
            Field fieldObj = cls.getDeclaredField(field);
            fieldObj.setAccessible(true);
            return fieldObj.get(obj);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getStatic(Class cls, String field)
    {
        try {
            Field fieldObj = cls.getDeclaredField(field);
            fieldObj.setAccessible(true);
            return fieldObj.get(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void set(Object obj, String field, Object value)
    {
        try {
            Class cls = obj.getClass();
            Field fieldObj = cls.getDeclaredField(field);
            fieldObj.setAccessible(true);
            fieldObj.set(obj, value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Method getMethod(Class cls, String methodName, Class... argumentTypes)
    {
        try {
            Method method = cls.getDeclaredMethod(methodName, argumentTypes);
            method.setAccessible(true);
            return method;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static Object executeMethod(Method method, Object object, Object... arguments)
    {
        try {
            return method.invoke(object, arguments);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
