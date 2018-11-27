package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private static FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uploadFileBatch(List<File> fileList){
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始上传文件");
        boolean isSuccess = false;
        try {
            isSuccess = ftpUtil.uploadFile("img",fileList);
        } catch (IOException e) {
            logger.error("FtpClient disconnect Exception",e);
        }
        logger.info("上传文件结束");
        return isSuccess;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException{
        boolean upload = true;
        FileInputStream fis = null;
        //连接FTP服务器
        if(connectFTPServer(this.ip,this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.setBufferSize(1024);
                ftpClient.enterLocalPassiveMode();
                if(fileList!=null){
                    for(File fileItem: fileList){
                        fis = new FileInputStream(fileItem);
                        ftpClient.storeFile(fileItem.getName(), fis);
                    }
                }else {
                    upload = false;
                }
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                upload = false;
            }
            finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return upload;
    }

    private boolean connectFTPServer(String ip, int port, String user, String pwd){
        boolean isSuccess = false;
        ftpClient = new FTPClient();

        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("FTP连接异常",e);
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
