package cn.huohuas001.config;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class BotClientConfig {
    private static final String JSON_FILE_PATH = "config.json";
    private JSONObject config;

    public BotClientConfig() {
        reload();
    }

    public void reload() {
        try {
            byte[] bytes = Files.readAllBytes(new File(JSON_FILE_PATH).toPath());
            this.config = JSONObject.parseObject(new String(bytes));
        } catch (IOException e) {
            log.error("加载配置文件失败");
        }

    }

    public String getKey() {
        reload();
        return config.getString("key");
    }

    public String getAllowedIp() {
        reload();
        return config.getString("allowed-ip");
    }
}
