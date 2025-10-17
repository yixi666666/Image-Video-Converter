package com.yixi.service;

import com.yixi.pojo.DTO.ImgAndConfigDTO;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public interface ImageService {
    String imageCropping(String image) throws IOException;

    String imageToBraille(ImgAndConfigDTO imgAndConfigDTO) throws IOException;

}
