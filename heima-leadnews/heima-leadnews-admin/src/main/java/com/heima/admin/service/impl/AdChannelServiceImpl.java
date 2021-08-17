package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdChannelMapper;
import com.heima.admin.service.AdChannelService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AdChannelServiceImpl extends ServiceImpl<AdChannelMapper, AdChannel>
        implements AdChannelService {

    @Override
    public ResponseResult findByNameAndPage(ChannelDto dto) {
        //参数校验
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //分页参数检查
        dto.checkParam();
        //分页查询，根据名称模糊查询
        Page page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AdChannel> query = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(dto.getName())){
            query.like(AdChannel::getName , dto.getName());
        }

        IPage iPage = page(page, query);

        //返回结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) iPage.getTotal());
        responseResult.setData(iPage.getRecords());
        return responseResult;
    }

    /**
     * 新增频道
     * @param adChannel
     * @return
     */
    @Override
    public ResponseResult saveAdChannel(AdChannel adChannel) {
        //校验参数
        if(adChannel == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //保存
        adChannel.setCreatedTime(new Date());
        save(adChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 修改频道
     * @param adChannel
     * @return
     */
    @Override
    public ResponseResult updateAdChannel(AdChannel adChannel) {
        //校验参数
        if(adChannel == null || adChannel.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //修改频道
        updateById(adChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除频道
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteAdChannel(Integer id) {
        //校验参数
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断当前频道是否存在 和 是否有效
        AdChannel adChannel = getById(id);
        if(adChannel == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if(adChannel.getStatus()){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"不能删除有效频道");
        }

        //删除频道
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
