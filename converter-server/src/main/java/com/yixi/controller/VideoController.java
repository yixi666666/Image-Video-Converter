package com.yixi.controller;

import com.yixi.dto.VidFrameAndConfigDTO;
import com.yixi.handler.VideoCharWebSocketHandler;
import com.yixi.img_processor.VideoFrameProcessor;
import com.yixi.result.Result;
import com.yixi.service.VideoService;
import com.yixi.vo.BrailleVidPathVO;
import com.yixi.vo.VidFrameToStrVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private VideoCharWebSocketHandler wsHandler;
    @Autowired
    private VideoFrameProcessor frameProcessor;

    @Value("${video.local-path:F:/Java-related Projects/Image-Video-Converter/converter-server/src/main/resources/temp/}")
    private String localVideoPath;

    @Value("${video.http-prefix:/api/videos/}")
    private String httpVideoPrefix;

    // server.address默认值设为localhost，server.port默认值设为8080
    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private String serverPort;

    // 其他接口代码不变...
    @PostMapping("/convertFrame/{clientId}")
    public Result<VidFrameToStrVO> convertVidFrame(
            @PathVariable String clientId,
            @Valid @RequestBody VidFrameAndConfigDTO request) {
        try {

            if(frameProcessor.getClientId()==null){
                frameProcessor.setClientId(clientId);
            }

            String brailleStr = videoService.vidFrameToBraille(request);
            frameProcessor.addCharFrame(brailleStr);
            wsHandler.sendFrameTo(clientId, brailleStr);
            return Result.success(new VidFrameToStrVO(brailleStr));
        } catch (Throwable t) {
            log.error("处理视频帧转换时发生异常", t);
            return Result.error("处理失败：" + t.getMessage());
        }
    }

    @PostMapping("/finish")
    public Result<Void> finishProcessing() {
        frameProcessor.markProcessingComplete();
        return Result.success();
    }

    @PostMapping("/brailleVid")
    public Result<BrailleVidPathVO> processBrailleVideo() {
        try {
            String localVideoPath = frameProcessor.combineImagesToVideo();
            if (localVideoPath == null) {
                return Result.error("合成失败");
            }
            log.info("服务器本地视频路径：{}", localVideoPath);

            File videoFile = new File(localVideoPath);
            String videoFileName = videoFile.getName();
            if (!videoFile.exists()) {
                log.error("视频文件不存在：{}", localVideoPath);
                return Result.error("视频文件不存在");
            }

            // 拼接HTTP URL
            String httpVideoUrl = String.format(
                    "http://%s:%s%s%s",
                    serverAddress,
                    serverPort,
                    httpVideoPrefix,
                    videoFileName
            );
            log.info("返回前端的HTTP视频URL：{}", httpVideoUrl);

            return Result.success(new BrailleVidPathVO(httpVideoUrl));
        } catch (Exception e) {
            log.error("处理音频文件失败", e);
            return Result.error("处理失败：" + e.getMessage());
        }
    }

    // 新增一个视频下载接口
    @GetMapping("/videos/{videoName}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable String videoName) {
        try {
            // 设置视频文件路径
            File videoFile = new File(localVideoPath + videoName);
            if (!videoFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            log.info("下载视频:{}",videoName);

            // 创建文件资源
            Resource resource = new FileSystemResource(videoFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)  // 设置文件为二进制流
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + videoFile.getName() + "\"")  // 触发下载
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}