package gyurix.configfile;

import gyurix.utils.Primitives;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigData {
    public String comment;
    public String stringData;
    public Object objectData;
    public ArrayList<ConfigData> listData;
    public LinkedHashMap<ConfigData, ConfigData> mapData;

    public ConfigData() {
    }

    public ConfigData(String stringData) {
        this.stringData = stringData;
    }

    public ConfigData(String stringData,String comment) {
        this.stringData = stringData;
        if (comment!=null&&!comment.isEmpty())
            this.comment=comment;
    }

    public static ConfigData serializeObject(Object obj, Type... parameters) {
        return serializeObject(obj, false, parameters);
    }

    public static ConfigData serializeObject(Object obj, boolean className, Type... parameters) {
        if (obj == null) {
            System.out.println("OBJ=Null");
            return null;
        }
        Class c = Primitives.wrap(obj.getClass());
        if (c.isArray()) {
            className = true;
            parameters = new Type[]{c.getComponentType()};
        }
        ConfigSerialization.Serializer s = ConfigSerialization.getSerializer(c);
        ConfigData cd = s.toData(obj, parameters);
        if ((cd.stringData != null) && (cd.stringData.startsWith("‼")))
            cd.stringData = ("\\" + cd.stringData);
        if (className) {
            String prefix = "‼" + ConfigSerialization.getAlias(obj.getClass());
            for (Type t : parameters) {
                prefix = prefix + "-" + ConfigSerialization.getAlias((Class) t);
            }
            prefix = prefix + "‼";
            cd.stringData = (prefix + cd.stringData);
        }
        return cd;
    }

    public static String escape(String in) {
        StringBuilder out = new StringBuilder();
        String escSpace = "\n:->";
        char prev = '\n';
        for (char c : in.toCharArray()) {
            switch (c) {
                case ' ':
                case '#':
                    out.append(escSpace.contains("" + prev) ? "\\" + c : Character.valueOf(c));
                    break;
                case '‼':
                    if (prev == '\\')
                        out.deleteCharAt(out.length() - 1);
                    out.append("‼");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\n':
                    out.append(escSpace.contains("" + prev)||escSpace.contains(""+prev) ? "\\n": '\n');
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
            prev = c;
        }
        if ((prev == '\n') && (out.length() != 0)) {
            out.setCharAt(out.length() - 1, '\\');
            out.append('n');
        }
        return out.toString();
    }

    public static String unescape(String in) {
        StringBuilder out = new StringBuilder(in.length());
        String uchars = "0123456789abcdef0123456789ABCDEF";
        boolean escape = false;
        int ucode = -1;
        for (char c : in.toCharArray()) {
            if (ucode != -1) {
                int id = uchars.indexOf(c) % 16;
                if (id == -1) {
                    out.append((char) ucode);
                    ucode = -1;
                } else {
                    ucode = ucode * 16 + id;
                    continue;
                }
            }
            if (escape) {
                switch (c) {
                    case 'u':
                        ucode = 0;
                        break;
                    case 'n':
                        out.append("\n");
                        break;
                    case 'r':
                        out.append("\r");
                        break;
                    case 't':
                        out.append("\t");
                        break;
                    case 'b':
                        out.append("\b");
                        break;
                    case ' ':
                    case '-':
                    case '>':
                    case '\\':
                        out.append(c);
                }
                escape = false;
            } else if (!(escape = c == '\\')) {
                out.append(c);
            }
        }
        if (ucode != -1)
            out.append((char) ucode);
        return out.toString().replaceAll("\n +#", "\n#");
    }

    public Object deserialize(Class c, Type... types) {
        if (this.objectData != null)
            return this.objectData;
        String str = this.stringData == null ? "" : this.stringData;

        if (str.startsWith("‼")) {
            str = str.substring(1);
            int id = str.indexOf("‼");
            if (id != -1) {
                str = str.substring(0, id);
                String[] classNames = str.split("-");
                c = ConfigSerialization.realClass(classNames[0]);
                types = new Type[classNames.length - 1];
                for (int i = 1; i < classNames.length; i++) {
                    types[(i - 1)] = ConfigSerialization.realClass(classNames[i]);
                }
                this.stringData = this.stringData.substring(id + 2);
                ConfigSerialization.Serializer ser = ConfigSerialization.getSerializer(c);
                this.objectData = ser.fromData(this, c, types);
            }
        } else {
            ConfigSerialization.Serializer ser = ConfigSerialization.getSerializer(c);
            this.objectData = ser.fromData(this, c, types);
        }
        this.stringData = null;
        this.mapData = null;
        this.listData = null;
        return this.objectData;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        if (this.objectData != null) {
            return serializeObject(this.objectData, new Type[0]).toString();
        }
        if (this.stringData != null&&!this.stringData.isEmpty()) {
            out.append(escape(this.stringData));
        }
        if (this.mapData != null&&!this.mapData.isEmpty()) {
            for (Map.Entry<ConfigData, ConfigData> d : this.mapData.entrySet()) {
                String value = d.getValue().toString();
                if (value==null)
                    continue;
                value=value.replace("\n", "\n  ");
                String key = (d.getKey()).toString().replace("\n", "\n  ");
                if (key.contains("\n")) {
                    out.append("\n  > ").append(key).append("\n  : ").append(value);
                } else {
                    out.append("\n  ").append(key).append(": ").append(value);
                }
                if ((d.getKey()).comment != null)
                    out.append("\n#").append((d.getKey()).comment.replace("\n", "\n#"));
                if ((d.getValue()).comment != null)
                    out.append("\n#").append((d.getValue()).comment.replace("\n", "\n#"));
            }
        }
        if (this.listData != null&&!this.listData.isEmpty()) {
            for (ConfigData d : this.listData) {
                String data = d.toString();
                if (data==null)
                    data="";
                else if (data.startsWith("\n  "))
                    data = data.substring(3);
                out.append("\n- ").append(data);
                if (d.comment != null)
                    out.append("\n#").append(d.comment.replace("\n", "\n#"));
            }
        }
        if (out.length()==0)
            return null;
        return out.toString();
    }

    public boolean equals(Object obj) {
        return obj != null;
    }


    public int hashCode() {
        return this.stringData == null ? this.objectData == null ? this.listData == null ? this.mapData == null ? 0 :
                this.mapData.hashCode() : this.listData.hashCode() : this.objectData.hashCode() : this.stringData.hashCode();
    }
}



/* Location:           D:\Szerverek\SpaceCraft\plugins\ConfLangLib.jar

 * Qualified Name:     ConfigData

 * JD-Core Version:    0.7.0.1

 */