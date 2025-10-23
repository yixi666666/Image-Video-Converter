package com.yixi.img_processor;

import com.yixi.handler.VideoCharWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
public class VideoFrameProcessor {
    @Autowired
    private VideoCharWebSocketHandler wsHandler;

    private static volatile boolean initialized = false;

    // 超时时间（根据实际场景调整，单位：秒）
    private static final long FFMPEG_TIMEOUT = 30;

    private final BlockingQueue<String> charFramesQueue = new LinkedBlockingQueue<>();
    private final List<String> imagePaths = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger frameCounter = new AtomicInteger(0);

    private Thread processingThread;
    private volatile boolean isRunning = false;
    private volatile boolean isProcessingComplete = false;

    //处理视频来源的id
    private String clientId;

    // 添加延迟时间配置
    private static final long PROCESSING_DELAY_MS = 3000; // 3秒延迟

    // JavaCV组件
    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    @PostConstruct
    public void init() {
        // 防止重复初始化
        if (initialized) {
            log.warn("VideoFrameProcessor已经初始化，跳过重复初始化");
            return;
        }

        try {

            // 添加FFmpeg日志回调
            FFmpegLogCallback.set();

            isRunning = true;
            processingThread = new Thread(this::processFrames, "Frame-Processor-Thread");
            processingThread.setPriority(Thread.MIN_PRIORITY);
            processingThread.start();

            initialized = true;

        } catch (Exception e) {
            log.error("VideoFrameProcessor初始化失败", e);
            throw e;
        }
    }



