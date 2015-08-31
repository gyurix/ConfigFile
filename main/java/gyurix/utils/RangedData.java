package gyurix.utils;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class RangedData<T extends Comparable>{
    public Comparable min,max;
    public RangedData(){}
    public RangedData(T min, T max){
        this.min=min;
        this.max=max;
    }
    public boolean contains(Comparable value){
        return (min==null||min.compareTo(value)<=0)&&(max==null||max.compareTo(value)>=0);
    }
    public boolean any(){
        return min==null&&max==null;
    }
    public static Object get(Map<?,?> map,Comparable value){
        Object notFound = null;
        for (Map.Entry<?,?> e:map.entrySet()){
            RangedData rd= (RangedData) e.getKey();
            if (rd.any())
                notFound=e.getValue();
            else if (rd.contains(value)){
                return e.getValue();
            }
        }
        return notFound;
    }
}
