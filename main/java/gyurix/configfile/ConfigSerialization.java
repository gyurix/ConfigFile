package gyurix.configfile;

import gyurix.utils.ClassUtils;
import gyurix.utils.DualMap;
import sun.reflect.ReflectionFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;


public class ConfigSerialization {
    public static final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
    public static final DualMap<Class, String> aliases = new DualMap();
    public static final HashMap<Class, Serializer> serializers = new HashMap();
    public static final DualMap<Class, Class> interfaceBasedClasses = new DualMap();
    public static ArrayList<String> errors = new ArrayList<String>();

    public static String getAlias(Class c) {
        if (c.isArray())
            c = Array.class;
        String al = aliases.get(c);
        if (al == null) {
            Class i = interfaceBasedClasses.getKey(c);
            if (i != null) {
                al = aliases.get(i);
                return al == null ? i.getName() : al;
            }
            return c.getName();
        }
        return al;
    }

    public static String calculateClassName(Class type, Class objectClass) {
        if (!objectClass.getName().equals(type.getName())) {
            Class c = interfaceBasedClasses.get(type);
            c = c == null ? type : c;
            if (c != objectClass) {
                return "-" + getAlias(objectClass);
            }
        }
        return "";
    }

    public static Class getNotInterfaceClass(Class cl) {
        Class c = interfaceBasedClasses.get(cl);
        return c == null ? cl : c;
    }

    public static Class realClass(String alias) {
        Class alC = aliases.getKey(alias);
        try {
            Class c = alC == null ? Class.forName(alias) : alC;
            Class c2 = interfaceBasedClasses.get(c);
            return c2 == null ? c : c2;
        } catch (Throwable e) {
            errorLog(e);
        }
        return null;
    }

    public static Serializer getSerializer(Class cl) {
        Class c = cl;
        if (cl.isArray()) {
            return serializers.get(Array.class);
        }
        Serializer s = serializers.get(c);
        for (; ; ) {
            if (s != null)
                return s;
            c = c.getSuperclass();
            if ((c == null) || (c == Object.class))
                break;
            s = serializers.get(c);
        }
        for (Class i : ClassUtils.getAllInterfaces(cl)) {
            s = serializers.get(i);
            if (s != null) {
                return s;
            }
        }
        return serializers.get(Object.class);
    }

    public static Object newInstance(Class cl) {
        try {
            try{
                return cl.newInstance();
            }
            catch (Throwable err){
                return rf.newConstructorForSerialization(cl, Object.class.getDeclaredConstructor()).newInstance();
            }
        } catch (Throwable e) {
            errorLog(e);
        }
        return null;
    }

    public static void errorLog(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(" - ").append(e.getMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            String loc = s.toString();
            if (loc.contains("gyurix")) {
                sb.append('\n').append(loc);
            }
        }
        String err = sb.toString();
        if (!errors.contains(err)) {
            errors.add(err);
            System.out.println("ConfigFile Error Reporter - found a new type of error, w:\n\n" + err + "\n\nYou should report this bug to the plugins dev, gyuriX");
            e.printStackTrace();
        }
    }

    public interface StringSerializable {
        String toString();
    }

    public interface Serializer {
        Object fromData(ConfigData paramConfigData, Class paramClass, Type... paramVarArgs);

        ConfigData toData(Object paramObject, Type... paramVarArgs);
    }

    @Target({java.lang.annotation.ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ConfigOptions {
        String defaultValue() default "null";

        String comment() default "";

        boolean serialize() default true;
    }
}



/* Location:           D:\Szerverek\SpaceCraft\plugins\ConfLangLib.jar

 * Qualified Name:     ConfigSerialization

 * JD-Core Version:    0.7.0.1

 */