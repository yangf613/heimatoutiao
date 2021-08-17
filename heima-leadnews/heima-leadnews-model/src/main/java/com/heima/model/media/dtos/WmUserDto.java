package com.heima.model.media.dtos;

import lombok.Data;

@Data
public class WmUserDto {

    /**
     * 用户名
     */
    private String name;

    /**
     * 密码
     */
    private String password;
}