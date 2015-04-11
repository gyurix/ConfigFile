package gyurix.configfile;

import gyurix.utils.ArrayUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ConfigFile {
    public File file;
    public ConfigData data = new ConfigData();
    public Charset charset = Charset.forName("UTF-8");
    public String addressSplit = "\\.";

    public ConfigFile() {
    }

    public ConfigFile(InputStream stream) {
        load(stream);
    }

    public ConfigFile(File file) {
        load(file);
    }

    public ConfigFile(String in) {
        load(in);
    }

    public ConfigFile(ConfigData d) {
        this.data = d;
    }

    public boolean reload() {
        this.data = new ConfigData();
        return load(this.file);
    }

    public boolean save() {
        try {
            this.file.createNewFile();
            Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            w.write(toString());
            w.close();
            return true;
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return false;
    }

    public boolean load(File f) {
        try {
            this.file = f;
            byte[] b = Files.readAllBytes(f.toPath());
            load(new String(b, this.charset));
            return true;
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return false;
    }

    public boolean load(InputStream stream) {
        try {
            byte[] b = new byte[stream.available()];
            stream.read(b);
            return load(new String(b, this.charset));
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return false;
    }

    public boolean load(String in) {
        ArrayList<ConfigReader> readers = new ArrayList<ConfigReader>();

        readers.add(new ConfigReader(-1, this.data));

        for (String s : in.split("\r?\n")) {
            int blockLvl = 0;
            while ((s.length() > blockLvl) && (s.charAt(blockLvl) == ' '))
                blockLvl++;
            s = s.substring(blockLvl);
            int id = readers.size() - 1;
            if (!s.isEmpty()) {
                if (s.startsWith("#")) {
                    readers.get(id).addComment(s.substring(1));
                } else {
                    while (readers.get(id).blockLvl > blockLvl) {
                        readers.remove(id);
                        id--;
                    }
                    readers.get(id).handleInput(readers, s, blockLvl);
                }
            }
        }
        return true;
    }
    public ConfigData getData(ConfigData key) {
        if (data.mapData==null)
            data.mapData=new LinkedHashMap<ConfigData, ConfigData>();
        ConfigData out=data.mapData.get(key);
        if (out==null)
            data.mapData.put(key,out=new ConfigData(""));
        return out;
    }
    public ConfigData getData(String address) {
        String[] parts = address.split(this.addressSplit);
        ConfigData d = data;
        for (String p : parts) {
            if (p.matches("#\\d+")){
                int num=Integer.valueOf(p.substring(1));
                if (d.listData==null)
                    d.listData=new ArrayList<ConfigData>();
                while (d.listData.size()<=num){
                    d.listData.add(new ConfigData(""));
                }
                d=d.listData.get(num);
            }
            else{
                ConfigData key=new ConfigData(p);
                if (d.mapData==null)
                    d.mapData=new LinkedHashMap<ConfigData, ConfigData>();
                else if (d.mapData.containsKey(key)){
                    d=d.mapData.get(key);
                }
                else{
                    d.mapData.put(key,d=new ConfigData(""));
                }
            }
        }
        return d;
    }
    public boolean removeData(String address) {
        String[] allParts = address.split(this.addressSplit);
        int len=allParts.length-1;
        String[] parts= (String[]) ArrayUtils.subarray(allParts,0,len);
        ConfigData d = this.data;
        for (String p : parts) {
            if (p.matches("#\\d+")){
                if (d.listData==null)
                    return false;
                int num=Integer.valueOf(p.substring(1));
                if (d.listData.size()>=num){
                    return false;
                }
                d=d.listData.get(num);
            }
            else{
                ConfigData key=new ConfigData(p);
                if (d.mapData==null||!d.mapData.containsKey(key)){
                    return false;
                }
                else{
                    d=d.mapData.get(key);
                }
            }
        }
        if (allParts[len].matches("#\\d+")){
            return d.listData.remove(Integer.valueOf(allParts[len].substring(1)));
        }
        return d.mapData.remove(new ConfigData(allParts[len]))!=null;
    }

    public Object get(String adress, Class cl) {
        return getData(adress).deserialize(cl, new Type[0]);
    }

    public ConfigFile subConfig(String address) {
        ConfigData d = getData(address);
        return new ConfigFile(d);
    }

    public ConfigFile subConfig(ConfigData key) {
        return new ConfigFile(getData(key));
    }

    public ConfigFile subConfig(int id) {
        try {
            return new ConfigFile(this.data.listData.get(id));
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return null;
    }


    public String getString(String address) {
        return getData(address).stringData;
    }


    public void setString(String address, String str) {
        getData(address).stringData = str;
    }

    public void setObject(String address, Object obj) {
        getData(address).objectData = obj;
    }

    public String toString() {
        String s = (this.data.comment != null ? "#" + this.data.comment.replace("\n", "\n#") + "\n" : "") + this.data.toString().replace("\n  ", "\n").replaceAll("\n +#", "\n#");
        return s.startsWith("\n") ? s.substring(1) : s;
    }
}



/* Location:           D:\Szerverek\SpaceCraft\plugins\ConfLangLib.jar

 * Qualified Name:     ConfigFile

 * JD-Core Version:    0.7.0.1

 */