package pt.ist.cnv.cloudprime.mss.util;

import pt.ist.cnv.cloudprime.mss.metrics.RequestMetrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ASUS on 27/04/2016.
 */
public class MetricsFileWriter {

    public static synchronized void writeRequestToFile(RequestMetrics m) throws IOException {

        File folder = new File(Config.STORAGE_DIR);

        for (File file :  folder.listFiles()) {
            if (file.isFile() && file.getName().equals(m.getRequestNumber() + ".txt")){
                return;
            }
        }

        FileWriter fileWriter = new FileWriter(new File(folder.getAbsolutePath() + File.separator + m.getRequestNumber() + ".txt"));
        fileWriter.write(m.toJSON().toString());
        fileWriter.flush();
        fileWriter.close();
    }



}
