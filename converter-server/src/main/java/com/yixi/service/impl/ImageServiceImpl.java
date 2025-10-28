package com.yixi.service.impl;

import com.yixi.braille_converter.BrailleConverter;
import com.yixi.dto.BrailleConvertConfigDTO;
import com.yixi.dto.ImgAndConfigDTO;
import com.yixi.img_processor.ImageProcessor;
import com.yixi.service.ImageService;
import com.yixi.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    /**
     * 将Base64编码的图片裁剪，只保留左半部分
     * @param imageBase64
     * @return
     * @throws IOException
     */
    public String imageCropping(String imageBase64) throws IOException {
        // 1.获取图片
        BufferedImage originalImage = ImageUtils.base64ToBufferedImage(imageBase64);

        // 2. 计算裁剪区域
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int halfWidth = width / 2;

        // 3. 裁剪图像（只保留左半部分）
        BufferedImage croppedImage = originalImage.getSubimage(
                0,          // 起始x坐标
                0,          // 起始y坐标
                halfWidth,  // 裁剪宽度（原宽度的一半）
                height      // 裁剪高度（保持原高度）
        );

        // 4. 将裁剪后的图像转换回Base64
        return ImageUtils.bufferedImageToBase64(croppedImage);
    }

    /**
     * 将图片转换为盲文
     * @param imgAndConfig
     * @return
     * @throws IOException
     */
    public String imageToBraille(ImgAndConfigDTO imgAndConfig) throws IOException{
        String imageBase64 = imgAndConfig.getImage().getImageBase64();
        BrailleConvertConfigDTO config = imgAndConfig.getConfig();

        BufferedImage originalImage = ImageUtils.base64ToBufferedImage(imageBase64);
        BufferedImage processedImage = ImageProcessor.loadImage(originalImage,config.getOutputWidth());
        if(config.getIsDithering()){
            processedImage = ImageProcessor.floyd_SteinbergDithering(processedImage);
        }
        String asciiArt = BrailleConverter.convertToAscii(
                processedImage,config.getIsMonospaced(),config.getIsInverted());
        return asciiArt;
    }

}
