/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metamug.mtg.s3.uploader;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

/**
 *
 * @author deepak
 */
public class S3Uploader {

    private static final String AWS_S3_BUCKET = "www.metamug.com";
    private static final String AWS_ACCESS_KEY = "KEY";
    private static final String AWS_SECRET_KEY = "SECRET";
    public final static AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
    private final static AmazonS3Client s3Client = new AmazonS3Client(credentials);

    public static void main(String[] args) {
        String path = args[0];
        String uri = args[1];
        String url = "";
        if (StringUtils.isNotBlank(path)) {
            byte[] buffer = getBytes(path);
            url = upload(new ByteArrayInputStream(buffer),
                    buffer.length,
                    uri);
        }
        System.out.println("");
    }

    public static String upload(InputStream inputStream, long fileSize, String URI) {
        String publicURL;
        //ClientConfiguration max retry
        ObjectMetadata objectMetaData = new ObjectMetadata();
        objectMetaData.setContentLength(fileSize);
//        objectMetaData.setContentType(IMAGE_CONTENT_TYPE);
        objectMetaData.setCacheControl("public");
        Calendar c = Calendar.getInstance();
        c.setTime(c.getTime());
        c.add(Calendar.MONTH, 6);
        String sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz").format(c.getTime());
        objectMetaData.setHeader("Expires", sdf);//Thu, 21 Mar 2042 08:16:32 GMT

        PutObjectResult por = s3Client.putObject(
                new PutObjectRequest(AWS_S3_BUCKET, URI,
                        inputStream,
                        objectMetaData)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        publicURL = "http://metamug.net/" + URI;
        return publicURL;
    }

    public static byte[] getBytes(String path) {
        byte[] buffer = null;
        try {
            //since these domains are static .. specific headers are not needed.
            buffer = Jsoup.connect(path)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .timeout(12000)
                    .followRedirects(true)
                    .execute().bodyAsBytes();
        } catch (IOException ex) {
            Logger.getLogger(S3Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return buffer;
    }
}
