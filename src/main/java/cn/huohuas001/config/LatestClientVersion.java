package cn.huohuas001.config;

import com.alibaba.fastjson2.JSON;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LatestClientVersion {
    private static final String JSON_FILE_PATH = "latestVersion.json";

    public static String getVersion(String type) {
        try (FileReader reader = new FileReader(JSON_FILE_PATH)) {
            Map<String, Object> versionMap = JSON.parseObject(reader,  Map.class);
            if (versionMap.containsKey(type))  {
                return (String) versionMap.get(type);
            }
        } catch (IOException e) {
            log.error("无法读取版本文件: {}", e.getMessage());
        }
        return "0.0.0";
    }
}
