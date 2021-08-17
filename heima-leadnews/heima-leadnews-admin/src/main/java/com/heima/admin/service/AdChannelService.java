package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;

public interface AdChannelService extends IService<AdChannel> {

    /**
     * 根据名称分页查询
     */
     ResponseResult findByNameAndPage(ChannelDto dto);

    /**
     * 新增频道
     * @param adChannel
     * @return
     */
    ResponseResult saveAdChannel(AdChannel adChannel);

    /**
     * 修改频道
     * @param adChannel
     * @return
     */
    ResponseResult updateAdChannel(AdChannel adChannel);

    /**
     * 删除频道
     * @param id
     * @return
     */
    ResponseResult deleteAdChannel(Integer id);
}
