package com.yixi.controller;

import com.yixi.dto.ImageDTO;
import com.yixi.dto.ImgAndConfigDTO;
import com.yixi.result.Result;
import com.yixi.service.ImageService;
import com.yixi.vo.ImageVO;
import com.yixi.vo.ImgToBrailleStrVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @PostMapping("/toBraille")
    public Result<ImgToBrailleStrVO> imageToBraille (@Valid @RequestBody ImgAndConfigDTO request) throws IOException{
        String brailleStr = imageService.imageToBraille(request);
        return Result.success(new ImgToBrailleStrVO(brailleStr));
    }

}
