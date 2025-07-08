package cn.huohuas001.Events;

import cn.huohuas001.client.ServerClient;
import cn.huohuas001.tools.ClientManager;
import cn.huohuas001.tools.ServerPackage;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bot_handleSendPack2Server extends BotEvent {

    public Bot_handleSendPack2Server(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        try {
            // 1. 参数提取与校验
            String serverId = body.getString("serverId");
            String type = body.getString("type");
            JSONObject data = body.getJSONObject("data");

            // 2. 获取客户端实例（避免重复调用）
            ServerPackage serverPackage = clientManager.getServerPackageById(serverId);
            ServerClient serverClient = serverPackage != null ? serverPackage.getServerClient() : null;

            // 3. 构造响应状态包
            JSONObject statusPack = new JSONObject();
            boolean status = false;

            if (serverClient != null) {
                // 4. 发送消息并捕获可能的状态异常
                try {
                    status = serverClient.sendMessage(ServerSendEvent.valueOf(type), data, packId);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid event type: {}", type, e);
                } catch (Exception e) {
                    log.error("Failed to send message to server", e);
                }
            }

            // 5. 统一发送回调（避免重复代码）
            statusPack.put("status", status);
            if (botClient != null) {
                botClient.sendMessage(BotClientSendEvent.CALLBACK, statusPack, packId);
            } else {
                log.warn("BotClient is null, cannot send callback");
            }

            return true;
        } catch (Exception e) {
            log.error("Error in Bot_handleSendPack2Server", e);
            return false;
        }
    }
}
