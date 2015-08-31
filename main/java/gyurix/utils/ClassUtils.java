package gyurix.utils;

import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
    public static List<Class> getAllInterfaces(Class cls) {
        if(cls == null) {
            return null;
        } else {
            ArrayList interfacesFound = new ArrayList();
            getAllInterfaces(cls, interfacesFound);
            return interfacesFound;
        }
    }

    private static void getAllInterfaces(Class cls, List interfacesFound) {
        while(cls != null) {
            Class[] interfaces = cls.getInterfaces();

            for(int i = 0; i < interfaces.length; ++i) {
                if(!interfacesFound.contains(interfaces[i])) {
                    interfacesFound.add(interfaces[i]);
                    getAllInterfaces(interfaces[i], interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }

    }
}
