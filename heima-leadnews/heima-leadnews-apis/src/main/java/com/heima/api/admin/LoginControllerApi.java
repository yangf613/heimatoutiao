package com.heima.api.admin;

import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;

@Api(value = "登录管理" , tags = "login", description = "登录用户管理API")
public interface LoginControllerApi {

    /**
     * 登录功能
     * @param dto
     * @return
     */
    @ApiOperation(value = "登录功能")
    ResponseResult login(@RequestBody AdUserDto dto);
}
