package com.yixi.controller;

import com.yixi.common.Result;
import com.yixi.pojo.DTO.ImageDTO;
import com.yixi.pojo.DTO.ImgAndConfigDTO;
import com.yixi.pojo.VO.ImageVO;
import com.yixi.pojo.VO.ImgToBrailleStrVO;
import com.yixi.service.ImageService;
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

    @PostMapping("/crop")
    public Result<ImageVO> imageCrop(@RequestBody ImageDTO request) throws IOException {
        //图片裁剪
        String processedImageBase64 = imageService.imageCropping(request.getImageBase64());

        return Result.success(ImageVO.builder()
                        .imageBase64(processedImageBase64)
                        .fileName(request.getFileName())
                        .fileType(request.getFileType())
                        .build());
    }

    @PostMapping("/toBraille")
    public Result<ImgToBrailleStrVO> imageToBraille (@Valid @RequestBody ImgAndConfigDTO request) throws IOException{
        String brailleStr = imageService.imageToBraille(request);
        return Result.success(new ImgToBrailleStrVO(brailleStr));
    }

}
