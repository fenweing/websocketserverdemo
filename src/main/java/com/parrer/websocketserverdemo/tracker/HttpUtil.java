package com.parrer.websocketserverdemo.tracker;

import com.parrer.annotation.I18nMessage;
import com.parrer.exception.ServiceException;
import com.parrer.util.AssertUtil;
import com.parrer.util.ExceptionUtil;
import com.parrer.util.StringUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    private final static int SOCKET_TIMEOUT = 3000;
    private final static int COLLECTION_TIMEOUT = 5000;

    @I18nMessage(zh = {"远程下载文件失败，地址-【{}】，返回码-【{}】，返回消息-【{}】", "远程下载文件失败，地址-【{}】"}
            , en = {"download file failed,url-[{}],resCode-[{}],resMsg-[{}]", "download file failed,url-[{}]"})
    public static InputStream requestFile(String sourceUrl, String targetPath) {
        AssertUtil.isFalse(StringUtil.isBlankLeastone(sourceUrl, targetPath), "文件网络地址和下载存放地址不能为空！");
        GetMethod get = new GetMethod(sourceUrl);
        final HttpClient httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(buildManagerParams());
        try {
            httpClient.executeMethod(get);
            if (get.getStatusCode() != HttpStatus.SC_OK) {
                get.abort();
                ExceptionUtil.doThrow(0, sourceUrl, get.getStatusCode(), get.getResponseBodyAsString());
            } else {
                write(get.getResponseBodyAsStream(), targetPath);
            }
        } catch (Exception e) {
            ExceptionUtil.doThrow(e, 1, sourceUrl);
        } finally {
            get.releaseConnection();
        }
        return null;
    }

    public static String get(String sourceUrl) {
        AssertUtil.isFalse(StringUtil.isBlankLeastone(sourceUrl), "文件网络地址不能为空！");
        GetMethod get = new GetMethod(sourceUrl);
        final HttpClient httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(buildManagerParams());
        try {
            httpClient.executeMethod(get);
            if (get.getStatusCode() != HttpStatus.SC_OK) {
                get.abort();
                throw new ServiceException("请求失败-{}", sourceUrl);
            }
            try (InputStream responseBodyAsStream = get.getResponseBodyAsStream()) {
                return IOUtils.toString(responseBodyAsStream, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new ServiceException(e, "请求失败-{}", sourceUrl);
        } finally {
            get.releaseConnection();
        }
    }

    private static HttpConnectionManager buildManagerParams() {
        HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
        HttpConnectionManagerParams managerParams = new HttpConnectionManagerParams();
        managerParams.setSoTimeout(SOCKET_TIMEOUT);
        managerParams.setConnectionTimeout(COLLECTION_TIMEOUT);
        connectionManager.setParams(managerParams);
        return connectionManager;
    }

    private static void write(InputStream inputStream, String targetPath) throws IOException {
        File file = new File(targetPath);
        try (OutputStream outputStream = FileUtils.openOutputStream(file)) {
            byte[] bytes = new byte[1024];
            int i;
            while ((i = (inputStream.read(bytes))) != -1) {
                outputStream.write(bytes, 0, i);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new ServiceException("流写入文件失败，文件路径-{}", targetPath);
        }
        inputStream.close();
    }

}
