//package com.parrer.websocketserverdemo;
//
//import com.parrer.scriptGenerate.ApiDemoGenerateWrapper;
//import com.parrer.scriptGenerate.ApiMethodDemo;
//import com.parrer.scriptGenerate.JavaScriptGenerate;
//import com.parrer.util.CollcUtil;
//import org.junit.jupiter.api.Test;
//
////@SpringBootTest
//class WebsocketserverdemoApplicationTests {
//
//    @Test
//    void contextLoads() {
//    }
//
//    public static void main(String[] args) {
//        ApiDemoGenerateWrapper apiCommonDemoGenerateWrapper = ApiDemoGenerateWrapper.of().withControllerPackage("com.parrer.websocketserverdemo")
//                .withDomainName("NewYear")
//                .withServicePackage("com.parrer.apidemo")
//                .withMainPath("/commonDemo")
//                .withRequestParamPackage("com.parrer.apidemo")
//                .withApiMethodDemoList(CollcUtil.ofList(
//                        ApiMethodDemo.of()
//                                .withMethodName("getCommonDemo")
//                                .withApilog(true)
//                                .withApilogPrintResult(false)
//                                .withApiType("get")
//                                .withApiPath("/getCommonDemo")
//                                .withResponseType("ResponseEntity")
//                                .withReturnType("List")
//                                .withReturnGenerateType("String"),
//                        ApiMethodDemo.of()
//                                .withMethodName("addCommonDemo")
//                                .withApilog(true)
//                                .withApilogPrintResult(true)
//                                .withApiType("post")
//                                .withApiPath("/addCommonDemo")
//                                .withResponseType("ResponseEntity")
//                                .withValidate(true)
//                                .withReturnType("List")
//                                .withReturnGenerateType("String"),
//                        ApiMethodDemo.of()
//                                .withMethodName("updateCommonDemo")
//                                .withApiType("post")
//                                .withApiPath("/updateCommonDemo")
//                                .withResponseType("ResponseEntity")
//                                .withValidate(true)
//                                .withReturnType(void.class.getSimpleName())
//                ));
//        JavaScriptGenerate javaScriptGenerate = new JavaScriptGenerate() {
//            @Override
//            public String getBridgePath() {
//                return "src/test/java";
//            }
//        };
//        javaScriptGenerate.generateApiDemoClass(CollcUtil.ofList(apiDemoGenerateWrapper, apiCommonDemoGenerateWrapper));
//
//    }
//
//}
