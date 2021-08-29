package org.suricsun.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author: SuricSun
 * @date: 2021/8/8
 */
public class Serializer {

    public static void Serialize(Object obj, String outputPath) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.close();
        fileOutputStream.close();
    }
}
