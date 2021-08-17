package com.heima.api.admin;

import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "频道管理" , tags = "channel", description = "频道管理API")
public interface ChannelControllerApi {

    /**
     * 根据名称分页查询
     * @param dto
     * @return
     */
    @ApiOperation(value = "频道分页列表查询")
    ResponseResult findByNameAndPage(ChannelDto dto);

    /**
     * 新增频道
     * @param adChannel
     * @return
     */
    @ApiOperation(value = "新增频道")
    ResponseResult saveAdChannel(AdChannel adChannel);

    /**
     * 频道修改&频道有效无效设置
     * @param adChannel
     * @return
     */
    @ApiOperation(value = "频道修改&频道有效无效设置")
    ResponseResult updateAdChannel(AdChannel adChannel);

    /**
     * 删除频道
     * @param id
     * @return
     */
    @ApiOperation(value = "删除频道")
    ResponseResult deleteAdChannel(Integer id);

    /**
     * 查询所有频道
     * @return
     */
    ResponseResult findAll();
}
