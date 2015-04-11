package gyurix.configfile;

import gyurix.utils.ArrayUtils;
import gyurix.utils.Primitives;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

public class DefaultSerializers {
    public static void init() {
        ConfigSerialization.serializers.put(String.class, new StringSerializer());
        NumberSerializer numberSerializer = new NumberSerializer();
        ConfigSerialization.serializers.put(Short.class, numberSerializer);
        ConfigSerialization.serializers.put(Integer.class, numberSerializer);
        ConfigSerialization.serializers.put(Long.class, numberSerializer);
        ConfigSerialization.serializers.put(Float.class, numberSerializer);
        ConfigSerialization.serializers.put(Double.class, numberSerializer);
        ConfigSerialization.serializers.put(Boolean.class, new BooleanSerializer());
        ConfigSerialization.serializers.put(Character.class, new CharacterSerializer());
        ConfigSerialization.serializers.put(java.lang.reflect.Array.class, new ArraySerializer());
        ConfigSerialization.serializers.put(Collection.class, new CollectionSerializer());
        ConfigSerialization.serializers.put(Map.class, new MapSerializer());
        ConfigSerialization.serializers.put(Object.class, new ObjectSerializer());
        ConfigSerialization.serializers.put(Pattern.class, new PatternSerializer());
        ConfigSerialization.aliases.put(String.class, "str");
        ConfigSerialization.aliases.put(UUID.class, "uuid");
        ConfigSerialization.aliases.put(Short.class, "s");
        ConfigSerialization.aliases.put(Integer.class, "i");
        ConfigSerialization.aliases.put(Long.class, "l");
        ConfigSerialization.aliases.put(Float.class, "f");
        ConfigSerialization.aliases.put(Double.class, "d");
        ConfigSerialization.aliases.put(Boolean.class, "configuration");
        ConfigSerialization.aliases.put(Character.class, "c");
        ConfigSerialization.aliases.put(java.lang.reflect.Array.class, "[]");
        ConfigSerialization.aliases.put(Collection.class, "{}");
        ConfigSerialization.aliases.put(java.util.List.class, "{L}");
        ConfigSerialization.aliases.put(java.util.Set.class, "{S}");
        ConfigSerialization.aliases.put(java.util.LinkedHashSet.class, "{LS}");
        ConfigSerialization.aliases.put(java.util.TreeSet.class, "{TS}");
        ConfigSerialization.aliases.put(Map.class, "<>");
        ConfigSerialization.aliases.put(LinkedHashMap.class, "<L>");
        ConfigSerialization.aliases.put(TreeMap.class, "<T>");
        ConfigSerialization.aliases.put(Object.class, "?");
        ConfigSerialization.interfaceBasedClasses.put(java.util.List.class, java.util.ArrayList.class);
        ConfigSerialization.interfaceBasedClasses.put(java.util.Set.class, java.util.HashSet.class);
        ConfigSerialization.interfaceBasedClasses.put(Map.class, HashMap.class);
        new Primitives();
    }

