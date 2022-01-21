package com.parrer.websocketserverdemo.cfind.controller;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddReferenceReq {
    @NotBlank(message = "类型不能为空！")
    private String type;
    @NotBlank(message = "内容不能为空！")
    private String reference;
    private String filename;

    public static AddReferenceReq of() {
        return new AddReferenceReq();
    }


}
