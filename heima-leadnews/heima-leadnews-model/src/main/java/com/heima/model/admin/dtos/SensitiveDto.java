package com.heima.model.admin.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("封装Sensitive分页查询的请求参数")
public class SensitiveDto extends PageRequestDto {

    /**
     * 敏感词名称
     */
    @ApiModelProperty("敏感词名称")
    private String name;
}
