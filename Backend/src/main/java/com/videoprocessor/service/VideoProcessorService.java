package com.videoprocessor.service;

import com.videoprocessor.model.FrameExtractionRequest;
import com.videoprocessor.model.FrameExtractionResponse;
import com.videoprocessor.model.VideoInfo;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 视频处理服务类
 * 负责视频信息读取、帧提取和base64编码转换
 */
@Service
public class VideoProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessorService.class);

    @Value("${video.processor.temp-dir:${java.io.tmpdir}/video-processor}")
    private String tempDir;

    @Value("${video.processor.max-duration:3600}")
    private int maxDuration;

    @Value("${video.processor.supported-formats:mp4,avi,mov,mkv,wmv,flv,webm}")
    private String supportedFormats;

    @Value("${video.processor.output-format:jpg}")
    private String defaultOutputFormat;

    @Value("${video.processor.image-quality:85}")
    private int defaultImageQuality;

    private final Java2DFrameConverter frameConverter = new Java2DFrameConverter();

    /**
     * 获取视频信息
     */
    public VideoInfo getVideoInfo(MultipartFile videoFile) throws Exception {
        logger.info("开始获取视频信息: {}", videoFile.getOriginalFilename());
        
        // 保存临时文件
        File tempFile = saveTemporaryFile(videoFile);
        
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempFile)) {
            grabber.start();
            
            VideoInfo videoInfo = new VideoInfo(
                videoFile.getOriginalFilename(),
                grabber.getLengthInTime() / 1000, // 转换为毫秒
                grabber.getFrameRate(),
                grabber.getImageWidth(),
                grabber.getImageHeight(),
                videoFile.getSize(),
                getFileExtension(videoFile.getOriginalFilename())
            );
            
            logger.info("视频信息获取成功: {}", videoInfo);
            return videoInfo;
            
        } finally {
            // 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 提取视频帧并转换为base64
     */
    public FrameExtractionResponse extractFrames(MultipartFile videoFile, FrameExtractionRequest request) {
        long startTime = System.currentTimeMillis();
        FrameExtractionResponse response = new FrameExtractionResponse();
        
        try {
            logger.info("开始提取视频帧: {}, 请求参数: {}", videoFile.getOriginalFilename(), request);
            
            // 验证文件格式
            if (!isValidVideoFormat(videoFile.getOriginalFilename())) {
                response.setSuccess(false);
                response.setMessage("不支持的视频格式");
                return response;
            }
            
            // 保存临时文件
            File tempFile = saveTemporaryFile(videoFile);
            
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempFile)) {
                grabber.start();
                
                // 获取视频信息
                VideoInfo videoInfo = new VideoInfo(
                    videoFile.getOriginalFilename(),
                    grabber.getLengthInTime() / 1000,
                    grabber.getFrameRate(),
                    grabber.getImageWidth(),
                    grabber.getImageHeight(),
                    videoFile.getSize(),
                    getFileExtension(videoFile.getOriginalFilename())
                );
                response.setVideoInfo(videoInfo);
                
                // 提取帧
                List<FrameExtractionResponse.FrameData> frames = extractFramesFromVideo(
                    grabber, request, videoInfo);
                
                response.setFrames(frames);
                response.setTotalFramesExtracted(frames.size());
                response.setSuccess(true);
                response.setMessage("帧提取成功");
                
                logger.info("视频帧提取完成，共提取 {} 帧", frames.size());
                
            } finally {
                // 清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            
        } catch (Exception e) {
            logger.error("视频帧提取失败: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("视频处理失败: " + e.getMessage());
        }
        
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        return response;
    }

    /**
     * 从视频中提取帧
     */
    private List<FrameExtractionResponse.FrameData> extractFramesFromVideo(
            FFmpegFrameGrabber grabber, FrameExtractionRequest request, VideoInfo videoInfo) throws Exception {
        
        List<FrameExtractionResponse.FrameData> frames = new ArrayList<>();
        
        double frameRate = videoInfo.getFrameRate();
        long videoDurationMs = videoInfo.getDuration();
        
        // 计算提取参数
        int startTimeMs = (request.getStartTimeSeconds() != null ? request.getStartTimeSeconds() : 0) * 1000;
        int endTimeMs = request.getEndTimeSeconds() != null ? 
            request.getEndTimeSeconds() * 1000 : (int) videoDurationMs;
        
        // 如果指定了间隔秒数，按间隔提取
        if (request.getIntervalSeconds() != null) {
            extractFramesByInterval(grabber, frames, request, startTimeMs, endTimeMs, frameRate);
        } else {
            // 按原帧率提取所有帧
            extractAllFrames(grabber, frames, request, startTimeMs, endTimeMs, frameRate);
        }
        
        return frames;
    }

    /**
     * 按间隔提取帧
     */
    private void extractFramesByInterval(FFmpegFrameGrabber grabber, 
            List<FrameExtractionResponse.FrameData> frames,
            FrameExtractionRequest request, int startTimeMs, int endTimeMs, double frameRate) throws Exception {
        
        int intervalMs = request.getIntervalSeconds() * 1000;
        
        for (int currentTimeMs = startTimeMs; currentTimeMs < endTimeMs; currentTimeMs += intervalMs) {
            // 跳转到指定时间
            grabber.setTimestamp(currentTimeMs * 1000); // FFmpeg使用微秒
            
            Frame frame = grabber.grabImage();
            if (frame != null) {
                FrameExtractionResponse.FrameData frameData = convertFrameToData(
                    frame, currentTimeMs / 1000.0, frames.size() + 1, request);
                if (frameData != null) {
                    frames.add(frameData);
                }
            }
        }
    }

    /**
     * 提取所有帧
     */
    private void extractAllFrames(FFmpegFrameGrabber grabber,
            List<FrameExtractionResponse.FrameData> frames,
            FrameExtractionRequest request, int startTimeMs, int endTimeMs, double frameRate) throws Exception {
        
        // 跳转到开始时间
        if (startTimeMs > 0) {
            grabber.setTimestamp(startTimeMs * 1000);
        }
        
        Frame frame;
        int frameNumber = 1;
        
        while ((frame = grabber.grabImage()) != null) {
            long currentTimeMs = grabber.getTimestamp() / 1000; // 转换为毫秒
            
            if (currentTimeMs > endTimeMs) {
                break;
            }
            
            if (currentTimeMs >= startTimeMs) {
                FrameExtractionResponse.FrameData frameData = convertFrameToData(
                    frame, currentTimeMs / 1000.0, frameNumber++, request);
                if (frameData != null) {
                    frames.add(frameData);
                }
            }
        }
    }

    /**
     * 将Frame转换为FrameData
     */
    private FrameExtractionResponse.FrameData convertFrameToData(
            Frame frame, double timestampSeconds, int frameNumber, FrameExtractionRequest request) {
        
        try {
            BufferedImage bufferedImage = frameConverter.convert(frame);
            if (bufferedImage == null) {
                return null;
            }
            
            FrameExtractionResponse.FrameData frameData = new FrameExtractionResponse.FrameData(
                frameNumber, timestampSeconds, null, bufferedImage.getWidth(), bufferedImage.getHeight());
            
            // 如果需要返回base64编码
            if (request.getReturnBase64()) {
                String base64 = convertImageToBase64(bufferedImage, 
                    request.getOutputFormat(), request.getImageQuality());
                frameData.setBase64Data(base64);
            }
            
            return frameData;
            
        } catch (Exception e) {
            logger.error("转换帧数据失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将BufferedImage转换为base64编码
     */
    private String convertImageToBase64(BufferedImage image, String format, Integer quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        String outputFormat = format != null ? format : defaultOutputFormat;
        
        // 写入图片
        ImageIO.write(image, outputFormat, baos);
        
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 保存临时文件
     */
    private File saveTemporaryFile(MultipartFile videoFile) throws IOException {
        // 确保临时目录存在
        Path tempDirPath = Paths.get(tempDir);
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }
        
        // 创建临时文件
        String fileName = System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
        File tempFile = new File(tempDirPath.toFile(), fileName);
        
        // 保存文件
        videoFile.transferTo(tempFile);
        
        return tempFile;
    }

    /**
     * 验证视频格式
     */
    private boolean isValidVideoFormat(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        String[] formats = supportedFormats.split(",");
        
        for (String format : formats) {
            if (format.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}