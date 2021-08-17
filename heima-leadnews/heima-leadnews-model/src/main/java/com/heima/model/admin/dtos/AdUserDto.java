package com.heima.model.admin.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("封装登录功能的请求参数")
public class AdUserDto {

    //用户名
    @ApiModelProperty("用户名")
    private String name;

    //密码
    @ApiModelProperty("密码")
    private String password;
}