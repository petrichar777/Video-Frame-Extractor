package com.videoprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 视频帧提取应用程序主类
 * 提供视频转换为图片帧并输出base64编码的功能
 */
@SpringBootApplication
public class VideoFrameExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoFrameExtractorApplication.class, args);
    }

}