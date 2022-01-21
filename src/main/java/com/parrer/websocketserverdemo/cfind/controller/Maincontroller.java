package com.parrer.websocketserverdemo.cfind.controller;

import com.parrer.annotation.ApiLog;
import com.parrer.component.BaseImpl;
import com.parrer.exception.ServiceException;
import com.parrer.function.FConsumer;
import com.parrer.util.AssertUtil;
import com.parrer.util.DateUtil;
import com.parrer.util.LogUtil;
import com.parrer.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@RestController
@ApiLog
@RequestMapping("/cfind")
public class Maincontroller extends BaseImpl implements ApplicationRunner {
    @Value("${cfind.command:/cfind/cfind.sh}")
    private String findCommandPath;
    @Value("${cfind.oriFilePath:/cfind/file}")
    private String oriFilePath;
    @Value("${cfind.attachFileDir:/cfind/attach}")
    private String attachFileDir;
    @Value("${cfind.docFileDir:/cfind/doc}")
    private String docFileDir;
    @Value("${cfind.htmlFileDir:/cfind/html}")
    private String htmlFileDir;
    @Value("${cfind.htmlIndexFilePath:/cfind/htmlIndexFile}")
    private String htmlIndexFilePath;
    @Autowired
    private HtmlIndexCache htmlIndexCache;
    String addEvent = "addEvent";
    String html = "html";
    String htmlReq = "htmlReq";
    @Value("${cfind.filterFileKeywordShellPath:/cfind/filterFileKeyword.sh}")
    private String filterFileKeywordShellPath;