    public static class StringSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return input.stringData;
        }

        public ConfigData toData(Object input, Type... parameters) {
            return new ConfigData((String) input);
        }
    }

    public static class UUIDSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return java.util.UUID.fromString(input.stringData);
        }

        public ConfigData toData(Object input, Type... parameters) {
            return new ConfigData(input.toString());
        }
    }

    public static class CharacterSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return Character.valueOf(input.stringData.charAt(0));
        }

        public ConfigData toData(Object in, Type... parameters) {
            return new ConfigData("" + (Character) in);
        }
    }

    public static class BooleanSerializer implements ConfigSerialization.Serializer {
        public static java.util.regex.Pattern trueRegex = java.util.regex.Pattern.compile("\\+||true||yes");

        public Object fromData(ConfigData input, Class cl, Type... parameters) {
            return Boolean.valueOf(trueRegex.matcher(input.stringData).matches());
        }

        public ConfigData toData(Object in, Type... parameters) {
            return new ConfigData(((Boolean) in).booleanValue() ? "+" : "-");
        }
    }

    public static class NumberSerializer implements ConfigSerialization.Serializer {
        public static HashMap<Class, java.lang.reflect.Method> methods = new HashMap();

        static {
            try {
                methods.put(Short.class, Short.class.getMethod("decode", new Class[]{String.class}));
                methods.put(Integer.class, Integer.class.getMethod("decode", new Class[]{String.class}));
                methods.put(Long.class, Long.class.getMethod("decode", new Class[]{String.class}));
                methods.put(Float.class, Float.class.getMethod("valueOf", new Class[]{String.class}));
                methods.put(Double.class, Double.class.getMethod("valueOf", new Class[]{String.class}));
            } catch (Throwable e) {
                ConfigSerialization.errorLog(e);
            }
        }

        public Object fromData(ConfigData input, Class fixClass, Type... parameters) {
            java.lang.reflect.Method m = (java.lang.reflect.Method) methods.get(fixClass);
            try {
                return m.invoke(null, new Object[]{input.stringData});
            } catch (Throwable e) {
                ConfigSerialization.errorLog(e);
                try {
                    return m.invoke(null, new Object[]{"0"});
                } catch (Throwable e2) {
                    System.out.println("IMPOSSIBLE");
                    ConfigSerialization.errorLog(e);
                }
            }
            return null;
        }

        public ConfigData toData(Object input, Type... parameters) {
            return new ConfigData(input.toString());
        }
    }

    public static class ArraySerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class fixClass, Type... parameterTypes) {
            Class cl = Object.class;
            Type[] types = new Type[0];
            if (parameterTypes.length >= 1) {
                if ((parameterTypes[0] instanceof ParameterizedType)) {
                    ParameterizedType pt = (ParameterizedType) parameterTypes[0];
                    cl = (Class) pt.getRawType();
                    types = pt.getActualTypeArguments();
                } else {
                    cl = (Class) parameterTypes[0];
                }
            }
            if (input.listData != null) {
                Object ar = java.lang.reflect.Array.newInstance(cl, input.listData.size());
                int i = 0;
                for (ConfigData d : input.listData) {
                    java.lang.reflect.Array.set(ar, i++, d.deserialize(cl, types));
                }
                return ar;
            }
            return java.lang.reflect.Array.newInstance(cl, 0);
        }


        public ConfigData toData(Object input, Type... parameters) {
            Class cl = parameters.length >= 1 ? (Class) parameters[0] : Object.class;
            ConfigData d = new ConfigData();
            d.listData = new java.util.ArrayList();
            for (Object o : java.util.Arrays.asList((Object[]) input)) {
                if (o != null) {
                    d.listData.add(ConfigData.serializeObject(o, o.getClass() != cl, new Type[0]));
                }
            }
            return d;
        }
    }

    public static class CollectionSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class fixClass, Type... parameterTypes) {
            try {
                Collection col = (Collection) fixClass.newInstance();
                Class cl;
                Type[] types;
                ParameterizedType pt;
                if (input.listData != null) {
                    cl = Object.class;
                    types = new Type[0];
                    if (parameterTypes.length >= 1) {
                        if ((parameterTypes[0] instanceof ParameterizedType)) {
                            pt = (ParameterizedType) parameterTypes[0];
                            cl = (Class) pt.getRawType();
                            types = pt.getActualTypeArguments();
                        } else {
                            cl = (Class) parameterTypes[0];
                        }
                    }
                    for (ConfigData d : input.listData) {
                        col.add(d.deserialize(cl, types));
                    }
                }
                return col;
            } catch (Throwable e) {
                ConfigSerialization.errorLog(e);
            }
            return null;
        }


        public ConfigData toData(Object input, Type... parameters) {
            Type[] types = new Type[0];
            Class cl = Object.class;
            if (parameters.length >= 1) {
                if ((parameters[0] instanceof ParameterizedType)) {
                    ParameterizedType key = (ParameterizedType) parameters[0];
                    types = key.getActualTypeArguments();
                    cl = (Class) key.getRawType();
                } else {
                    cl = (Class) parameters[0];
                }
            }
            if (((Collection) input).isEmpty())
                return new ConfigData("");
            ConfigData d = new ConfigData();
            d.listData = new java.util.ArrayList();
            for (Object o : (Collection) input) {
                d.listData.add(ConfigData.serializeObject(o, o.getClass() != cl, types));
            }
            return d;
        }
    }

    public static class MapSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class fixClass, Type... parameterTypes) {
            try {
                Map map = (Map) fixClass.newInstance();
                Class keyClass;
                Type[] keyTypes;
                Class valueClass;
                Type[] valueTypes;
                ParameterizedType pt;
                if (input.mapData != null) {
                    keyClass = Object.class;
                    keyTypes = new Type[0];
                    if (parameterTypes.length >= 1) {
                        if ((parameterTypes[0] instanceof ParameterizedType)) {
                            pt = (ParameterizedType) parameterTypes[0];
                            keyClass = (Class) pt.getRawType();
                            keyTypes = pt.getActualTypeArguments();
                        } else {
                            keyClass = (Class) parameterTypes[0];
                        }
                    }
                    valueClass = Object.class;
                    valueTypes = new Type[0];
                    if (parameterTypes.length >= 2) {
                        if ((parameterTypes[1] instanceof ParameterizedType)) {
                            pt = (ParameterizedType) parameterTypes[1];
                            valueClass = (Class) pt.getRawType();
                            valueTypes = pt.getActualTypeArguments();
                        } else {
                            valueClass = (Class) parameterTypes[1];
                        }
                    }
                    for (java.util.Map.Entry<ConfigData, ConfigData> e : input.mapData.entrySet()) {
                        map.put(((ConfigData) e.getKey()).deserialize(keyClass, keyTypes), ((ConfigData) e.getValue()).deserialize(valueClass, valueTypes));
                    }
                }
                return map;
            } catch (Throwable e) {
                ConfigSerialization.errorLog(e);
            }
            return null;
        }


        public ConfigData toData(Object input, Type... parameters) {
            if (((Map) input).isEmpty())
                return new ConfigData();
            Class keyClass = Object.class;
            Class valueClass = Object.class;
            Type[] keyTypes = new Type[0];
            Type[] valueTypes = new Type[0];
            if (parameters.length >= 1) {
                if ((parameters[0] instanceof ParameterizedType)) {
                    ParameterizedType key = (ParameterizedType) parameters[0];
                    keyTypes = key.getActualTypeArguments();
                    keyClass = (Class) key.getRawType();
                } else {
                    keyClass = (Class) parameters[0];
                }
                if (parameters.length >= 2) {
                    if ((parameters[1] instanceof ParameterizedType)) {
                        ParameterizedType value = (ParameterizedType) parameters[1];
                        valueTypes = value.getActualTypeArguments();
                        valueClass = (Class) value.getRawType();
                    } else {
                        valueClass = (Class) parameters[1];
                    }
                }
            }
            ConfigData d = new ConfigData();
            d.mapData = new LinkedHashMap();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) input).entrySet()) {
                Object key = e.getKey();
                Object value = e.getValue();
                if ((key != null) && (value != null))
                    d.mapData.put(ConfigData.serializeObject(key, key.getClass() != keyClass, keyTypes),
                            ConfigData.serializeObject(value, value.getClass() != valueClass, valueTypes));
            }
            return d;
        }
    }

    public static class ObjectSerializer implements ConfigSerialization.Serializer {
        public Object fromData(ConfigData input, Class fixClass, Type... parameters) {
            try {
                if (ArrayUtils.contains(fixClass.getInterfaces(), ConfigSerialization.StringSerializable.class)) {
                    return fixClass.getConstructor(new Class[]{String.class}).newInstance(new Object[]{input.stringData});
                }
            } catch (Throwable e) {
                ConfigSerialization.errorLog(e);
            }
            Object obj = ConfigSerialization.newInstance(fixClass);
            if (input.mapData==null)
                return obj;
            for (Field f : fixClass.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    String fn = f.getName();
                    ConfigData d = input.mapData.get(new ConfigData(fn));
                    if (d != null) {
                        Type[] types = (f.getGenericType() instanceof ParameterizedType) ? ((ParameterizedType) f.getGenericType()).getActualTypeArguments() : new Type[0];
                        Object out = d.deserialize(ConfigSerialization.getNotInterfaceClass(Primitives.wrap(f.getType())), types);
                        if (out != null)
                            f.set(obj, out);
                    }
                } catch (Throwable e) {
                    ConfigSerialization.errorLog(e);
                }
            }
            return obj;
        }
        public ConfigData toData(Object obj, Type... parameters) {
            Class c = Primitives.wrap(obj.getClass());
            if (ArrayUtils.contains(c.getInterfaces(), ConfigSerialization.StringSerializable.class)) {
                return new ConfigData(obj.toString());
            }
            ConfigData out = new ConfigData();
            out.mapData = new LinkedHashMap();
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    String dfValue="null";
                    String comment="";
                    ConfigSerialization.ConfigOptions options = (ConfigSerialization.ConfigOptions) f.getAnnotation(ConfigSerialization.ConfigOptions.class);
                    if (options!=null){
                        if (!options.serialize())
                            continue;
                        dfValue=""+options.defaultValue();
                        comment=""+options.comment();
                    }
                    Object o = f.get(obj);
                    if (o != null&&!o.toString().matches(dfValue)) {
                        String fn = f.getName();
                        String cn = ConfigSerialization.calculateClassName(f.getType(), o.getClass());
                        Type t = f.getGenericType();
                        out.mapData.put(new ConfigData(fn,comment), ConfigData.serializeObject(o, cn.isEmpty(),
                                t instanceof ParameterizedType ?
                                            ((ParameterizedType) t).getActualTypeArguments() :
                                            (((Class) t).isArray() ?
                                                    new Type[]{((Class) t).getComponentType()} :
                                                    new Type[0])));
                    }
                } catch (Throwable e) {
                    ConfigSerialization.errorLog(e);
                }
            }
            return out;
        }
    }

    public static class PatternSerializer implements ConfigSerialization.Serializer {
        @Override
        public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
            return Pattern.compile(data.stringData);
        }

        @Override
        public ConfigData toData(Object pt, Type... paramVarArgs) {
            return new ConfigData(((Pattern) pt).pattern());
        }
    }
}



/* Location:           D:\Szerverek\SpaceCraft\plugins\ConfLangLib.jar

 * Qualified Name:     DefaultSerializers

 * JD-Core Version:    0.7.0.1

 */