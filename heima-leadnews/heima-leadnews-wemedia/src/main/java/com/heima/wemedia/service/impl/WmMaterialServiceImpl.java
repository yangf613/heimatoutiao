package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.media.dtos.WmMaterialDto;
import com.heima.model.media.pojos.WmNewsMaterial;
import com.heima.wemedia.maper.WmMaterialMapper;
import com.heima.common.fastdfs.FastDFSClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.pojos.WmMaterial;
import com.heima.model.media.pojos.WmUser;
import com.heima.utils.threadlocal.WmThreadLocalUtils;
import com.heima.wemedia.maper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FastDFSClient fastDFSClient;

    @Value("${fdfs.url}")
    private String fileServerUrl;

    @Autowired
    private WmNewsMaterialMapper wmMaterialMapper;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        // 参数校验
        if (multipartFile == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //上传图片
        String file = null;
        try {
            file = fastDFSClient.uploadFile(multipartFile);
        } catch (IOException e) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        //保存数据到数据库中
        WmUser wmUser = WmThreadLocalUtils.getUser();

        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUrl(file);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setUserId(wmUser.getId());
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);
        //拼接图片路径
        wmMaterial.setUrl(fileServerUrl + file);
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList(WmMaterialDto dto) {
        //参数校验
        dto.checkParam();
        //条件查询
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //获取当前登录用户
        WmUser user = WmThreadLocalUtils.getUser();
        lambdaQueryWrapper.eq(WmMaterial::getUserId, user.getId());
        //是否收藏
        if (dto.getIsCollection() != null && dto.getIsCollection().shortValue() == 1) {
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        //按照日期倒序排列
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);
        //分页
        IPage page = new Page(dto.getPage(), dto.getSize());
        IPage<WmMaterial> ipage = page(page, lambdaQueryWrapper);
        //返回数据
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) ipage.getTotal());
        List<WmMaterial> list = ipage.getRecords();

        //为每张图片加前缀
        list = list.stream().map(item -> {
            item.setUrl(fileServerUrl+item.getUrl());
            return item;
        }).collect(Collectors.toList());

        pageResponseResult.setData(list);
        return pageResponseResult;
    }

    @Override
    public ResponseResult delPicture(Integer id) {
        //参数校验
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //判断图片是否被引用
        WmMaterial wmMaterial = getById(id);
        if(wmMaterial == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        LambdaQueryWrapper<WmNewsMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WmNewsMaterial::getMaterialId,id);
        Integer count = wmMaterialMapper.selectCount(lambdaQueryWrapper);
        if(count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前图片被引用");
        }

        //删除fastDFS中的图片数据
        String fileId = wmMaterial.getUrl().replace(fileServerUrl, "");
        fastDFSClient.delFile(fileId);
        //删除数据库中的数据
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 收藏与取消收藏
     * @param id
     * @param type
     * @return
     */
    @Override
    public ResponseResult updateStatus(Integer id, Short type) {
        //校验参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //更新状态
        WmUser user = WmThreadLocalUtils.getUser();
        update(Wrappers.<WmMaterial>lambdaUpdate().set(WmMaterial::getIsCollection,type)
                .eq(WmMaterial::getId,id).eq(WmMaterial::getUserId,user.getId()));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
