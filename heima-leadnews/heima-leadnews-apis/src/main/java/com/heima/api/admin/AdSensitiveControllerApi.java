package com.heima.api.admin;

import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "敏感词管理" , tags = "sensitive", description = "敏感词管理API")
public interface AdSensitiveControllerApi {

    /**
     * 根据名称列表分页查询敏感词
     * @param dto
     * @return
     */
    @ApiOperation(value = "敏感词分页列表查询")
     ResponseResult listSensitive(SensitiveDto dto);

    /**
     * 新增敏感词
     * @param adSensitive
     * @return
     */
    @ApiOperation(value = "新增敏感词")
    ResponseResult saveSensitive(AdSensitive adSensitive);

    /**
     * 修改敏感词
     * @param adSensitive
     * @return
     */
    @ApiOperation(value = "修改敏感词")
    ResponseResult updateSensitive(AdSensitive adSensitive);

    /**
     * 根据id删除敏感词
     * @param id
     * @return
     */
    @ApiOperation(value = "删除敏感词")
    ResponseResult deleteSensitive(Integer id);


}
