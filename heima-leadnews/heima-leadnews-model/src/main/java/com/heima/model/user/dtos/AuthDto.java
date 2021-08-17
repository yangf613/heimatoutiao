package com.heima.model.user.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class AuthDto extends PageRequestDto {

    private Integer id;
    //审核驳回原因
    private String msg;
    //状态
    private Short status;
}
