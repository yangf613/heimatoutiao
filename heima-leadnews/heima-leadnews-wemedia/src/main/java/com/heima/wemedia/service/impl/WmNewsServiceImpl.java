package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.message.NewsAutoScanConstants;
import com.heima.common.constants.wemedia.WemediaContans;
import com.heima.common.exception.CustomException;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.media.dtos.WmNewsDto;
import com.heima.model.media.dtos.WmNewsPageReqDto;
import com.heima.model.media.pojos.WmMaterial;
import com.heima.model.media.pojos.WmNews;
import com.heima.model.media.pojos.WmNewsMaterial;
import com.heima.model.media.pojos.WmUser;
import com.heima.model.media.vo.WmNewsVo;
import com.heima.utils.threadlocal.WmThreadLocalUtils;
import com.heima.wemedia.maper.WmMaterialMapper;
import com.heima.wemedia.maper.WmNewsMapper;
import com.heima.wemedia.maper.WmNewsMaterialMapper;
import com.heima.wemedia.maper.WmUserMapper;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Value("${fdfs.url}")
    private String fileServerUrl;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        //参数校验
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数校验
        dto.checkParam();

        //分页条件查询
        IPage pageParam = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //状态精准查询
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        //频道精准查询
        if (dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        //时间范围查询
        if (dto.getBeginPubdate() != null && dto.getEndPubdate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubdate(), dto.getEndPubdate());
        }
        //关键字模糊查询
        if (dto.getKeyWord() != null) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyWord());
        }

        //查询当前登录用户
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        lambdaQueryWrapper.eq(WmNews::getUserId, user.getId());

        //按照创建时间倒序排列
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);

        IPage page = page(pageParam, lambdaQueryWrapper);

        //封装返回结果
        PageResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        result.setHost(fileServerUrl);

        return result;
    }

    /**
     * 自媒体文章保存
     *
     * @param dto
     * @param isSubmit 是否为提交 1 为提交 0为草稿
     * @return
     */
    @Override
    public ResponseResult saveNews(WmNewsDto dto, Short isSubmit) {
        //参数校验
        if (dto == null || StringUtils.isBlank(dto.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //保存或修改自媒体文章
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        if (WemediaContans.WM_NEWS_TYPE_AUTO.equals(dto.getType())) {
            wmNews.setType(null);
        }
        if (dto.getImages() != null && dto.getImages().size() > 0) {
            wmNews.setImages(dto.getImages().toString().replace("[", "")
                    .replace("]", "").replace(fileServerUrl, "")
                    .replace(" ", ""));
        }
        //保存或修改文章
        saveWmNews(wmNews, isSubmit);

        //维护文章和素材的关系
        String content = dto.getContent();
        List<Map> list = JSON.parseArray(content, Map.class);
        List<String> materials = ectractUrlInfo(list);

        //关联内容中图片与素材的关系
        if (isSubmit == WmNews.Status.SUBMIT.getCode() && materials.size() != 0) {
            ResponseResult responseResult = saveRelativeInfoForContent(materials, wmNews.getId());
            if (responseResult != null) {
                //return responseResult;
                throw new CustomException(responseResult.getErrorMessage());
            }
        }

        //关联封面中图片与素材的关系
        if (isSubmit == WmNews.Status.SUBMIT.getCode()) {
            ResponseResult responseResult = saveRelativeInfoForCover(dto, materials, wmNews);
            if (responseResult != null) {
                //return responseResult;
                throw new CustomException(responseResult.getErrorMessage());
            }
        }

        return null;
    }

    /**
     * 关联封面中图片与素材的关系
     *
     * @param dto
     * @param materials
     * @param wmNews
     * @return
     */
    private ResponseResult saveRelativeInfoForCover(WmNewsDto dto, List<String> materials, WmNews wmNews) {
        List<String> images = dto.getImages();
        //自动匹配封面
        if (dto.getType().equals(WemediaContans.WM_NEWS_TYPE_AUTO)) {
            //内容中图片数量小于等于2,设置为单图
            if (materials.size() > 0 && materials.size() <= 2) {
                wmNews.setType(WemediaContans.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else if (materials.size() > 2) {
                //如果内容中的图片大于2 则设置为多图
                wmNews.setType(WemediaContans.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else {
                //内容中没有图片，则设置为无图
                wmNews.setType(WemediaContans.WM_NEWS_NONE_IMAGE);
            }
            //修改文章信息
            if (images != null && images.size() > 0) {
                wmNews.setImages(images.toString().replace("[", "")
                        .replace("]", "").replace(fileServerUrl, "")
                        .replace(" ", ""));
            }
            updateById(wmNews);
        }
        //保存封面图片与素材的关系
        if (images != null && images.size() > 0) {
            ResponseResult responseResult = saveRelativeInfoForImage(images, wmNews.getId());
            if (responseResult != null) {
                return responseResult;
            }
        }
        return null;
    }

    /**
     * 保存封面与素材的关系
     *
     * @param images
     * @param newsId
     * @return
     */
    private ResponseResult saveRelativeInfoForImage(List<String> images, Integer newsId) {
        List<String> materials = new ArrayList<>();
        for (String image : images) {
            materials.add(image.replace(fileServerUrl, ""));
        }
        return saveRelativeInfo(materials, newsId, WemediaContans.WM_NEWS_COVER_REFERENCE);
    }

    /**
     * 关联文章内容中图片素材的关系
     *
     * @param materials
     * @param newsId
     * @return
     */
    private ResponseResult saveRelativeInfoForContent(List<String> materials, Integer newsId) {
        return saveRelativeInfo(materials, newsId, WemediaContans.WM_NEWS_CONTENT_REFERENCE);
    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    /**
     * 保存图片与素材关系
     *
     * @param materials
     * @param newsId
     * @param type
     * @return
     */
    private ResponseResult saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        //获取数据库中的素材信息
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(WmMaterial::getUrl, materials);
        lambdaQueryWrapper.eq(WmMaterial::getUserId, WmThreadLocalUtils.getUser().getId());
        List<WmMaterial> dbWmMaterials = wmMaterialMapper.selectList(lambdaQueryWrapper);
        //通过图片路径获取素材id
        List<String> materialsIds = new ArrayList<>();
        if (dbWmMaterials != null && dbWmMaterials.size() > 0) {
            Map<String, Integer> uriIdMap = dbWmMaterials.stream().collect(
                    Collectors.toMap(WmMaterial::getUrl, WmMaterial::getId));
            for (String material : materials) {
                String materialId = String.valueOf(uriIdMap.get(material));
                if ("null".equals(materialId)) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "应用图片失效");
                }
                materialsIds.add(materialId);
            }
        }
        //批量保存数据
        wmNewsMaterialMapper.saveRelations(materialsIds, newsId, type);
        return null;
    }

    /**
     * 提取图片信息
     *
     * @param list
     * @return
     */
    private List<String> ectractUrlInfo(List<Map> list) {
        List<String> materials = new ArrayList<>();
        for (Map map : list) {
            if (map.get("type").equals(WemediaContans.WM_NEWS_TYPE_IMAGE)) {
                String imgUrl = (String) map.get("value");
                String material = imgUrl.replace(fileServerUrl, "");
                materials.add(material);
            }
        }
        return materials;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 保存或修改文章
     *
     * @param wmNews
     * @param isSubmit
     */
    private void saveWmNews(WmNews wmNews, Short isSubmit) {
        wmNews.setStatus(isSubmit);
        wmNews.setUserId(WmThreadLocalUtils.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1);
        boolean flag = false;
        if (wmNews.getId() == null) {
            flag = save(wmNews);
        } else {
            //如果是修改，则先删除素材与文章的关系
            LambdaQueryWrapper<WmNewsMaterial> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(WmNewsMaterial::getNewsId, wmNews.getId());
            wmNewsMaterialMapper.delete(lambdaQueryWrapper);
            flag = updateById(wmNews);
        }
        //发送消息
        if(flag){
            kafkaTemplate.send(NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_TOPIC,JSON.toJSONString(wmNews.getId()));
        }
    }

    /**
     * 根据文章id查询文章
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult findWmNewsById(Integer id) {
        //参数校验
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "必须提供文章id");
        }

        //查询
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        //封装数据，返回
        ResponseResult responseResult = ResponseResult.okResult(wmNews);
        responseResult.setHost(fileServerUrl);
        return responseResult;
    }

    /**
     * 删除文章
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult delNews(Integer id) {
        //参数校验
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "必须提供文章id");
        }
        //获取文章对象
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        //判断文章是否上架 , status = 9 ， enable = 1
        if (wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode()) &&
                wmNews.getEnable().equals(WemediaContans.WM_NEWS_ENABLE_UP)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章已发布，不能删除");
        }
        //删除文章与素材的关联关系
        wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().
                eq(WmNewsMaterial::getNewsId, id));
        //删除文章
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        //参数校验
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "必须提供文章id");
        }
        //获取文章对象
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        //判断文章是否发布
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            //未发布
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章未发布，不能上下架");
        }
        //修改文章状态，同步到app端（后期做）TODO
        if (dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2) {
            update(Wrappers.<WmNews>lambdaUpdate().eq(WmNews::getId,dto.getId())
                    .set(WmNews::getEnable,dto.getEnable()));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public List<Integer> findRelease() {
        List<WmNews> list = list(Wrappers.<WmNews>lambdaQuery().eq(WmNews::getStatus, 8)
                .lt(WmNews::getPublishTime, new Date()));
        List<Integer> resultList = list.stream().map(WmNews::getId).collect(Collectors.toList());
        return resultList;
    }

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Override
    public PageResponseResult findListAndPage(NewsAuthDto dto) {

        //校验分页参数
        dto.checkParam();
        int currentPage = dto.getPage();

        //起始页
        dto.setPage((dto.getPage()-1)*dto.getSize());
        dto.setTitle("%"+dto.getTitle()+"%");

        //分页查询
        List<WmNewsVo> list = wmNewsMapper.findListAndPage(dto);
        //数据总条数
        int count = wmNewsMapper.findListCount(dto);
        //封装返回结果，返回
        PageResponseResult pageResponseResult = new PageResponseResult(currentPage,dto.getSize(),count);
        pageResponseResult.setData(list);
        return pageResponseResult;
    }

    @Autowired
    private WmUserMapper wmUserMapper;

    @Override
    public WmNewsVo findWmNewsVo(Integer id) {
        WmUser wmUser = null;
        //查询文章信息
        WmNews wmNews = getById(id);
        if(wmNews != null && wmNews.getUserId() != null){
             wmUser = wmUserMapper.selectById(wmNews.getUserId());
        }
        //封装返回结果
        WmNewsVo wmNewsVo = new WmNewsVo();
        BeanUtils.copyProperties(wmNews,wmNewsVo);
        if(wmNewsVo!=null){
            wmNewsVo.setAuthorName(wmUser.getName());
        }
        return wmNewsVo;
    }
}
