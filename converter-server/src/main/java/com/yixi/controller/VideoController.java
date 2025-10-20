package com.yixi.controller;

import com.yixi.dto.VidFrameAndConfigDTO;
import com.yixi.handler.VideoCharWebSocketHandler;
import com.yixi.result.Result;
import com.yixi.service.VideoService;
import com.yixi.vo.VidFrameToStrVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private VideoCharWebSocketHandler wsHandler;

    // 存储字符帧
    private final List<String> charFrames = new ArrayList<>();

    //@PostMapping(value = "toBraille", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> videoToBraille (@Valid @RequestBody VidFrameAndConfigDTO request) throws IOException{
        return null;
    }

    /**
     * 视频帧转盲文（定向推送）
     */
    @PostMapping("/convertFrame/{clientId}")
    public Result<VidFrameToStrVO> convertVidFrame(
            @PathVariable String clientId,
            @Valid @RequestBody VidFrameAndConfigDTO request) throws IOException {

        String brailleStr = videoService.vidFrameToBraille(request);

        // ✅ 定向推送给当前客户端
        wsHandler.sendFrameTo(clientId, brailleStr);

        // 也返回一份结果（可选）
        return Result.success(new VidFrameToStrVO(brailleStr));
    }


}

