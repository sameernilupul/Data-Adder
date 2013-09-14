/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adddata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sameernilupul
 */
public class Runner {

    public static void main(String[] args) throws IOException {
        String dir = "/home/sinmin/data/sameera/Small";
        File[] files = finder(dir);
        Executor[] execs = new Executor[5];

        int nextrun = 0;

        while (nextrun < 10) {
            for (int i = 0; i < execs.length; i++) {


                if (execs[i] == null || !execs[i].isAlive() ) {
                    System.out.println("Starting ="+nextrun);
                    execs[i] = new Executor(files[nextrun].getName(), dir);
                    execs[i].start();
                    nextrun++;
                }
                //System.out.println(files[i].getName());
                //add(dir+"/"+files[i].getName());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Runner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static File[] finder(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".xml");
            }
        });

    }
}

class Executor extends Thread {

    String name;
    String dir;

    public Executor(String file_name, String dir) {
        this.name = file_name;
        this.dir = dir;
    }

    public void run() {
        try {
            System.out.println(this.name);
            Runtime r = Runtime.getRuntime();
            Process proc = r.exec("java -jar ./dist/AddData.jar " + this.dir + "/" + this.name);
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            while (!input.readLine().endsWith("end"));
        } catch (IOException ex) {
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
