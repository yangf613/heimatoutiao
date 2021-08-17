package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApUserRealnameService extends IService<ApUserRealname> {
    /**
     * 根据状态查询用户认证信息列表
     * @param dto
     * @return
     */
    ResponseResult loadListByStatus(AuthDto dto);

    /**
     * 根据状态进行审核
     * @param dto
     * @param status
     * @return
     */
    ResponseResult updateStatusById(AuthDto dto, Short status);
}
