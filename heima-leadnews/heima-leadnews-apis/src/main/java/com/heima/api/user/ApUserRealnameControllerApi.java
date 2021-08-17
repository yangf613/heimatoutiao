package com.heima.api.user;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;

public interface ApUserRealnameControllerApi {

    /**
     * 根据状态查询用户认证信息列表
     * @param dto
     * @return
     */
    ResponseResult loadListByStatus(AuthDto dto);

    /**
     * 审核通过
     * @param dto
     * @return
     */
    ResponseResult authPass(AuthDto dto) ;

    /**
     * 审核失败
     * @param dto
     * @return
     */
    ResponseResult authFail(AuthDto dto);
}
