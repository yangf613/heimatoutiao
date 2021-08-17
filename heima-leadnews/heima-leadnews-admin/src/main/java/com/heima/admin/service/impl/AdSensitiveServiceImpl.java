package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdSensitiveMapper;
import com.heima.admin.service.AdSensitiveService;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AdSensitiveServiceImpl extends ServiceImpl<AdSensitiveMapper, AdSensitive> implements AdSensitiveService {
    /**
     * 根据名称列表分页查询
     * @param dto
     * @return
     */
    @Override
    public ResponseResult listSensitive(SensitiveDto dto) {
        //检查参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();
        //根据名称模糊分页查询
        Page page = new Page(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<AdSensitive> wrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(dto.getName())){
            wrapper.like(AdSensitive::getSensitives , dto.getName());
        }
        IPage iPage = page(page, wrapper);
        //返回结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(), (int) iPage.getTotal());
        responseResult.setData(iPage.getRecords());
        return responseResult;
    }

    /**
     * 新增敏感词
     * @param adSensitive
     * @return
     */
    @Override
    public ResponseResult saveSensitive(AdSensitive adSensitive) {
        //校验参数
        if(adSensitive == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //新增敏感词
        adSensitive.setCreatedTime(new Date());
        save(adSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 修改敏感词
     * @param adSensitive
     * @return
     */
    @Override
    public ResponseResult updateSensitive(AdSensitive adSensitive) {
        //校验参数
        if(adSensitive == null || adSensitive.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //修改敏感词
        updateById(adSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除敏感词
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteSensitive(Integer id) {
        //校验参数
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断当前敏感词是否存在
        AdSensitive sensitive = getById(id);
        if(sensitive ==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //删除敏感词
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
