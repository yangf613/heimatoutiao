package com.heima.wemedia.maper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.media.vo.WmNewsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmNewsMapper extends BaseMapper<WmNews> {

    List<WmNewsVo> findListAndPage(@Param("dto") NewsAuthDto dto);

    int findListCount(@Param("dto") NewsAuthDto dto);
}