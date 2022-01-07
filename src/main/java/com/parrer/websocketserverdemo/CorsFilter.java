package com.parrer.websocketserverdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class CorsFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    /**
     * 例外url，不进行参数校验
     */
    private final static Set<String> EXCEPTION_URL_PARAM = new HashSet<>();

    /**
     * 安全过滤的开关，1代表开
     */
    private final static Boolean OPEN_SECURITY_STATUS = true;


    @Override
    public void init(FilterConfig filterConfig){
        // 解决代码扫描 不要存在空方法
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        doCrossDomain(request, response);
        logger.info("URL: {}", request.getRequestURL());
        final String preReqMethodName = "OPTIONS";
        if (preReqMethodName.equals(request.getMethod().toUpperCase())) {
            response.setStatus(200);
            response.setHeader("Access-Control-Allow-Origin", "*");
//            response.setHeader("Access-Control-Allow-Headers", "bt,ct");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Request-Method", "GET,POST");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 解决代码扫描 不要存在空方法
    }

    /**
     * 处理跨域
     *
     * @param request
     */
    private void doCrossDomain(HttpServletRequest request, HttpServletResponse response) {
        String originUrl = (request.getHeader("Origin") != null ? request.getHeader("Origin")
                : request.getHeader("Referer"));
        if (originUrl != null) {
            String[] remoteUrl = originUrl.split("/");
            String url = remoteUrl[0] + "//" + remoteUrl[1] + remoteUrl[2];
            // 允许特定的跨域url
//            response.setHeader("Access-Control-Allow-Origin", url);
            response.setHeader("Access-Control-Allow-Origin", "*");
            // 允许特定的请求头
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, X-Requested-With, Content-Type, Accept, PT_TOKEN,Authorization");
            // 允许特定的请求方式
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json;charset=UTF-8");
    }

}
