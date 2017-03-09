package yucl.learn.demo.fs.service.impl;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Created by yuchunlei on 2017/3/7.
 */
public class FileContentHash {

    public static String getFileMD5(File file) throws IOException {
        HashCode md5 = Files.hash(file, Hashing.md5());
        String md5Hex = md5.toString();
        return md5Hex;
    }

    public static String getFileSHA1(File file) throws IOException {
        HashCode sha1 = Files.hash(file, Hashing.sha1());
        String sha1Hex = sha1.toString();
        return sha1Hex;
    }

    public static String getFileCRC32(File file) throws IOException {
        HashCode crc32 = Files.hash(file, Hashing.crc32());
        int crc32Int = crc32.asInt();
        // the Checksum API returns a long, but it's padded with 0s for 32-bit CRC this is the value you would get if using that API directly
        //  long checksumResult = crc32.padToLong();
        return crc32.toString();
    }

    public static void main(String[] args){
        try {
           // System.out.println(getFileMD5(new File("F:\\server\\apache-tomcat-8.5.9\\apache-tomcat-8.5.9\\webapps\\examples\\m.mp4")));
            //System.out.println(getFileSHA1(new File("F:\\server\\apache-tomcat-8.5.9\\apache-tomcat-8.5.9\\webapps\\examples\\m.mp4")));
            System.out.println(getFileCRC32(new File("F:\\server\\apache-tomcat-8.5.9\\apache-tomcat-8.5.9\\webapps\\examples\\test.mp4")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
