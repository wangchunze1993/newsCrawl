package com.neo.sk.arachne2.utils.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * User: meng
 * Date: 2015/2/11
 * Time: 10:22
 */
public class InputStreamUtils {
  private static final Logger log = LoggerFactory.getLogger(InputStreamUtils.class);
  private static final String defaultCharSet = "UTF-8";

  public InputStreamUtils() {
  }

  public static String inputStream2String(InputStream is, String charSet) {
    if(is != null) {
      StringBuilder sb = new StringBuilder();

      try {
        BufferedReader e = new BufferedReader(new InputStreamReader(is, charSet));

        String line;
        while((line = e.readLine()) != null) {
          sb.append(line).append("\n");
        }
      } catch (Exception var13) {
        ;
      } finally {
        try {
          is.close();
          return sb.toString();
        } catch (IOException var12) {
          var12.printStackTrace();
        }
      }
    }

    return null;
  }

  public static String inputStream2String(InputStream is) {
    return inputStream2String(is, "UTF-8");
  }

  public static void inputStream2File(InputStream is, String fileName, String charSet) {
    try {
      FileOutputStream e = new FileOutputStream(fileName);
      byte[] stream = new byte[1024];

      while(is.read(stream) != -1) {
        e.write(stream);
      }

      e.flush();
      e.close();
    } catch (FileNotFoundException var5) {
      var5.printStackTrace();
    } catch (IOException var6) {
      var6.printStackTrace();
    }

  }

  public static void inputStream2File(InputStream is, String fileName) {
    inputStream2File(is, fileName, "UTF-8");
  }

  public static HttpEntity getRealEntity(HttpEntity entity) {
    Header header = entity.getContentEncoding();
    Object entity2 = entity;
    if(header != null && header.getValue().toLowerCase().contains("gzip")) {
      entity2 = new GzipDecompressingEntity(entity);
    } else if(header != null && header.getValue().toLowerCase().contains("deflate")) {
      entity2 = new DeflateDecompressingEntity(entity);
    }

    return (HttpEntity)entity2;
  }

  public static String entity2String(HttpEntity entity, String charSet) {
    Header header = entity.getContentEncoding();
    Object entity2 = entity;
    if(header != null && header.getValue().toLowerCase().contains("gzip")) {
      entity2 = new GzipDecompressingEntity(entity);
    } else if(header != null && header.getValue().toLowerCase().contains("deflate")) {
      entity2 = new DeflateDecompressingEntity(entity);
    }

    try {
      return inputStream2String(((HttpEntity)entity2).getContent(), charSet);
    } catch (IOException var7) {
      log.warn(var7.getMessage());
      if(var7.getMessage().contains("GZIP format")) {
        log.info("not a gzip format, try normal");

        try {
          return inputStream2String(entity.getContent(), charSet);
        } catch (IOException var6) {
          log.warn(var6.getMessage());
        }
      }

      return null;
    }
  }

  public static String entity2String(HttpEntity entity) {
    return entity2String(entity, "UTF-8");
  }

  public static void saveFile(HttpEntity entity, String fileName) throws IOException {
    Header header = entity.getContentEncoding();
    Object entity2 = entity;
    if(header != null && header.getValue().toLowerCase().contains("gzip")) {
      log.info("entity content encoding is " + header.getValue());
      entity2 = new GzipDecompressingEntity(entity);
    } else if(header != null && header.getValue().toLowerCase().contains("deflate")) {
      log.info("entity content encoding is " + header.getValue());
      entity2 = new DeflateDecompressingEntity(entity);
    }

    inputStream2File(((HttpEntity)entity2).getContent(), fileName);
  }
}
