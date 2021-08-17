package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;

public interface AdSensitiveService extends IService<AdSensitive> {

    ResponseResult listSensitive(SensitiveDto dto);

    ResponseResult saveSensitive(AdSensitive adSensitive);

    ResponseResult updateSensitive(AdSensitive adSensitive);

    ResponseResult deleteSensitive(Integer id);
}
