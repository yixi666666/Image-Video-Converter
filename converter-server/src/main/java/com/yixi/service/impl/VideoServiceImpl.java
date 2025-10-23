package com.yixi.service.impl;

import com.yixi.braille_converter.BrailleConverter;
import com.yixi.dto.BrailleConvertConfigDTO;
import com.yixi.dto.VidFrameAndConfigDTO;
import com.yixi.img_processor.ImageProcessor;
import com.yixi.service.VideoService;
import com.yixi.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    public String vidFrameToBraille(VidFrameAndConfigDTO vidFrameAndConfig) throws IOException {
        String vidFrameBase64 = vidFrameAndConfig.getImage().getImageBase64();
        BrailleConvertConfigDTO config = vidFrameAndConfig.getConfig();

        BufferedImage originalImage = ImageUtils.base64ToBufferedImage(vidFrameBase64);
        BufferedImage processedImage = ImageProcessor.loadImage(originalImage,config.getOutputWidth());
        if(config.getIsDithering()){
            processedImage = ImageProcessor.floyd_SteinbergDithering(processedImage);
        }
        String asciiArt = BrailleConverter.convertToAscii(
                processedImage, config.getIsMonospaced(), config.getIsInverted());
        return asciiArt;
    }

}
