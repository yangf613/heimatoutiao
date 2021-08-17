package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.LoginService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LoginServiceImpl extends ServiceImpl<AdUserMapper, AdUser> implements LoginService {

    @Override
    public ResponseResult login(AdUserDto dto) {
        //校验参数
        if(StringUtils.isEmpty(dto.getName()) || StringUtils.isEmpty(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE, "用户名或密码不能为空");
        }

        //根据用户名查询数据库中的用户信息
        QueryWrapper<AdUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",dto.getName());

        List<AdUser> adUserList = list(queryWrapper);
        if(adUserList != null && adUserList.size() == 1){
            AdUser adUser = adUserList.get(0);

            String password = DigestUtils.md5DigestAsHex((dto.getPassword() + adUser.getSalt()).getBytes());
            if(adUser.getPassword().equals(password)){
                Map<String,Object> map = Maps.newHashMap();
                adUser.setSalt("");
                adUser.setPassword("");
                map.put("token", AppJwtUtil.getToken(adUser.getId().longValue()));
                map.put("user",adUser);
                return ResponseResult.okResult(map);
            }else {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
        }else{
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在，请确认");
        }
    }
}
