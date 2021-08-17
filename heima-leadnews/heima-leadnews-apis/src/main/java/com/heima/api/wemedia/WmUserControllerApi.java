package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.media.pojos.WmUser;

public interface WmUserControllerApi {

    /**
     * 保存自媒体用户
     * @param wmUser
     * @return
     */
    ResponseResult save(WmUser wmUser);


    /**
     * 根据名称查询用户
     * @param name
     * @return
     */
    WmUser findByName(String name);

    /**
     * 根据id查询自媒体用户
     * @param id
     * @return
     */
    WmUser findWmUserById(Long id);

}
