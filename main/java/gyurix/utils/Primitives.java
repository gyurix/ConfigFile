//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package gyurix.utils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Primitives {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE;
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE;

    public Primitives() {
    }

    private static void add(Map<Class<?>, Class<?>> forward, Map<Class<?>, Class<?>> backward, Class<?> key, Class<?> value) {
        forward.put(key, value);
        backward.put(value, key);
    }

    public static boolean isPrimitive(Type type) {
        return PRIMITIVE_TO_WRAPPER_TYPE.containsKey(type);
    }


    public static <T> Class<T> wrap(Class<T> type) {
        Class wrapped = (Class)PRIMITIVE_TO_WRAPPER_TYPE.get(type);
        return wrapped == null?type:wrapped;
    }

    public static <T> Class<T> unwrap(Class<T> type) {
        Class unwrapped = (Class)WRAPPER_TO_PRIMITIVE_TYPE.get(type);
        return unwrapped == null?type:unwrapped;
    }

    static {
        HashMap primToWrap = new HashMap(16);
        HashMap wrapToPrim = new HashMap(16);
        add(primToWrap, wrapToPrim, Boolean.TYPE, Boolean.class);
        add(primToWrap, wrapToPrim, Byte.TYPE, Byte.class);
        add(primToWrap, wrapToPrim, Character.TYPE, Character.class);
        add(primToWrap, wrapToPrim, Double.TYPE, Double.class);
        add(primToWrap, wrapToPrim, Float.TYPE, Float.class);
        add(primToWrap, wrapToPrim, Integer.TYPE, Integer.class);
        add(primToWrap, wrapToPrim, Long.TYPE, Long.class);
        add(primToWrap, wrapToPrim, Short.TYPE, Short.class);
        add(primToWrap, wrapToPrim, Void.TYPE, Void.class);
        PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
        WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
    }
}
