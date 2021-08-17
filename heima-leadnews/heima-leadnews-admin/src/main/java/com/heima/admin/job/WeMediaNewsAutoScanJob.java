package com.heima.admin.job;

import com.heima.admin.feign.WemediaFeign;
import com.heima.admin.service.WemediaNewsAutoScanService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class WeMediaNewsAutoScanJob {

    @Autowired
    private WemediaNewsAutoScanService wemediaNewsAutoScanService;

    @Autowired
    private WemediaFeign wemediaFeign;

    @XxlJob("wemediaAutoScanJob")
    public ReturnT<String> hello(String param) throws Exception{
        log.info("自媒体文章审核调度任务开始执行....");

        List<Integer> release = wemediaFeign.findRelease();
        if(release!=null && !release.isEmpty()){
            for (Integer id : release) {
                wemediaNewsAutoScanService.autoScanByMediaNewsId(id);
            }
        }

        log.info("自媒体文章审核调度任务执行结束....");
        return ReturnT.SUCCESS;

    }
}
