package com.heima.model.media.vo;

import com.heima.model.media.pojos.WmNews;
import lombok.Data;

@Data
public class WmNewsVo  extends WmNews {
    /**
     * 作者名称
     */
    private String authorName;
}