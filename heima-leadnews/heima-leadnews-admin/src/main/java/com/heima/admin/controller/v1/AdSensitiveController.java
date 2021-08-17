package com.heima.admin.controller.v1;

import com.heima.admin.service.AdSensitiveService;
import com.heima.api.admin.AdSensitiveControllerApi;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
public class AdSensitiveController implements AdSensitiveControllerApi {

    @Autowired
    private AdSensitiveService adSensitiveService;

    @PostMapping("/list")
    @Override
    public ResponseResult listSensitive(@RequestBody SensitiveDto dto) {
        return adSensitiveService.listSensitive(dto);
    }

    @PostMapping("/save")
    @Override
    public ResponseResult saveSensitive(@RequestBody AdSensitive adSensitive) {
        return adSensitiveService.saveSensitive(adSensitive);
    }

    @PostMapping("/update")
    @Override
    public ResponseResult updateSensitive(@RequestBody AdSensitive adSensitive) {
        return adSensitiveService.updateSensitive(adSensitive);
    }

    @DeleteMapping("/del/{id}")
    @Override
    public ResponseResult deleteSensitive(@PathVariable("id") Integer id) {
        return adSensitiveService.deleteSensitive(id);
    }
}