    @GetMapping("/search/{keyword}")
    public ResponseEntity getByKeyword(@PathVariable("keyword") String keyword) {
        LogUtil.apiEntry(keyword);
        if (isBlank(keyword)) {
            log.error("blank keyword!");
            return ResponseEntity.ok().build();
        }
        MainResponse response = getAndResolve(keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getHtml/{dir}/{srcname}")
    public ResponseEntity getHtml(@PathVariable String dir, @PathVariable String srcname, HttpServletResponse
            response) {
        LogUtil.apiEntry(dir, srcname);
        AssertUtil.isFalse(StringUtil.isBlankLeastone(dir, srcname), "dir and srcname can not be blank!");
        File file = new File(htmlFileDir + "/" + dir + "/" + srcname);
        AssertUtil.isTrue(file.exists(), "src not exists,dir-{},srcname-{}", dir, srcname);
        try (FileInputStream fileInputStream = FileUtils.openInputStream(file);
             ServletOutputStream outputStream = response.getOutputStream();) {
            IOUtils.copy(fileInputStream, outputStream);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("read html file and write to response failed!", e);
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/getFile/{fileName}")
    public ResultResponse getHtml(@PathVariable String fileName, HttpServletResponse
            response) {
        fileName = getSafeFileName(fileName);
        AssertUtil.notEmpty(fileName, "空文件名！");
        File file = new File(docFileDir + "/" + fileName);
        AssertUtil.isTrue(file.exists(), "file not exists,fileName-{}", fileName);
        try (FileInputStream fileInputStream = FileUtils.openInputStream(file);
             ServletOutputStream outputStream = response.getOutputStream();) {
            String fileData = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
            IOUtils.copy(fileInputStream, outputStream);
            return ResultResponse.ok(fileData);
        } catch (Exception e) {
            throw new ServiceException(e, "获取文件失败-{}", fileName);
        }
    }

    private String getSafeFileName(String fileName) {
        return fileName.replace(".", "").replace("/", "").replace("\\", "");
    }

    @PostMapping("/addHtml")
    public ResultResponse addReference(@Validated @RequestBody AddHtmlReq addHtmlReq) {
        addHtmlReq.setHtml(htmlSpecialHandle(addHtmlReq.getUrl(), addHtmlReq.getTitle(), addHtmlReq.getHtml()));
        ((FConsumer) getStrategyGroup(addEvent).getStrategy(ReferenceTypeEnum.HTML.getKey())).consume(addHtmlReq);
        return ResultResponse.ok();
    }

    @PostMapping("/add")
    public ResponseEntity addReference(@Validated @RequestBody AddReferenceReq addReferenceReq) {
        String reference = addReferenceReq.getReference();
        addReferenceReq.setReference(reference.replace("%0A", "\r\n"));
        ((FConsumer) getStrategyGroup(addEvent).getStrategy(addReferenceReq.getType())).consume(addReferenceReq);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity upload(@RequestParam("file") MultipartFile multipartFile) {
        AssertUtil.notNull(multipartFile, "上传文件为空！");
        log.info("upload file-{}", multipartFile.getSize());
        try (InputStream inputStream = multipartFile.getInputStream();) {
            String format = new SimpleDateFormat(DateUtil.DATE_FORMAT_YMDHMS).format(new Date());
            File dir = new File(attachFileDir);
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
                AssertUtil.isTrue(mkdirs, "create attach dir failed!");
            }
            File file = new File(attachFileDir + "/" + format);
            boolean newFile = file.createNewFile();
            AssertUtil.isTrue(newFile, "create attach file failed!");
            FileUtils.copyInputStreamToFile(inputStream, file);
            return ResponseEntity.ok("/cfind/getAttach/" + format);
        } catch (Exception e) {
            log.error("error occurred when uploading file!", e);
            return ResponseEntity.status(500).body("upload failed!");
        }
    }

    @GetMapping("/getAttach/{id}")
    public ResponseEntity getAttach(@PathVariable String id, HttpServletResponse response) {
        LogUtil.apiEntry(id);
        File file = new File(attachFileDir + "/" + id);
        if (!file.exists()) {
            log.error("attach file not exists-{}!", id);
            return ResponseEntity.ok().build();
        }
        try (FileInputStream fileInputStream = FileUtils.openInputStream(file);
             ServletOutputStream outputStream = response.getOutputStream();) {
            IOUtils.copy(fileInputStream, outputStream);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("read attach file and write to response failed!", e);
            return ResponseEntity.ok().build();
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        addStrategyGroup(addEvent)
                .addStrategy(ReferenceTypeEnum.HTML.getKey(), (FConsumer<AddReferenceReq>) (reference) -> addHtml(reference))
                .addStrategy(htmlReq, (FConsumer<AddHtmlReq>) (addHtmlReq) -> dealAddHtml(addHtmlReq))
                .addStrategy(ReferenceTypeEnum.FILE.getKey(), (FConsumer<AddReferenceReq>) (reference) -> addFile(reference))
                .addStrategy(ReferenceTypeEnum.MD.getKey(), (FConsumer<AddReferenceReq>) (reference) -> addMd(reference))
                .addStrategy(ReferenceTypeEnum.UNKNOWN.getKey(), (FConsumer<AddReferenceReq>) (reference) -> addOther(reference));
    }

    private void addOther(AddReferenceReq reference) {
        LogUtil.info("添加内容类型未知！-{}", reference);
    }

    private void addMd(AddReferenceReq addReferenceReq) {
        String type = addReferenceReq.getType();
        String reference = addReferenceReq.getReference();
        //获取第一行
        String baser = reference.split("\r")[0];
        String basen = reference.split("\n")[0];
        String firstLine = baser.length() > basen.length() ? basen : baser;
        //获取第一行结束
        int kwIdx = firstLine.indexOf("??");
        String date = DateUtil.formatYYYYMMDD(new Date());
        String dateTime = DateUtil.format(new Date());
        if (kwIdx > -1 && kwIdx + 2 < firstLine.length()) {
            String keyword = firstLine.substring(kwIdx + 2);
            reference = "<div>" + keyword + "-begin_type[" + type + "]\r\n\r\n</br>"
                    + "date: " + dateTime + "\r\n\r\n</div>\r\n"
                    + reference + "\r\n<div>" + keyword + "-end_type[" + type + "]</div>";
        } else {
            reference = "<div>" + date + "-begin_type[" + type + "]\r\n\r\n</br>"
                    + "date: " + dateTime + "\r\n\r\n</div>\r\n"
                    + reference + "\r\n<div>" + date + "-end_type[" + type + "]</div>";
        }
        reference = "\r\n\r\n" + reference;
        File file = new File(oriFilePath);
        createAndWriteFile(reference, file, false);
    }

    private void addFile(AddReferenceReq addReferenceReq) {
        String filename = addReferenceReq.getFilename();
        AssertUtil.notEmpty(filename, "filename can not be blank!");
        if (StringUtils.startsWith(filename, "f:")) {
            filename = filename.substring(2);
            AssertUtil.notEmpty(filename, "filename can not be blank!");
        }
        String reference = addReferenceReq.getReference();
        reference = "<p>_type[file]</p>\r\n" + reference;
        File file = new File(docFileDir + "/" + filename);
        createAndWriteFile(reference, file, false);
    }

    private void addHtml(AddReferenceReq addReferenceReq) {
        String reference = addReferenceReq.getReference();
        AssertUtil.notEmpty(reference, "html content can not be blank!");
        //deal kw line
        String firstLine = reference.substring(0, reference.indexOf("\n"));
        int kwIdx = firstLine.indexOf("??");
        String keyword = firstLine.substring(kwIdx + 2);
        keyword = trim(keyword);
        String logReference = reference.replace("$", EMPTY).replace("{", EMPTY);
        AssertUtil.notEmpty(keyword, "key word is null,reference-{}", logReference);
        //deal kw line end
        //deal host line
        reference = reference.substring(firstLine.length() + 1);
        int hostEndIdx = reference.indexOf("\n");
        AssertUtil.isTrue(hostEndIdx != -1, "invalid html content-{}", logReference);
        String hostLine = reference.substring(0, hostEndIdx);
        String prefix = hostLine.startsWith("http://") ? "http://" :
                (hostLine.startsWith("https://") ? "https://" : "");
        AssertUtil.notEmpty(prefix, "invalid html content-{}", logReference);
        String substring = hostLine.substring(prefix.length());
        int sepIdx = substring.indexOf("/");
        String host = sepIdx == -1 ? substring : substring.substring(0, sepIdx);
        AssertUtil.notEmpty(host, "invalid html content-{}", logReference);
        host = prefix + host + "/";
        //deal host line end
        String htmlReference = reference.substring(hostLine.length());
        AssertUtil.notEmpty(htmlReference, "empty html reference,total reference-{}", logReference);
        //special condition handle
        htmlReference = htmlSpecialHandle(host, keyword, htmlReference);
        //special condition handle end
        //deal head block
        String completeHtml = EMPTY;
        if (htmlReference.startsWith("<htm")) {
            log.info("html literal mode");
            completeHtml = htmlReference;
        } else if (htmlReference.startsWith("<head")) {
            log.info("head mode");
            int headBeginIdx = htmlReference.indexOf("<head>");
            int headEndIdx = htmlReference.indexOf("</head>");
            String headBlock = "";
            if (headBeginIdx != -1 && headEndIdx != -1) {
                headBlock = htmlReference.substring(headBeginIdx, headEndIdx + 7);
            }
            //deal head block end
            //deal div block
            String divBlock = headEndIdx != -1 ? htmlReference.substring(headEndIdx + 8) : htmlReference;
            AssertUtil.notEmpty(divBlock, "divBlock can not be null!");
            //deal div block end
            completeHtml = "<html>" + headBlock + "<body>" + divBlock + "</body></html>";
        } else if (htmlReference.startsWith("<div")) {
            log.info("html and div mode");
            completeHtml = getHtmlFromUrlAndDiv(hostLine, htmlReference);
        }
        dealAddHtml(AddHtmlReq.of().withHtml(completeHtml).withUrl(host).withTitle(keyword));
    }

    private String htmlSpecialHandle(String host, String keyword, String htmlReference) {
        if (StringUtil.isBlankLeastone(keyword, host, htmlReference)) {
            return htmlReference;
        }
        if (host.toLowerCase().contains("csdn")) {
            htmlReference = htmlReference.replace("window.location.href", "");
        }
        return removeNextLineLabel(htmlReference);
    }

    private void dealAddHtml(AddHtmlReq addHtmlReq) {
        String keyword = trim(addHtmlReq.getTitle());
        String host = addHtmlReq.getUrl();
        String completeHtml = addHtmlReq.getHtml();
        AssertUtil.notEmpty(completeHtml, "html text is blank after resolve!");
        String uuid = getUUID();
        addIndexInfo(keyword, uuid);
        forceMkdir(htmlFileDir + "/" + uuid);
        completeHtml = dealScriptItem(completeHtml);
        completeHtml = dealLinkItem(completeHtml, host, uuid);
        completeHtml = dealImgItem(completeHtml, host, uuid);
        completeHtml = dealOther(completeHtml);
        boolean writeHtmlFile = createAndWriteFile(completeHtml, new File(htmlFileDir + "/" + uuid + "/" + uuid + ".html"), false);
        AssertUtil.isTrue(writeHtmlFile, "create html file failed!");
    }

    private void addIndexInfo(String keyword, String uuid) {
        String indexInfo = "\n" + htmlIndexCache.getIndexLine(keyword, uuid);
        createAndWriteFile(indexInfo, new File(htmlIndexFilePath), true);
        htmlIndexCache.add(uuid, keyword);
    }

    private void forceMkdir(String dirPath) {
        File htmlDir = new File(dirPath);
        try {
            FileUtils.forceMkdir(htmlDir);
        } catch (IOException e) {
            log.error("mkdir failed!-{}", dirPath, e);
            throw new ServiceException(e, "mkdir failed!");
        }
    }

    private String getUUID() {
        return UUID.randomUUID().toString().replace("_", "").toLowerCase();
    }

    private String dealImgItem(String completeHtml, String host, String dirName) {
        AssertUtil.notEmpty(completeHtml, "html string can not be blank!");
        AssertUtil.notEmpty(host, "host can not be blank!");
        Document doc = Jsoup.parse(completeHtml);
        Elements imgs = doc.getElementsByTag("img");
        if (imgs.size() == 0) {
            return completeHtml;
        }
        for (Element img : imgs) {
            try {
                String src = img.attr("src");
                if (isBlank(src)) {
                    continue;
                }
                src = startsWith(src, "http") ? src : (host + removeStart(removeStart(src, "/"), "/"));//存在这种形式的地址：//uri/path
                String imgName = HtmlUtil.getUrlSourceName(src);
                File file = new File(htmlFileDir + "/" + dirName + "/" + imgName);
                if (!file.exists()) {
                    boolean newFile = file.createNewFile();
                    AssertUtil.isTrue(newFile, "create new img file failed!");
                }
                com.parrer.util.HttpUtil.requestFile(src, file.getPath());
                img.attr("src", "/cfind/getHtml/" + dirName + "/" + imgName);
            } catch (Exception e) {
                log.error("deal img element failed!,link-{}", img.html());
            }
        }
        return doc.html();
    }

    private String removeNextLineLabel(String htmlReference) {
        if (isBlank(htmlReference)) {
            return htmlReference;
        }
        while (startsWith(htmlReference, "\r") || startsWith(htmlReference, "\n")
                || endsWith(htmlReference, "\r") || endsWith(htmlReference, "\n")) {
            htmlReference = removeStart(htmlReference, "\r");
            htmlReference = removeStart(htmlReference, "\n");
            htmlReference = removeEnd(htmlReference, "\r");
            htmlReference = removeEnd(htmlReference, "\n");
        }
        return htmlReference;
    }

    private String getHtmlFromUrlAndDiv(String hostLine, String htmlReference) {
        AssertUtil.notEmpty(StringUtil.isBlankLeastone(hostLine, htmlReference), "html url and div reference can not be blank!");
        Map<String, String> resMap = HttpUtil.init().get(hostLine);
        AssertUtil.isTrue("200".equals(resMap.get("statusCode")), "request html failed,return code-{}", resMap.get("statusCode"));
        String result = resMap.get("result");
        AssertUtil.notEmpty(result, "request html return blank!");
        Document doc = Jsoup.parse(result);
        Elements links = doc.getElementsByTag("link");
        String linkJoin = join(links, EMPTY);
        Elements styles = doc.getElementsByTag("style");
        String styleJoin = join(styles, EMPTY);
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html lang=\"zh-CN\"><head><meta charset=\"utf-8\"><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">")
                .append(linkJoin).append(styleJoin).append("</head><body>")
                .append(htmlReference).append("</body></html>");
        return htmlBuilder.toString();
    }

    private String dealOther(String completeHtml) {
        return completeHtml;
    }

    private String dealLinkItem(String completeHtml, String host, String dirName) {
        AssertUtil.notEmpty(completeHtml, "html string can not be blank!");
        AssertUtil.notEmpty(host, "host can not be blank!");
        Document doc = Jsoup.parse(completeHtml);
        Elements links = doc.getElementsByTag("link");
        if (links.size() == 0) {
            return completeHtml;
        }
        HttpUtil httpUtil = HttpUtil.init();
        for (Element link : links) {
            try {
                String href = link.attr("href");
                if (isBlank(href)) {
                    link.remove();
                    continue;
                }
                href = startsWith(href, "http") ? href : (host + removeStart(removeStart(href, "/"), "/"));//存在这种形式的地址：//uri/path
                String cssName = HtmlUtil.getUrlSourceName(href);
                if (!endsWith(cssName, ".css")) {
                    log.error("not css file!-{}", cssName);
                    continue;
                }
//                boolean css = "stylesheet".equals(link.attr("rel")) ||
//                        "text/css".equals(link.attr("type"));
//                if (!css) {
//                    link.remove();
//                    continue;
//                }
                Map<String, String> resMap = httpUtil.get(href);
                if (!"200".equals(resMap.get("statusCode"))) {
                    log.error("request css link failed!,href-{}", href);
                    continue;
                }
                String result = resMap.get("result");
                if (isBlank(result)) {
                    log.error("request css link return blank!,href-{}", href);
                    continue;
                }
                boolean writeFile = createAndWriteFile(result, new File(htmlFileDir + "/" + dirName + "/" + cssName), false);
                if (!writeFile) {
                    log.error("write css text to file failed!-{}", cssName);
                    continue;
                }
                link.attr("href", "/cfind/getHtml/" + dirName + "/" + cssName);
            } catch (Exception e) {
                log.error("deal link element failed!,link-{}", link.html());
            }
        }
        return doc.html();
    }

    private String dealScriptItem(String completeHtml) {
        AssertUtil.notEmpty(completeHtml, "html string can not be blank!");
        Document doc = Jsoup.parse(completeHtml);
        Elements script = doc.getElementsByTag("script");
        script.remove();
//        log.info(doc.html());
        //todo del script in iframe
        return doc.html();
    }

    private boolean createAndWriteFile(String reference, File file, boolean append) {
        try {
            if (!file.exists()) {
                log.info("ori file not exists,create one!");
                boolean newFile = file.createNewFile();
                AssertUtil.isTrue(newFile, "create new ori file failed!");
            }
            FileUtils.write(file, reference, StandardCharsets.UTF_8, append);
            return true;
        } catch (Exception e) {
            log.error("error occurred when write reference to orifile!", e);
            return false;
        }
    }


    private MainResponse getAndResolve(String keyword) {
        MainResponse mainResponse = new MainResponse();
        if (isBlank(keyword)) {
            return mainResponse;
        }
        return mainResponse.setHtmldir(getHtmlSourceList(keyword))
                .setFileNames(getFileSourceList(keyword));
    }

    private List<String> getFileSourceList(String keyword) {
        if (isBlank(keyword)) {
            return new ArrayList<>();
        }
        try (InputStream inputStream = getLinuxCommandInputStreamByShellPath(filterFileKeywordShellPath, keyword);) {
            if (inputStream == null) {
                return new ArrayList<>();
            }
            List<String> fileSourceList = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            return customizeFileSourceList(fileSourceList);
        } catch (Exception e) {
            throw new ServiceException(e, "根据keyword-{}获取文件列表失败！", keyword);
        }
    }

    private List<String> customizeFileSourceList(List<String> fileSourceList) {
        if (CollectionUtils.isEmpty(fileSourceList)) {
            return new ArrayList<>();
        }
        return fileSourceList.stream().filter(StringUtils::isNotBlank)
                .filter(fileSource -> StringUtils.isNotBlank(trim(fileSource)))
                .collect(Collectors.toList());
    }

    private InputStream getLinuxCommandInputStreamByShellPath(String filterFileKeywordShellPath, String keyword) {
        if (isBlank(keyword)) {
            return null;
        }
        Runtime run = Runtime.getRuntime();
        try {
            String param = "sh " + filterFileKeywordShellPath + " " + keyword;
            Process process = run.exec(param);
            return process.getInputStream();
        } catch (IOException e) {
            log.error("error occurred when get from linux!", e);
            return null;
        }
    }

    private List<String> getHtmlSourceList(String keyword) {
        return isBlank(keyword) ? new ArrayList<>() : htmlIndexCache.get(keyword);
    }


    public static void main(String[] args) {
        Maincontroller maincontroller = new Maincontroller();
//        MainResponse andResolve = new Maincontroller().getAndResolve("xx");
//        System.out.println(JsonUtil.toString(andResolve));
//        String ss = null;
//        try {
//            ss = FileUtils.readFileToString(new File("D:\\virtualBox\\shared\\mini.html"), StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String xx = new Maincontroller().dealScriptItem(ss);
//        System.out.println(xx);
        Map<String, String> resMap = HttpUtil.init().get("https://www.jianshu.com/p/2425a1c14755");
        String result = resMap.get("result");
//        log.info(result);
//        maincontroller.addHtml("??ii3\n" +
//                "https://www.jianshu.com/p/2425a1c14755\n" +
//                "<div ");
//        try {
//            FileUtils.write(new File("d:\\\\tt.html"), result);
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//        System.out.println(startsWith());
    }

    @Data
    public static class MainResponse {
        private String textareaContent;
        private String divContent;
        private List<String> htmldir = new ArrayList<>();
        private List<String> fileNames = new ArrayList<>();


        public MainResponse(String divContent, String textareaContent) {
            this.textareaContent = textareaContent;
            this.divContent = divContent;
        }

        public MainResponse() {
        }

        public MainResponse setTextareaContent(String textareaContent) {
            this.textareaContent = textareaContent;
            return this;
        }

        public MainResponse setDivContent(String divContent) {
            this.divContent = divContent;
            return this;
        }

        public MainResponse setHtmldir(List<String> htmldir) {
            this.htmldir = htmldir;
            return this;
        }

        public MainResponse setFileNames(List<String> fileNames) {
            this.fileNames = fileNames;
            return this;
        }
    }
}
