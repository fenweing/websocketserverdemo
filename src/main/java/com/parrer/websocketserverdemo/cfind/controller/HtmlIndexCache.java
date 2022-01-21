package com.parrer.websocketserverdemo.cfind.controller;

import com.parrer.exception.ServiceException;
import com.parrer.util.LogUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class HtmlIndexCache {
    public LinkedMultiValueMap<String, String> htmlIndexCacheMap = new LinkedMultiValueMap<String, String>();
    @Value("${cfind.htmlIndexFilePath:/cfind/htmlIndexFile}")
    private String htmlIndexFilePath;
    private final static String INDEX_SPLIT = "<<<<>>>>";

    @PostConstruct
    public void init() {
        try {
            File htmlIndexFile = new File(htmlIndexFilePath);
            if (!htmlIndexFile.exists()) {
                htmlIndexFile.createNewFile();
            }
            List<String> indexLines = FileUtils.readLines(htmlIndexFile, StandardCharsets.UTF_8);
            if (CollectionUtils.isEmpty(indexLines)) {
                LogUtil.warn("html索引缓存为空！");
            }
            indexLines.forEach(index -> {
                if (StringUtils.isEmpty(index)) {
                    return;
                }
                String[] split = getIndexLineArr(index);
                if (ArrayUtils.isEmpty(split) || split.length < 2) {
                    return;
                }
                htmlIndexCacheMap.add(split[1], split[0]);
            });
        } catch (IOException e) {
            throw new ServiceException(e, "初始化html索引缓存失败！");
        }
    }

    private String[] getIndexLineArr(String index) {
        return index.split(INDEX_SPLIT);
    }

    public void add(String uuid, String keyword) {
        htmlIndexCacheMap.add(keyword, uuid);
    }

    public List<String> get(String keyword) {
        return htmlIndexCacheMap.get(keyword);
    }

    public String getIndexLine(String keyword, String uuid) {
        return keyword + INDEX_SPLIT + uuid;
    }
}
