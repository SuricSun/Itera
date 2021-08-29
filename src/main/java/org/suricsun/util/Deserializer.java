package org.suricsun.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author: SuricSun
 * @date: 2021/8/8
 */
public class Deserializer {

    public static Object deserialize(String objPath) throws IOException, ClassNotFoundException {

        FileInputStream fileInputStream = new FileInputStream(objPath);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Object obj =  objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
        return obj;
    }
}
