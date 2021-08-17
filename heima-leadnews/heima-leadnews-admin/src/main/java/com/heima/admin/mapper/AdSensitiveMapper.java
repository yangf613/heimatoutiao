package com.heima.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.admin.pojos.AdSensitive;

import java.util.List;

public interface AdSensitiveMapper extends BaseMapper<AdSensitive> {

    public List<String> findAllSensitive();
}
