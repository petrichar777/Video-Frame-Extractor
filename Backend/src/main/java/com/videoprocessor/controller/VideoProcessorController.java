package com.videoprocessor.controller;

import com.videoprocessor.model.FrameExtractionRequest;
import com.videoprocessor.model.FrameExtractionResponse;
import com.videoprocessor.model.VideoInfo;
import com.videoprocessor.service.VideoProcessorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频处理REST API控制器
 */
@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "*")
public class VideoProcessorController {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessorController.class);

    @Autowired
    private VideoProcessorService videoProcessorService;

    /**
     * 获取视频信息
     */
    @PostMapping("/info")
    public ResponseEntity<?> getVideoInfo(@RequestParam("file") MultipartFile videoFile) {
        try {
            logger.info("接收到获取视频信息请求: {}", videoFile.getOriginalFilename());
            
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("视频文件不能为空"));
            }
            
            VideoInfo videoInfo = videoProcessorService.getVideoInfo(videoFile);
            return ResponseEntity.ok(createSuccessResponse("获取视频信息成功", videoInfo));
            
        } catch (Exception e) {
            logger.error("获取视频信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取视频信息失败: " + e.getMessage()));
        }
    }

    /**
     * 提取视频帧（支持表单参数）
     */
    @PostMapping("/extract-frames")
    public ResponseEntity<?> extractFrames(
            @RequestParam("file") MultipartFile videoFile,
            @RequestParam(value = "intervalSeconds", required = false) Integer intervalSeconds,
            @RequestParam(value = "startTimeSeconds", defaultValue = "0") Integer startTimeSeconds,
            @RequestParam(value = "endTimeSeconds", required = false) Integer endTimeSeconds,
            @RequestParam(value = "outputFormat", defaultValue = "jpg") String outputFormat,
            @RequestParam(value = "imageQuality", defaultValue = "85") Integer imageQuality,
            @RequestParam(value = "returnBase64", defaultValue = "true") Boolean returnBase64) {
        
        try {
            logger.info("接收到提取视频帧请求: {}", videoFile.getOriginalFilename());
            
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("视频文件不能为空"));
            }
            
            // 构建请求对象
            FrameExtractionRequest request = new FrameExtractionRequest();
            request.setVideoFileName(videoFile.getOriginalFilename());
            request.setIntervalSeconds(intervalSeconds);
            request.setStartTimeSeconds(startTimeSeconds);
            request.setEndTimeSeconds(endTimeSeconds);
            request.setOutputFormat(outputFormat);
            request.setImageQuality(imageQuality);
            request.setReturnBase64(returnBase64);
            
            // 验证参数
            String validationError = validateRequest(request);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(createErrorResponse(validationError));
            }
            
            FrameExtractionResponse response = videoProcessorService.extractFrames(videoFile, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("提取视频帧失败: {}", e.getMessage(), e);
            FrameExtractionResponse errorResponse = new FrameExtractionResponse(false, "提取视频帧失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 提取视频帧（支持JSON请求体）
     */
    @PostMapping("/extract-frames-json")
    public ResponseEntity<?> extractFramesWithJson(
            @RequestParam("file") MultipartFile videoFile,
            @Valid @RequestPart("request") FrameExtractionRequest request) {
        
        try {
            logger.info("接收到JSON格式的提取视频帧请求: {}", videoFile.getOriginalFilename());
            
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("视频文件不能为空"));
            }
            
            request.setVideoFileName(videoFile.getOriginalFilename());
            
            FrameExtractionResponse response = videoProcessorService.extractFrames(videoFile, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("提取视频帧失败: {}", e.getMessage(), e);
            FrameExtractionResponse errorResponse = new FrameExtractionResponse(false, "提取视频帧失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Video Frame Extractor");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * 提取视频帧并只返回base64编码列表
     */
    @PostMapping("/extract-frames-base64-only")
    public ResponseEntity<?> extractFramesBase64Only(
            @RequestParam("file") MultipartFile videoFile,
            @RequestParam(value = "intervalSeconds", required = false) Integer intervalSeconds,
            @RequestParam(value = "startTimeSeconds", defaultValue = "0") Integer startTimeSeconds,
            @RequestParam(value = "endTimeSeconds", required = false) Integer endTimeSeconds,
            @RequestParam(value = "outputFormat", defaultValue = "jpg") String outputFormat,
            @RequestParam(value = "imageQuality", defaultValue = "85") Integer imageQuality) {
        
        try {
            logger.info("接收到提取视频帧Base64请求: {}", videoFile.getOriginalFilename());
            
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("视频文件不能为空"));
            }
            
            // 构建请求对象
            FrameExtractionRequest request = new FrameExtractionRequest();
            request.setVideoFileName(videoFile.getOriginalFilename());
            request.setIntervalSeconds(intervalSeconds);
            request.setStartTimeSeconds(startTimeSeconds);
            request.setEndTimeSeconds(endTimeSeconds);
            request.setOutputFormat(outputFormat);
            request.setImageQuality(imageQuality);
            request.setReturnBase64(true); // 强制返回base64
            
            // 验证参数
            String validationError = validateRequest(request);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(createErrorResponse(validationError));
            }
            
            FrameExtractionResponse response = videoProcessorService.extractFrames(videoFile, request);
            
            if (response.isSuccess()) {
                // 只返回base64编码列表
                java.util.List<String> base64List = response.getFrames().stream()
                    .map(frame -> frame.getBase64Data())
                    .collect(java.util.stream.Collectors.toList());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "帧提取成功");
                result.put("base64Frames", base64List);
                result.put("totalFramesExtracted", response.getTotalFramesExtracted());
                result.put("processingTimeMs", response.getProcessingTimeMs());
                result.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("提取视频帧Base64失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("提取视频帧失败: " + e.getMessage()));
        }
    }

    /**
     * 提取视频帧并生成特定JSON格式（每秒一帧）
     */
    @PostMapping("/extract-frames-json-format")
    public ResponseEntity<?> extractFramesJsonFormat(
            @RequestParam("file") MultipartFile videoFile,
            @RequestParam(value = "promptText", defaultValue = "请描述这些图片的内容") String promptText) {
        
        try {
            logger.info("接收到生成JSON格式请求: {}", videoFile.getOriginalFilename());
            
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("视频文件不能为空"));
            }
            
            // 构建请求对象 - 默认每秒一帧
            FrameExtractionRequest request = new FrameExtractionRequest();
            request.setVideoFileName(videoFile.getOriginalFilename());
            request.setIntervalSeconds(1); // 每秒一帧
            request.setStartTimeSeconds(0);
            request.setOutputFormat("jpg");
            request.setImageQuality(85);
            request.setReturnBase64(true);
            
            FrameExtractionResponse response = videoProcessorService.extractFrames(videoFile, request);
            
            if (response.isSuccess()) {
                // 构建特定格式的JSON
                java.util.List<Map<String, String>> contentList = new java.util.ArrayList<>();
                
                // 添加所有帧的base64数据
                for (FrameExtractionResponse.FrameData frame : response.getFrames()) {
                    Map<String, String> imageItem = new HashMap<>();
                    imageItem.put("type", "image");
                    // 直接使用原始Base64编码
                    imageItem.put("image", frame.getBase64Data());
                    contentList.add(imageItem);
                }
                
                // 添加文本提示词
                Map<String, String> textItem = new HashMap<>();
                textItem.put("type", "text");
                textItem.put("text", promptText);
                contentList.add(textItem);
                
                // 构建最终的JSON结构
                Map<String, Object> jsonContent = new HashMap<>();
                jsonContent.put("content", contentList);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "JSON格式生成成功");
                result.put("jsonFormat", jsonContent);
                result.put("totalFramesExtracted", response.getTotalFramesExtracted());
                result.put("processingTimeMs", response.getProcessingTimeMs());
                result.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("生成JSON格式失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("生成JSON格式失败: " + e.getMessage()));
        }
    }

    /**
     * 获取支持的视频格式
     */
    @GetMapping("/supported-formats")
    public ResponseEntity<?> getSupportedFormats() {
        Map<String, Object> formats = new HashMap<>();
        formats.put("videoFormats", new String[]{"mp4", "avi", "mov", "mkv", "wmv", "flv", "webm"});
        formats.put("imageFormats", new String[]{"jpg", "jpeg", "png", "bmp"});
        return ResponseEntity.ok(createSuccessResponse("获取支持格式成功", formats));
    }

    /**
     * 验证请求参数
     */
    private String validateRequest(FrameExtractionRequest request) {
        if (request.getIntervalSeconds() != null && request.getIntervalSeconds() <= 0) {
            return "提取间隔必须大于0秒";
        }
        
        if (request.getStartTimeSeconds() != null && request.getStartTimeSeconds() < 0) {
            return "开始时间不能为负数";
        }
        
        if (request.getEndTimeSeconds() != null && request.getStartTimeSeconds() != null && 
            request.getEndTimeSeconds() <= request.getStartTimeSeconds()) {
            return "结束时间必须大于开始时间";
        }
        
        if (request.getImageQuality() != null && 
            (request.getImageQuality() < 1 || request.getImageQuality() > 100)) {
            return "图片质量必须在1-100之间";
        }
        
        return null;
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}