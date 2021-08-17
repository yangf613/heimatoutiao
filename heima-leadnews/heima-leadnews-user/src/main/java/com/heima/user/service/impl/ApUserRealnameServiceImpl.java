package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.user.UserConstants;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.pojos.WmUser;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.feign.ApAuthorFeign.ArticleFeign;
import com.heima.user.feign.WmUserFeign.WemediaFeign;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname>
        implements ApUserRealnameService {

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private WemediaFeign wemediaFeign;

    @Autowired
    private ArticleFeign articleFeign;

    @Override
    public ResponseResult loadListByStatus(AuthDto dto) {
        //检验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //校验分页参数
        dto.checkParam();
        QueryWrapper<ApUserRealname> wrapper = new QueryWrapper<>();
        if(dto.getStatus() != null){
            wrapper.lambda().eq(ApUserRealname::getStatus,dto.getStatus());
        }
        Page page = new Page(dto.getPage(), dto.getSize());
        IPage ipage = page(page, wrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)ipage.getTotal());
        responseResult.setData(ipage.getRecords());
        return responseResult;
    }

    /**
     * 根据状态审核
     * @param dto
     * @param status
     * @return
     */
    @GlobalTransactional
    @Override
    public ResponseResult updateStatusById(AuthDto dto, Short status) {
        // 检查参数
        if(dto  == null ||dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if(statusCheck(status)){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 修改状态
        ApUserRealname apUserRealname = new ApUserRealname();
        apUserRealname.setId(dto.getId());
        apUserRealname.setStatus(status);
        if(dto.getMsg()!=null){
            apUserRealname.setReason(dto.getMsg());
        }
        updateById(apUserRealname);

        //审核通过后，添加到自媒体表和作者表
        if(status.equals(UserConstants.PASS_AUTH)){
            ResponseResult responseResult = createWmUserAndAuthor(dto);
            if(responseResult != null){
                return responseResult;
            }
            //TODO 发送通知消息
        }
        // int a = 3/0;
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 创建自媒体账号和作者账号
     * @param dto
     * @return
     */
    private ResponseResult createWmUserAndAuthor(AuthDto dto) {
        //添加自媒体账号, 查询ap_user信息封装到wmuser中
        ApUserRealname userRealname = getById(dto.getId());
        ApUser apUser = apUserMapper.selectById(userRealname.getUserId());
        if(apUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //检测自媒体用户是否存在
        WmUser wmUser = wemediaFeign.findByName(apUser.getName());
        if(wmUser == null || wmUser.getId() == null){
            wmUser = new WmUser();
            //创建自媒体用户信息
            wmUser.setApUserId(apUser.getId());
            wmUser.setCreatedTime(new Date());
            wmUser.setSalt(apUser.getSalt());
            wmUser.setName(apUser.getName());
            wmUser.setPassword(apUser.getPassword());
            wmUser.setStatus(9);
            wmUser.setPhone(apUser.getPhone());
            wemediaFeign.save(wmUser);
        }
        //创建作者账号信息
        createAuthor(wmUser);
        //修改用户标记、
        apUser.setFlag(1);
        apUserMapper.updateById(apUser);
        return null;
    }

    /**
     * 创建作者账号信息
     * @param wmUser
     */
    private void createAuthor(WmUser wmUser) {
        Integer apUserId = wmUser.getApUserId();
        ApAuthor apAuthor = articleFeign.findByUserId(apUserId);
        if (apAuthor == null) {
            apAuthor = new ApAuthor();
            apAuthor.setName(wmUser.getName());
            apAuthor.setType(UserConstants.AUTH_TYPE);
            apAuthor.setCreatedTime(new Date());
            apAuthor.setUserId(apUserId);
            articleFeign.save(apAuthor);
        }
    }

    /**
     * 审核用户状态校验
     * @param status
     * @return
     */
    private boolean statusCheck(Short status) {
        if(status == null || (!status.equals(UserConstants.FAIL_AUTH) && !status.equals(UserConstants.PASS_AUTH))){
            return true;
        }
        return false;
    }
}
