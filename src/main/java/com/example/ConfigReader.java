package com.example;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
public class ConfigReader {
    private static Properties properties;
    static {
        try {
            InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");
            if (inputStream == null) {
                throw new IOException("Không tìm thấy file cấu hình: config.properties");
            }
            properties = new Properties();
            properties.load(inputStream);
            inputStream.close();

        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file cấu hình: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể đọc file cấu hình: " + e.getMessage());   
        }
        
    }
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }   
}
