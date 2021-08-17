package com.heima.api.article;

import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PathVariable;

public interface AuthorControllerApi {

    /**
     * 保存作者
     * @param apAuthor
     * @return
     */
    ResponseResult save(ApAuthor apAuthor);

    /**
     * 根据用户id查询作者信息
     * @param id
     * @return
     */
    ApAuthor findByUserId(Integer id);

    /**
     * 根据名称查询作者
     * @param name
     * @return
     */
    public ApAuthor findByName(@PathVariable("id") String name);
}