    public void addCharFrame(String charFrame) {
        try {
            charFramesQueue.put(charFrame);
            log.info("当前队列大小：{}",charFramesQueue.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("添加字符帧被中断", e);
        }
    }

    public void markProcessingComplete() {
        log.info("标记处理完成，当前队列大小: {}", charFramesQueue.size());
        isProcessingComplete = true;
        processingThread.interrupt();
    }

    private void processFrames() {
        log.info("Frame-Processor-Thread 开始执行");
        int processedCount = 0;
        while (isRunning) {
            try {
                String charFrame = charFramesQueue.poll(PROCESSING_DELAY_MS*2, TimeUnit.MILLISECONDS);
                if (charFrame!=null&&!charFrame.isEmpty()) {
                    Thread.sleep(PROCESSING_DELAY_MS);
                    List<String> batch = new ArrayList<>();
                    batch.add(charFrame);
                    if(!isProcessingComplete){
                        charFramesQueue.drainTo(batch,5);
                    }else{
                        log.info("字符帧已经处理完成，生成其余全部字符帧图片");
                        charFramesQueue.drainTo(batch);
                    }
                    for (String frame : batch) {
                        try {
                            String imagePath = generateImage(frame);
                            synchronized (imagePaths) {
                                imagePaths.add(imagePath);
                            }
                            processedCount++;
                        } catch (Exception e) {
                            log.error("生成图片失败", e);
                        }
                    }
                    log.info("正在批量处理{}帧，目前共处理了{}帧，剩余队列大小: {}",
                            batch.size(), processedCount, charFramesQueue.size());
                    if(isProcessingComplete){
                        log.info("所有字符帧处理完成,通知前端");
                        wsHandler.sendProcessingCompleteTo(clientId);
                        clientId=null;
                    }
                } else if (isProcessingComplete && charFramesQueue.isEmpty()) {
                    synchronized (this) {
                        if (isProcessingComplete && charFramesQueue.isEmpty()) {
                            processedCount = 0;
                        }
                    }
                } else {
                    // 未完成且队列为空时，等待新帧
                    Thread.sleep(PROCESSING_DELAY_MS);
                }
            } catch (InterruptedException e) {
                if (!isProcessingComplete && charFramesQueue.isEmpty()) {
                    continue;
                }
            } catch (Exception e) {
                log.error("处理帧时发生错误", e);
            }
        }
    }



    public String combineImagesToVideo() {
        log.info("准备合成视频，共{}张图片", imagePaths.size());
        FFmpegFrameRecorder recorder = null;
        try {
            // 获取第一张图片的尺寸
            File firstImage = new File(imagePaths.get(0));
            BufferedImage firstImg = ImageIO.read(firstImage);
            int width = firstImg.getWidth();
            int height = firstImg.getHeight();

            // 创建视频录制器
            recorder = new FFmpegFrameRecorder(getTempVidPath()+"temp.mp4", width, height);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(15);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

            // 调整编码器参数
            recorder.setVideoOption("tune", "stillimage");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("crf", "23"); // 使用CRF替代质量参数
            recorder.setVideoBitrate(2000000);
            recorder.setGopSize(10);

            // 添加更多兼容性选项
            recorder.setVideoOption("movflags", "+faststart");
            recorder.setVideoOption("pix_fmt", "yuv420p");

            log.info("开始录制视频......................");
            recorder.start();

            // 读取并写入每一帧
            for (String imagePath : imagePaths) {
                BufferedImage image = ImageIO.read(new File(imagePath));
                Frame frame = converter.convert(image);
                recorder.record(frame);
            }

            log.info("视频合成成功");
            return getTempVidPath()+"temp.mp4";
        } catch (Exception e) {
            log.error("合成视频时发生错误", e);
            return null;
        } finally {
            if (recorder != null) {
                try {
                    recorder.stop();
                } catch (Exception e) {
                    log.error("停止录制器时发生错误", e);
                }
            }
            cleanupImages();
            reset();
        }
    }

    private void reset() {
        log.info("重置处理器状态");
        isProcessingComplete = false;
        clientId=null;
        synchronized (imagePaths) {
            imagePaths.clear();
        }
        frameCounter.set(0);
    }

    // 将字符帧生成图片
    private String generateImage(String charFrame) throws IOException {
        int fontScale = 1;
        Font font = new Font("Segoe UI Symbol", Font.PLAIN, 12 * fontScale);

        // 计算图像尺寸
        String[] lines = charFrame.split("\n");
        int rows = lines.length;
        int cols = lines[0].length();

        // 创建图像
        BufferedImage image = new BufferedImage(
                cols * 8 * fontScale,  // 每个盲文字符约占8像素宽度
                rows * 16 * fontScale, // 每个盲文字符约占16像素高度
                BufferedImage.TYPE_INT_RGB
        );

        // 绘制背景和文本
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setFont(font);
        g.setColor(Color.BLACK);

        // 绘制每一行
        for (int i = 0; i < rows; i++) {
            g.drawString(lines[i], 0, (i + 1) * 16 * fontScale - 4 * fontScale);
        }

        g.dispose();

        // 保存图片
        int frameNum = frameCounter.incrementAndGet();
        String imageName = "frame_" + frameNum + ".png";

        String imagePath = getFramePath() + imageName;
        ImageIO.write(image, "png", new File(imagePath));

        return imagePath;
    }


    /**
     * 合并音频和视频文件
     * @param audioFile 音频文件
     * @return 合并后的视频文件路径
     */
    public String mergeAudioVideo(MultipartFile audioFile) throws IOException {
        Process process = null; // 声明进程变量，便于finally中销毁
        File tempAudioFile = null;

        try {
            // 使用工具方法获取路径
            String videoPath = getTempVidPath() + "temp.mp4";
            String outputVideoPath = getOutputVidPath() + "output.mp4";

            // ===== 1. 输入校验（提前拦截无效资源） =====
            // 验证视频文件是否存在且可读
            File videoFile = new File(videoPath);
            if (!videoFile.exists() || !videoFile.canRead()) {
                throw new FileNotFoundException("视频文件不存在或不可读: " + videoPath);
            }
            // 验证音频文件是否有效
            if (audioFile == null || audioFile.isEmpty()) {
                throw new IllegalArgumentException("音频文件为空或未上传");
            }
            // 验证输出目录是否可写
            File outputFile = new File(outputVideoPath);
            File outputDir = outputFile.getParentFile();
            if (outputDir != null && (!outputDir.exists() && !outputDir.mkdirs())) {
                throw new IOException("输出目录创建失败: " + outputDir.getAbsolutePath());
            }

            // 保存音频文件为临时文件
            tempAudioFile = File.createTempFile("audio_", ".temp");
            try (OutputStream os = new FileOutputStream(tempAudioFile)) {
                os.write(audioFile.getBytes());
            }

            // 获取当前平台的ffmpeg可执行文件路径
            String ffmpegPath = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);

            // ===== 2. 构建FFmpeg命令（关键：添加音频格式指定，解决管道识别问题） =====
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath);
            command.add("-i");
            command.add(videoPath);      // 输入视频

            // 指定音频输入文件路径
            command.add("-i");
            command.add(tempAudioFile.getAbsolutePath()); // 使用临时文件作为音频输入

            command.add("-c:v");
            command.add("copy");         // 视频流直接复制
            command.add("-c:a");
            command.add("aac");          // 音频编码为AAC
            command.add("-strict");      // 兼容部分FFmpeg版本的AAC编码器
            command.add("experimental");
            command.add("-shortest");    // 以最短的流为准
            command.add("-y");           // 覆盖输出文件
            command.add(outputVideoPath); // 输出文件路径

            // ===== 3. 执行FFmpeg命令（优化资源处理和超时） =====
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 合并输出和错误流，便于日志排查
            process = pb.start();

            // 读取FFmpeg输出日志（异步读取避免缓冲区阻塞）
            StringBuilder ffmpegLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("FFmpeg: {}", line);
                    ffmpegLog.append(line).append("\n"); // 保存日志用于错误排查
                }
            }

            // 等待进程完成（设置超时，避免无限阻塞）
            boolean finished = process.waitFor(FFMPEG_TIMEOUT, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly(); // 超时强制终止
                throw new RuntimeException("FFmpeg处理超时（" + FFMPEG_TIMEOUT + "秒），已强制终止");
            }

            // 检查退出码和输出文件
            int exitCode = process.exitValue();
            log.info("FFmpeg进程结束，退出码: {}", exitCode);

            if (exitCode == 0) {
                // 验证输出文件有效性（避免空文件）
                if (outputFile.exists() && outputFile.length() > 0) {
                    log.info("视频合并成功，输出文件: {}", outputVideoPath);
                    return outputVideoPath;
                }
                throw new RuntimeException("输出文件生成失败（文件为空或不存在）");
            } else {
                throw new RuntimeException("视频合并失败，退出码: " + exitCode + "\nFFmpeg日志: " + ffmpegLog);
            }

        } catch (Exception e) {
            log.error("处理音频视频合并失败", e);
            throw new RuntimeException("合并失败: " + e.getMessage());
        } finally {
            // 确保进程销毁，避免僵尸进程
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            // 删除临时文件
            if (tempAudioFile != null && tempAudioFile.exists()) {
                tempAudioFile.delete();
            }
        }
    }


    // 辅助方法：根据音频文件推断格式（用于FFmpeg的-f参数）
    private String getAudioFormat(MultipartFile audioFile) {
        // 优先通过MIME类型判断
        String contentType = audioFile.getContentType();
        if (contentType != null) {
            switch (contentType) {
                case "audio/mpeg": return "mp3";
                case "audio/wav": case "audio/x-wav": return "wav";
                case "audio/aac": return "aac";
                case "audio/ogg": return "ogg";
                case "audio/flac": return "flac";
            }
        }

        // MIME类型无法判断时，通过文件名后缀判断
        String filename = audioFile.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            String suffix = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            switch (suffix) {
                case "mp3": return "mp3";
                case "wav": return "wav";
                case "aac": return "aac";
                case "ogg": return "ogg";
                case "flac": return "flac";
            }
        }

        // 无法识别格式时抛出异常（避免FFmpeg因格式未知失败）
        throw new IllegalArgumentException("无法识别音频格式，MIME: " + contentType + "，文件名: " + filename);
    }


    private void cleanupImages() {
        try {
            // 等待一段时间确保文件释放
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("文件清理等待被中断");
            }

            // 使用工具方法获取frames目录路径
            String framePath = getFramePath();

            Files.walk(Paths.get(framePath))
                    .filter(Files::isRegularFile)
                    .sorted((p1, p2) -> p2.getFileName().toString().compareTo(p1.getFileName().toString())) // 逆序删除
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("成功删除图片文件: {}", path);
                        } catch (IOException e) {
                            // 尝试强制删除
                            try {
                                path.toFile().deleteOnExit();
                                log.warn("标记文件延迟删除: {}", path);
                            } catch (Exception ex) {
                                log.error("删除图片文件失败: {}", path, e);
                            }
                        }
                    });
        } catch (IOException e) {
            log.error("清理图片文件时发生错误", e);
        }
    }


    @PreDestroy
    public void destroy() {
        if (!initialized) {
            return;
        }

        log.info("开始执行destroy()");
        isRunning = false;
        if (processingThread != null) {
            processingThread.interrupt();
            try {
                processingThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        cleanupImages();
        initialized = false;
        log.info("后台帧处理线程已停止");
    }

    // 获取resources/temp/frames/目录的绝对路径
    public static String getFramePath() {
        return getResourceDirPath("temp/frames/");
    }

    // 获取resources/temp目录的绝对路径
    public static String getTempVidPath() {
        return getResourceDirPath("temp/");
    }

    // 获取resources/output/目录的绝对路径
    public static String getOutputVidPath() {
        return getResourceDirPath("output/");
    }

    // 通用方法：获取resources下指定子目录的绝对路径（不存在则创建）
    private static String getResourceDirPath(String relativePath) {
        try {
            // 1. 获取项目根目录（假设运行时工作目录是 imgOrVideoToStr 根目录）
            String projectRoot = System.getProperty("user.dir");

            // 2. 拼接模块路径（converter-server）+ 源码资源目录（src/main/resources）
            String moduleResourcesPath = projectRoot
                    + File.separator + "converter-server"  // 模块名
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources";

            // 3. 拼接传入的相对路径（如"temp/frames/"）
            File dir = new File(moduleResourcesPath, relativePath);

            // 4. 确保目录存在（不存在则创建）
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 5. 返回实际文件夹路径
            return dir.getAbsolutePath() + File.separator;
        } catch (Exception e) {
            throw new RuntimeException("获取源码resources目录路径失败：" + relativePath, e);
        }
    }

    public String getClientId(){
        return clientId;
    }

    public void setClientId(String clientId){
        this.clientId = clientId;
    }

}



