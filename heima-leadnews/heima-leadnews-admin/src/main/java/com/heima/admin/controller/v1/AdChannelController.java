package com.heima.admin.controller.v1;

import com.heima.admin.service.AdChannelService;
import com.heima.api.admin.ChannelControllerApi;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/channel")
public class AdChannelController implements ChannelControllerApi {

    @Autowired
    private AdChannelService adChannelService;

    @PostMapping("/list")
    @Override
    public ResponseResult findByNameAndPage(@RequestBody ChannelDto dto) {
        return adChannelService.findByNameAndPage(dto);
    }

    /**
     * 新增频道
     * @param adChannel
     * @return
     */
    @PostMapping("/save")
    @Override
    public ResponseResult saveAdChannel(@RequestBody AdChannel adChannel) {
        return adChannelService.saveAdChannel(adChannel);
    }

    /**
     * 修改频道
     * @param adChannel
     * @return
     */
    @PostMapping("/update")
    @Override
    public ResponseResult updateAdChannel(@RequestBody AdChannel adChannel) {
        return adChannelService.updateAdChannel(adChannel);
    }

    /**
     * 删除频道
     * @param id
     * @return
     */
    @GetMapping("/del/{id}")
    @Override
    public ResponseResult deleteAdChannel(@PathVariable("id") Integer id) {
        return adChannelService.deleteAdChannel(id);
    }

    /**
     * 查询所有频道你
     * @return
     */
    @GetMapping("/channels")
    @Override
    public ResponseResult findAll() {
        List<AdChannel> list = adChannelService.list();
        return ResponseResult.okResult(list);
    }
}
