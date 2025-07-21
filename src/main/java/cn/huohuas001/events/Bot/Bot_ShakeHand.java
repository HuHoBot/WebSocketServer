package cn.huohuas001.events.Bot;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.config.BotClientConfig;
import cn.huohuas001.events.Bot.EventEnum.BotClientSendEvent;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class Bot_ShakeHand extends BotEvent {
    private final BotClientConfig botClientConfig = new BotClientConfig();

    public Bot_ShakeHand(ClientManager clientManager) {
        super(clientManager);
    }

    private void botClientConnect(WebSocketSession session, String serverId, String hashKey) {
        JSONObject shakeHandPack = new JSONObject();
        BotClient botClient = new BotClient(session);
        botClient.setServerId(serverId);
        botClient.setHashKey(hashKey);
        clientManager.setBotClient(botClient);
        shakeHandPack.put("code", 1);
        shakeHandPack.put("msg", "");
        botClient.sendMessage(BotClientSendEvent.SHOOK_HAND, shakeHandPack);
        log.info("[Websocket] HuHoBot BotClient 已连接.");

        //重新清空WaitingList
        clientManager.reShakeWaitingServer();
    }

    @Override
    public boolean run() {
        return false;
    }

    public boolean runShake(WebSocketSession session) {
        String serverId = body.getString("serverId");
        String hashKey = body.getString("hashKey");

        String platform = body.getString("platform");
        String name = body.getString("name");
        String version = body.getString("version");

        JSONObject shakeHandPack = new JSONObject();
        BotClient _botClient = new BotClient(session);

        if (!serverId.equals("BotClient")) {
            _botClient.shutdown(1008, "");
            return false;
        }

        if (!hashKey.equals(botClientConfig.getKey())) {
            String msg = "密钥错误";
            shakeHandPack.put("code", 3);
            shakeHandPack.put("msg", msg);
            _botClient.sendMessage(BotClientSendEvent.SHOOK_HAND, shakeHandPack);
            _botClient.shutdown(1008, msg);
            return false;
        }

        if (session.getRemoteAddress() == null) {
            _botClient.shutdown(1008, "");
            return false;
        }

        String remoteIp = session.getRemoteAddress().getAddress().getHostAddress();

        if (!remoteIp.equals(botClientConfig.getAllowedIp())) {
            String msg = "IP 地址不在允许范围内";
            shakeHandPack.put("code", 7);
            shakeHandPack.put("msg", msg);
            _botClient.sendMessage(BotClientSendEvent.SHOOK_HAND, shakeHandPack);
            _botClient.shutdown(1008, msg);
            return false;
        }

        botClientConnect(session, serverId, hashKey);
        return true;
    }
}
