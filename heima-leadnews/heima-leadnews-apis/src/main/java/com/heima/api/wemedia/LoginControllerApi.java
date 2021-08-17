package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dtos.WmUserDto;

public interface LoginControllerApi {
    /**
     * 自媒体用户登录
     * @param dto
     * @return
     */
    ResponseResult login(WmUserDto dto);
}
