package com.haha.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 读取配置文件
 */
@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil(){}

    public static Properties readPropertiesFile(String fileName){
        //得到的也是当前ClassPath的绝对URI路径。
        //如：file:/D:/java/eclipse32/workspace/jbpmtest3/bin/
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        System.out.println(url.toString());
        System.out.println(url.getPath());
        String rpcConfigPath = "";
        if(url!=null){
            rpcConfigPath = url.getPath()+fileName;
        }
        Properties properties = null;
        try(InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)
        ){
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (FileNotFoundException e) {
            //log.error("occur exception when read properties file [{}]", fileName);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
