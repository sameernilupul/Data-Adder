/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adddata;

import java.io.BufferedWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Chameera Wijebandara
 */
public class AddData {

    Connection connection = null;

    public static void main(String[] args) throws IOException {
        AddData d = new AddData();
        try {
            d.initDB();
        } catch (Exception e) {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[0] + "_log.out", true)));
            out.append(e.getMessage());
            out.close();
        }
        //System.out.println("-----" + args[0]);
        d.add(args[0]);
        System.out.println("end");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[0] + "_log.out", true)));
        out.append("end");
        out.close();

    }

    public void initDB() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("MySQL JDBC Driver Registered!");



        connection = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/sinmin?useUnicode=true&characterEncoding=utf-8", "root", "s!nm!n");

    }

    public void add(String fileName) throws IOException {
        //PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("out1.out")));


        File fXmlFile = new File(fileName);
        DocumentBuilder dBuilder;
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
        } catch (Exception e) {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + "_log.out", true)));
            out.append(fileName + "\n");
            out.append(e.getMessage() + "\n");
            out.close();
            return;
        }
        doc.getDocumentElement().normalize();

        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        NodeList nList = doc.getElementsByTagName("post");

        System.out.println("----------------------------");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            //out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                String link = eElement.getElementsByTagName("link").item(0).getTextContent();
                String topic = eElement.getElementsByTagName("topic").item(0).getTextContent();
                String data = eElement.getElementsByTagName("date").item(0).getTextContent();
                String author = eElement.getElementsByTagName("author").item(0).getTextContent();
                String content = eElement.getElementsByTagName("content").item(0).getTextContent();

                //System.out.println("Staff id : " + link);
                //out.println("Link : " + link);
                //System.out.println("First Name : " + topic);
                //out.println("Topic : " + topic);
                // System.out.println("Last Name : " + data);
                //out.println("Data : " + data);
                //System.out.println("Nick Name : " + author);
                //out.println("Author : " + author);
                // System.out.println("Salary : " + content);
                //out.println("Content : " + content);

                // add to post
                String date[] = data.split("/");
                data = date[2] + "." + date[1] + "." + date[0];
                int post_Index;
                try {
                    post_Index = addPost(link, author, topic, data);
                } catch (SQLException ex) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + "_log.out", true)));
                    out.append(link + "\n");
                    out.append(ex.getMessage() + "\n");
                    out.close();
                    continue;
                }

                System.out.println(post_Index);
                ArrayList<String> l = getSentens(content);
                //out.println(l.size());
                for (int i = 0; i < l.size(); i++) {
                    // out.println(l.get(i));

                    // add to sentense table
                    int sentens_Index;
                    try {
                        sentens_Index = addSentens(l.get(i));

                    } catch (SQLException ex) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + "_log.out", true)));
                        out.append(link + "\n");
                        out.append(ex.getMessage() + "\n");
                        out.close();
                        continue;
                    }
                    try {
                        // update sentenspost
                        update_Sentens_Post(post_Index, sentens_Index, i);
                    } catch (SQLException ex) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + "_log.out", true)));
                        out.append(link + "\n");
                        out.append(ex.getMessage() + "\n");
                        out.close();

                    }

                    ArrayList<String> l2 = getWords(l.get(i));
                    //out.println(l2.size());
                    for (int j = 0; j < l2.size(); j++) {
                        // out.println(l2.get(j));

                        // add to word table
                        int wordIndex;
                        try {
                            wordIndex = addWord(l2.get(j));
                        } catch (SQLException ex) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + "_log.out", true)));
                            out.append(link + "\n");
                            out.append(ex.getMessage() + "\n");
                            out.close();
                            continue;
                        }
                        try {
                            // update link table wordsentens
                            updateSentens_Word(sentens_Index, wordIndex, j);
                        } catch (SQLException ex) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + "_log.out", true)));
                            out.append(link + "\n");
                            out.append(ex.getMessage() + "\n");
                            out.close();
                        }
                    }
                }
            }
        }

        //out.close();
    }

    public ArrayList<String> getSentens(String in) {

        ArrayList<String> ans = new ArrayList<String>();
        StringBuffer s = new StringBuffer();
        int count = 0;
        for (int i = 0; i < in.length(); i++) {

            //System.out.println(in.codePointAt(i));
            s.appendCodePoint(in.codePointAt(i));
            if (in.codePointAt(i) == 46) {
                //System.out.println("p");

                if (!ans.isEmpty() && (i - count) < 5) {
                    StringBuffer hold = new StringBuffer(ans.remove(ans.size() - 1));
                    hold = hold.append(s);

                    s = hold;
                    continue;

                }
                if (s.length() > 5) {
                    count = i;
                    ans.add(s.toString().trim());
                    s = new StringBuffer();
                }


            }

        }

        return ans;
    }

    public ArrayList<String> getWords(String in) {

        ArrayList<String> ans = new ArrayList<String>();
        StringBuffer s = new StringBuffer();

        for (int i = 0; i < in.length(); i++) {

            //System.out.println(in.codePointAt(i));

            if (in.codePointAt(i) >= 3456 && in.codePointAt(i) <= 3583 || in.codePointAt(i) == 8205 || in.codePointAt(i) == 160 || in.codePointAt(i) == 8204) {

                s.appendCodePoint(in.codePointAt(i));
            } else if (s.length() != 0) {
                ans.add(s.toString());
                s = new StringBuffer();

            }

        }

        return ans;
    }

    public int addWord(String s) throws SQLException {

        String sql = "select  word_index from word where content = ?";
        PreparedStatement prest = connection.prepareStatement(sql);
        prest.setString(1, s);
        ResultSet resultSet = prest.executeQuery();

        resultSet.next();

        if (resultSet.getRow() == 0) {

            sql = "INSERT INTO word(content) VALUES (?)";
            prest = connection.prepareStatement(sql);
            prest.setString(1, s);
            prest.executeUpdate();
        } else {

            sql = "update word set frequency = frequency+1 where content = ?";
            prest = connection.prepareStatement(sql);
            prest.setString(1, s);
            prest.executeUpdate();
        }


        sql = "select  word_index from word where content = ?";
        prest = connection.prepareStatement(sql);
        prest.setString(1, s);
        resultSet = prest.executeQuery();
        resultSet.next();

        return resultSet.getInt(1);


    }

    private void updateSentens_Word(int sentens_Index, int word_Index, int position) throws SQLException {

        String sql = "INSERT INTO sentense_word (sentense_index,word_index,position) VALUES  (?,?,?)";
        PreparedStatement prest = connection.prepareStatement(sql);
        prest.setInt(1, sentens_Index);
        prest.setInt(2, word_Index);
        prest.setInt(3, position);
        prest.executeUpdate();


    }

    private int addSentens(String s) throws SQLException {

        String sql = "select  sentense_index from sentense where content = ?";
        PreparedStatement prest = connection.prepareStatement(sql);
        prest.setString(1, s);
        ResultSet resultSet = prest.executeQuery();

        resultSet.next();

        if (resultSet.getRow() == 0) {

            sql = "INSERT INTO sentense(content) VALUES (?)";
            prest = connection.prepareStatement(sql);
            prest.setString(1, s);
            prest.executeUpdate();
        } else {

            sql = "update sentense set frequency = frequency+1 where content = ?";
            prest = connection.prepareStatement(sql);
            prest.setString(1, s);
            prest.executeUpdate();
        }


        sql = "select  sentense_index from sentense where content = ? ";
        prest = connection.prepareStatement(sql);
        prest.setString(1, s);
        resultSet = prest.executeQuery();
        resultSet.next();

        return resultSet.getInt(1);

    }

    private int addPost(String link, String author, String topic, String data) throws SQLException {



        String sql = "INSERT INTO post(url,author,topic,date) VALUES (?,?,?,?)";
        PreparedStatement prest = connection.prepareStatement(sql);
        prest.setString(1, link);
        prest.setString(2, author);
        prest.setString(3, topic);
        prest.setString(4, data);
        prest.executeUpdate();


        sql = "select count(post_index) from post";
        prest = connection.prepareStatement(sql);
        ResultSet resultSet = prest.executeQuery();
        resultSet.next();

        return resultSet.getInt(1);

    }

    private void update_Sentens_Post(int post_Index, int sentens_Index, int position) throws SQLException {


        String sql = "INSERT INTO post_sentense (post_index,sentense_index,position) VALUES  (?,?,?)";
        PreparedStatement prest = connection.prepareStatement(sql);
        prest.setInt(1, post_Index);
        prest.setInt(2, sentens_Index);
        prest.setInt(3, position);
        prest.executeUpdate();



    }
}
