import java.io.File;
import java.sql.*;
import org.apache.commons.io.FilenameUtils;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author Dino Kralj
 */

public class Main {

    public static final String CONNECTION_STRING_RAZVOJ = "";
    public static final String CONNECTION_STRING_XE = "";
    public static final String CONNECTION_USERNAME = "";
    public static final String CONNECTION_PASSWORD = "";

    public static void main(String[] args) {

        Connection con;
        UnaryOperator<String> upper =  (x) -> x.toUpperCase();
        ArrayList<String> listOfFilesDB = new ArrayList<String>();
        ArrayList<String> listOfFilenamesWithNoExtension = new ArrayList<String>();
        String selectStatement = "SELECT * FROM files";
        String createTableStatement = "CREATE TABLE UNIQUEFILES" + "(filename VARCHAR2(20))";
        String checkForTableStatement = "SELECT count(*) broj FROM user_tables WHERE table_name = 'UNIQUEFILES'";
        String insertFilesIntoTable = "INSERT INTO UNIQUEFILES VALUES (?)";
        String deleteFilesFromTable = "DELETE FROM UNIQUEFILES";
        int tableExistance = 0;

        File folder = new File("C:\\Programming\\Direktorij");
        File[] listOfFiles = folder.listFiles();

        try {

            //Connecting to DB and getting files from table in DB
            System.out.println("Trying to connect");
            con = DriverManager.getConnection(CONNECTION_STRING_XE, CONNECTION_USERNAME, CONNECTION_PASSWORD);

            System.out.println("Connection Established Successfull and the DATABASE NAME IS:"
                    + con.getMetaData().getDatabaseProductName());

            //Getting database file names
            System.out.println("Database file names: ");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(selectStatement);
            while (rs.next()) {
                //Adding files names into Arraylist
                listOfFilesDB.add(rs.getString("files"));
                System.out.println(rs.getString(1));
            }

            System.out.println("Files in array list are:");
            //Making all the letter uppercase for easier comparison
            //listOfFilesDB.replaceAll(upper);
            for(String s : listOfFilesDB) {
                System.out.println(s);
            }

            //Files from directory and removing their extension
            System.out.println("Directory file names: ");
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String filenameWithoutExt = FilenameUtils.removeExtension(listOfFiles[i].getName());
                    listOfFilenamesWithNoExtension.add(filenameWithoutExt);
                    System.out.println(listOfFiles[i].getName());
                }
            }

            //Files from directory without extension
            System.out.println("New array with files names without extension: ");
            //Making all the letter uppercase for easier comparison
            //listOfFilenamesWithNoExtension.replaceAll(upper);
            for(String s : listOfFilenamesWithNoExtension) {
                System.out.println(s);
            }

            //Checking for unique and duplicate values from two arraylists
            List<String> filesFromDir = new ArrayList<String>(listOfFilenamesWithNoExtension);
            List<String> fileFromDB = new ArrayList<String>(listOfFilesDB);

            ArrayList<String> duplicates = new ArrayList<String>(fileFromDB);
            duplicates.retainAll(filesFromDir);

            ArrayList<String> uniquesFromDB = new ArrayList<String>(fileFromDB);
            uniquesFromDB.removeAll(filesFromDir);

            ArrayList<String> uniquesFromDir = new ArrayList<String>(filesFromDir);
            uniquesFromDir.removeAll(fileFromDB);

            ArrayList<String> uniquesFromBoth = new ArrayList<String>();
            uniquesFromBoth.addAll(uniquesFromDB);
            uniquesFromBoth.addAll(uniquesFromDir);

            System.out.println("Duplicate files: ");
            System.out.println(duplicates);
            System.out.println("Unique files from DB: ");
            System.out.println(uniquesFromDB);
            System.out.println("Unique files from directory: ");
            System.out.println(uniquesFromDir);
            System.out.println("Unique files from both DB and directory: ");
            System.out.println(uniquesFromBoth);

            //Creating new table in DB and checking if it exists.
            ResultSet ifTableExists = st.executeQuery(checkForTableStatement);
            while(ifTableExists.next()) {
                tableExistance = ifTableExists.getInt("broj");
            }
            if(tableExistance == 0) {
                st.executeQuery(createTableStatement);
            }

            //Getting files from array into new table in DB
            PreparedStatement pstmt = con.prepareStatement(insertFilesIntoTable);

            for(String uniques : uniquesFromBoth) {
                pstmt.setString(1, uniques);
                pstmt.execute();
            }

            //Deleteing data from 'UNIQUEFILES' table
           // st.executeQuery(deleteFilesFromTable);

        } catch (Exception e) {
            System.out.println("Unable to make connection with DB, cannot get database data or cannot get unique array list.");
            e.printStackTrace();
        }
    }
}
