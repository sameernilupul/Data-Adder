/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adddata;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class FileBreaker {

    public static void main(String[] args) {

        BufferedReader br = null;

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader("/Users/sameernilupul/Desktop/SinMinData/Divaina_2013.xml"));
            BufferedWriter writer = null;
            int counter =0;
            int file_num =38;
            int postsize = 500;
            boolean start = true;
            while ((sCurrentLine = br.readLine()) != null) {
                if(sCurrentLine.trim().equals("</post>")){
                    //System.out.println(counter);
                    counter++;
                }
                try {
                    writer = new BufferedWriter(new FileWriter("/Users/sameernilupul/Desktop/SinMinData/Small/S"+file_num+".xml", true));
                    if(start){
                        writer.write("<root>\n");
                        start = false;
                    }
                    writer.write(sCurrentLine+"\n");
                    if(counter==postsize){
                        writer.write("</root>\n");
                    }
                    writer.flush();
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if(counter==postsize){
                    counter=0;
                    file_num++;
                    start=true;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}