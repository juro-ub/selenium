package utils;

import java.io.*;

public class Properties extends java.util.Properties {

    public Properties() throws Exception {
        load();
    }

    public void load() throws Exception {
        InputStream input = Properties.class.getClassLoader().getResourceAsStream("junit.properties");
        super.load(input);
    }

    public String getFileChrome() {
        return this.getProperty("file_chrome_driver");
    }
}
