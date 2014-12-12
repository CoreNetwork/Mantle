package us.corenetwork.mantle.hardmode;

import java.lang.reflect.Field;

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

    public static Object set(Object obj, String field)
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
}
