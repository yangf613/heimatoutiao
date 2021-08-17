package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.dtos.WmUserDto;
import com.heima.model.media.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体用户登录
     * @param dto
     * @return
     */
    ResponseResult login(WmUserDto dto);
}
