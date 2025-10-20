package com.yixi.service;

import com.yixi.dto.VidFrameAndConfigDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface VideoService {
    String vidFrameToBraille(VidFrameAndConfigDTO vidFrameAndConfigDTO)throws IOException;

}
