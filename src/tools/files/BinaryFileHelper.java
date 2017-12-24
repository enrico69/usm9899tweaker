/**
 * Handle edition of binary files.
 * Is a singleton
 * @author Eric COURTIAL
 */
package tools.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BinaryFileHelper {
    
    private RandomAccessFile binaryFile = null;
    private String filePath = "";
    private long fileLenght = 0;
    private static BinaryFileHelper instance = null;
    
    /**
     * Constructor
     * Exists only to defeat instantiation.
     */
    protected void BinaryFileHelper() {
    }
    
    /**
     * Get instance of this class
     * @return BinaryFileHelper
     */
    public static BinaryFileHelper getInstance() {
        if(instance == null) {
            instance = new BinaryFileHelper();
        }
        
        return instance;
    }
    
    /**
     * Set the filepath
     * @param path
     */
    public void setFilePath(String path) {
        this.filePath = path;
    }
    
    /**
     * Open the file
     * @throws FileNotFoundException
     */
    public void openFile() throws FileNotFoundException {
        File file = new File(this.filePath);
        this.fileLenght = file.length();
        this.binaryFile = new RandomAccessFile(file, "rw");
    }
    
    /**
     * Close the file
     * @throws java.io.IOException
     */
    public void closeFile() throws IOException {
        this.binaryFile.close();
    }
    
    /**
     * Give acces to the file
     * @return 
     */
    public RandomAccessFile getFile() {
        return this.binaryFile;
    }
    
    /**
     * Return a array of bytes of n lenght
     * @param startingPos
     * @param lenght
     * @return byte[]
     * @throws IOException
     */
    public byte[] getFilePart(long startingPos, int lenght) throws IOException {
        this.binaryFile.seek(startingPos);
        byte[] buffer = new byte[lenght];
        this.binaryFile.read(buffer);
        
        // If you want to display it in the console: Arrays.toString(...);
        
        return buffer;
    }

    /**
     * Return the hex value of a int
     * @param value
     * @return string
     */
    public String integerToHex(int value) {
        return Integer.toHexString(value);
    }
    
    /**
     * Return the hex value of a long
     * @param value
     * @return string
     */
    public String longToHex(long value) {
        return Long.toHexString(value);
    }
    
    /**
     * Return the int value of a hexadecimal string
     * @param value
     * @return string
     */
    public Integer hexToInt(String value) {
        return (int) Long.parseLong(value, 16);
    }
    
    /**
     * Return the long value of a hexadecimal string
     * @param value
     * @return string
     */
    public Long hexToLong(String value) {
        return Long.parseLong(value, 16);
    }
    
    /**
     * Go to a byte position
     * @param position
     * @throws IOException 
     */
    public void goToByte(int position) throws IOException {
        this.binaryFile.seek(position);
    }
    
    /**
     * Go to a byte position
     * @param position
     * @throws IOException 
     */
    public void goToByte(long position) throws IOException {
        this.binaryFile.seek(position);
    }
    
    /**
     * Write a int value at the current location of the file pointer
     * @param value
     * @throws IOException 
     */
    public void writeIntValue(int value) throws IOException {
        this.binaryFile.write(value);
    }
    
    /**
     * Write a hexadecimal string to the current location of the file pointer
     * @param value
     * @throws IOException 
     */
    public void writeHex(String value) throws IOException {
        this.binaryFile.writeBytes(value);
    }
    
    /**
     * Convert a bynary array to a string
     * @param array
     * @return string
     */
    public String binaryToText(byte[] array) {
        return new String(array);
    }
    
    
    /**
     * Return a list of position for a string to look for
     * @param search
     * @return List
     * @throws IOException 
     */
    public List<Long> getStringPosition(String search) throws IOException {
        return this.getStringPosition(search, 0);
    }
    

    /**
     * Return a list of position for a string to look for
     * @param search
     * @param startPosition
     * @return List
     * @throws IOException 
     */
    public List<Long> getStringPosition(String search, long startPosition) throws IOException { 
        int stringLenght = search.length();
        List<Long> results = new ArrayList();
        
        while (startPosition < (this.fileLenght - stringLenght)) {
            this.goToByte(startPosition);
            String currentBlock = this.binaryToText(this.getFilePart(startPosition, stringLenght));
            if(currentBlock.contentEquals(search)) {
                System.out.println("\nPosition: " + startPosition + " (" + this.longToHex(startPosition) + ")");
                System.out.println(currentBlock);
                results.add(startPosition);
            }
            startPosition++;
        }
         
        return results;
    }
    
    /**
     * Get the checksum of a file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException 
     * @author based on work of mkyong.com
     */
    public String getFileCheckSum() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        
        MessageDigest md = MessageDigest.getInstance("SHA1");
        FileInputStream fis = new FileInputStream(this.filePath);
        byte[] dataBytes = new byte[1024];

        int nread;
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = md.digest();

        //convert the byte to hex format
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        String checksum = sb.toString();
        System.out.println("Digest(in hex format):: " + checksum);
        
        return checksum;
    } 
            
    public long geFileLength()
    {
        return this.fileLenght;
    }
}
