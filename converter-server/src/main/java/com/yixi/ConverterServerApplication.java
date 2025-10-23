package com.yixi;

import com.yixi.img_processor.VideoFrameProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ConverterServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ConverterServerApplication.class, args);

        // 只在JVM关闭时执行清理
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            VideoFrameProcessor processor = context.getBean(VideoFrameProcessor.class);
            processor.destroy();
        }));
    }
}
