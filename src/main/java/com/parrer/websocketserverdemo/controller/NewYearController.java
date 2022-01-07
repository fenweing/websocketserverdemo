package com.parrer.websocketserverdemo.controller;

import com.parrer.annotation.ApiLog;
import com.parrer.exception.ServiceException;
import com.parrer.util.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.util.Date;

@RestController
@RequestMapping("/newyear")
@ApiLog
public class NewYearController {
    @PostMapping("/question")
    public String addQuestion(@RequestBody NewYearAddQuestionReq req) {
        if (StringUtils.isBlank(req.getContent())) {
            return "success";
        }
        try (FileWriter fileWriter = new FileWriter("newyearQuestion", true);) {
            fileWriter.write(DateUtil.format(new Date()) + " " + req.getContent());
            fileWriter.write("\r\n");
            return "success";
        } catch (Exception e) {
            throw new ServiceException(e, "write question error!");
        }
    }

    @Data
    public static class NewYearAddQuestionReq {
        private String content;
    }

}
