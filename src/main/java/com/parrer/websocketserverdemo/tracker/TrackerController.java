package com.parrer.websocketserverdemo.tracker;

import com.parrer.annotation.ApiLog;
import com.parrer.component.BaseImpl;
import com.parrer.exception.ServiceException;
import com.parrer.function.FFunction;
import com.parrer.function.FSupplier;
import com.parrer.thread.ScheduledExecutor;
import com.parrer.util.CollcUtil;
import com.parrer.util.LogUtil;
import com.parrer.websocketserverdemo.cfind.controller.ResultResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@RestController
@RequestMapping("/tracker")
public class TrackerController extends BaseImpl implements ApplicationRunner {
    private static String TRANSMISSION = "transmission";
    private static String ARIA = "aria";
    private static String URL_GITHUB = "github";

    private LinkedMultiValueMap<String, String> urlMap;

    @Autowired
    private ScheduledExecutor scheduledExecutor;
    private String transmissionFile = "transmission";
    private String ariaFile = "aria";

    @PostConstruct
    public void init() {
        urlMap = new LinkedMultiValueMap<>();
        urlMap.add(URL_GITHUB, "https://github.com/XIU2/TrackersListCollection/blob/master/all.txt");
        urlMap.add(URL_GITHUB, "https://github.com/ngosang/trackerslist/blob/master/trackers_all.txt");
        urlMap.add(URL_GITHUB, "https://github.com/XIU2/TrackersListCollection/blob/master/all.txt");
        addStrategyGroup(this.getClass().getName())
                .addStrategy(TRANSMISSION, (FSupplier<String>) () -> transmissionTracker())
                .addStrategy(ARIA, (FSupplier<String>) () -> ariaTracker())
                .addStrategy(URL_GITHUB, (FFunction<String, List<String>>) url -> resolveGithubTracker(url));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduledExecutor.scheduleWithFixedDelay(0L, 15L, TimeUnit.MINUTES, () -> {
            try {
                refreshFile();
            } catch (Exception e) {
                LogUtil.error(e, "刷新tracker文件失败！");
            }
        });
    }

    private void refreshFile() {
        String transmissionTracker = transmissionTracker();
        if (!isEmpty(transmissionTracker)) {
            writeFile(transmissionFile, transmissionTracker);
        }
        String ariaTracker = ariaTracker();
        if (!isEmpty(ariaTracker)) {
            writeFile(ariaFile, ariaTracker);
        }
    }

    private void writeFile(String fileName, String tracker) {
        try (FileWriter fileWriter = new FileWriter(fileName, false);) {
            fileWriter.write(tracker);
        } catch (Exception e) {
            throw new ServiceException(e, "write tracker error!");
        }
    }

    private List<String> resolveGithubTracker(String url) {
        List<String> allList = new ArrayList();
        String html = getHtmlByUrl(url);
        if (isEmpty(html)) {
            return allList;
        }
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsContainingText("/announce");
        if (elements == null || elements.isEmpty()) {
            return allList;
        }
        elements.forEach(ele -> {
            String tagName = ele.tagName();
            if (!"td".equals(tagName)) {
                return;
            }
            String tracker = ele.text();
            if (isEmpty(tracker)) {
                return;
            }
            allList.add(tracker);
        });
        return allList;
    }

    private String ariaTracker() {
        Set<String> trackerLineSet = getTrackerLineList();
        if (CollectionUtils.isEmpty(trackerLineSet)) {
            return EMPTY;
        }
        return String.join(",", trackerLineSet);
    }

    private String transmissionTracker() {
        Set<String> trackerLineSet = getTrackerLineList();
        if (CollectionUtils.isEmpty(trackerLineSet)) {
            return EMPTY;
        }
        return String.join("\r\n", trackerLineSet);
    }

    private Set<String> getTrackerLineList() {
        Set<String> allSet = new HashSet<>();
        if (MapUtils.isEmpty(urlMap)) {
            return allSet;
        }
        urlMap.forEach((type, urlList) -> {
            if (isEmpty(type) || CollectionUtils.isEmpty(urlList)) {
                return;
            }
            urlList.forEach(url -> {
                if (isEmpty(url)) {
                    return;
                }
                List<String> trackerList;
                try {
                    trackerList = ((FFunction<String, List<String>>) getStrategyGroup(TrackerController.class.getName()).getStrategy(type))
                            .apply(url);
                } catch (Exception e) {
                    LogUtil.error(e, "解析html tracker出错！");
                    return;
                }
                if (CollectionUtils.isEmpty(trackerList)) {
                    return;
                }
                allSet.addAll(trackerList);
            });
        });
        return allSet;
    }

    private String getHtmlByUrl(String url) {
        return HttpUtil.get(url);
    }

    @GetMapping("/transmission")
    @ApiLog
    public ResultResponse getTransmissionTracker() {
        String tracker = ((FSupplier<String>) getStrategyGroup(this.getClass().getName()).getStrategy(TRANSMISSION))
                .supply();
        return ResultResponse.ok(tracker);
    }

    @GetMapping("/transmissionFile")
    @ApiLog
    public void getTransmissionTrackerFile(HttpServletResponse response) {
        String tracker = getTrackerFromFile(transmissionFile);
        try {
            response.getWriter().write(tracker);
        } catch (IOException e) {
            throw new ServiceException(e, "回写tracker文件出错！");
        }
    }

    @GetMapping("/ariaFile")
    @ApiLog
    public void getAriaTrackerFile(HttpServletResponse response) {
        String tracker = getTrackerFromFile(ariaFile);
        try {
            response.getWriter().write(tracker);
        } catch (IOException e) {
            throw new ServiceException(e, "回写tracker文件出错！");
        }
    }

    private String getTrackerFromFile(String fileName) {
        try {
            return FileUtils.readFileToString(new File(fileName), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ServiceException(e, "读取tracker文件内容出错-{}", fileName);
        }
    }

    public static void main(String[] args) throws IOException {
        ArrayList<String> strings = CollcUtil.ofList("1", "2");
        String join = String.join("\r\n", strings);
        System.out.println(join);
    }


}
