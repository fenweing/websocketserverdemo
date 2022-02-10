package com.parrer.websocketserverdemo;

import com.parrer.scriptGenerate.ApiDemoGenerateWrapper;
import com.parrer.scriptGenerate.ApiMethodDemo;
import com.parrer.scriptGenerate.JavaScriptGenerate;
import com.parrer.util.CollcUtil;
import org.junit.jupiter.api.Test;

//@SpringBootTest
class WebsocketserverdemoApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        ApiDemoGenerateWrapper apiCommonDemoGenerateWrapper = ApiDemoGenerateWrapper.of().withControllerPackage("com.parrer.websocketserverdemo")
                .withDomainName("Tracker")
                .withControllerPackage("com.parrer.websocketserverdemo.tracker")
                .withServicePackage("com.parrer.websocketserverdemo.tracker")
                .withMainPath("/tracker")
                .withRequestParamPackage("com.parrer.websocketserverdemo.tracker")
                .withApiMethodDemoList(CollcUtil.ofList(
                        ApiMethodDemo.of()
                                .withMethodName("getTransmissionTrackerList")
                                .withApilog(true)
                                .withApilogPrintResult(false)
                                .withApiType("get")
                                .withApiPath("/transmission/list")
                                .withResponseType("ResultResponse")
                                .withReturnType("String"))
                );
        JavaScriptGenerate.of(WebsocketserverdemoApplicationTests.class)
                .ofBridgePath("src/main/java")
                .generateApiDemoClass(CollcUtil.ofList(apiCommonDemoGenerateWrapper));

    }

}
