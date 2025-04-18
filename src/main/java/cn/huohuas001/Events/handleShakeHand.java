package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.client.ServerClient;
import cn.huohuas001.config.LatestClientVersion;
import cn.huohuas001.tools.ClientManager;
import cn.huohuas001.tools.VersionManager;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class handleShakeHand extends BaseEvent{
    public handleShakeHand(ClientManager clientManager) {
        super(clientManager);
    }

    public static void botClientAllowConnect(ServerClient serverClient) {
        ClientManager clientManager = ClientManager.getInstance();
        BotClient botClient = clientManager.getBotClient();

        JSONObject shakeHandPack = new JSONObject();
        JSONObject botQueryPack = new JSONObject();
        String serverId = serverClient.getServerId();
        botQueryPack.put("serverId", serverId);

        botClient.sendRequestAndAwaitResponse(BotClientSendEvent.QUERY_BIND_SERVER_BY_ID, botQueryPack).thenAccept(response -> {
            String responseHashKey = response.getString("hashKey");

            if (!serverClient.getHashKey().equals(responseHashKey)) {
                String msg = "客户端密钥错误";
                shakeHandPack.put("code", 3);
                shakeHandPack.put("msg", msg);
                serverClient.sendMessage(ServerSendEvent.shaked, shakeHandPack);
                serverClient.shutdown(1008, msg);
                return;
            }
            if (clientManager.isRegisteredServer(serverId)) { //顶替连接
                ServerClient oriClient = clientManager.getServerPackageById(serverId).getServerClient();
                String msg = "serverId重复，已将本次连接顶替上一次连接...";
                shakeHandPack.put("code", 2);
                shakeHandPack.put("msg", msg);
                serverClient.sendMessage(ServerSendEvent.shaked, shakeHandPack);
                oriClient.shutdown(1008, "顶替连接.");
            } else {
                shakeHandPack.put("code", 1);
                shakeHandPack.put("msg", "");
                serverClient.sendMessage(ServerSendEvent.shaked, shakeHandPack);
            }

            clientManager.registerServer(serverId, serverClient);
            log.info("[Websocket]  服务器握手成功, ServerId: {}", serverId);
        });
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

    public boolean shakeHandRunner(WebSocketSession session) {
        String serverId = body.getString("serverId");
        String hashKey = body.getString("hashKey");

        String platform = body.getString("platform");
        String name = body.getString("name");
        String version = body.getString("version");

        ServerClient serverClient = new ServerClient(session);
        serverClient.setServerId(serverId);
        serverClient.setHashKey(hashKey);
        serverClient.setPlatform(platform);
        serverClient.setName(name);
        serverClient.setVersion(version);

        JSONObject shakeHandPack = new JSONObject();

        if(serverId == null || serverId.equals("")){
            //拒绝连接
            serverClient.shutdown(1008, "serverId为空.");
            return false;
        }

        if(serverId.equals("BotClient") && hashKey.equals("BotClient")){
            botClientConnect(session, serverId, hashKey);
            return true;
        }

        if(serverClient.getHashKey() == null || serverClient.getHashKey().equals("")){ //等待注册服务器
            clientManager.putAbsentServer(serverId, serverClient);
            String msg = "等待绑定";
            shakeHandPack.put("code",6);
            shakeHandPack.put("msg",msg);
            serverClient.sendMessage(ServerSendEvent.shaked,shakeHandPack);
            return false;
        }

        String latestVersion = LatestClientVersion.getVersion(serverClient.getPlatform());
        String ClientVersion = serverClient.getVersion();

        // 处理开发版提示
        if ("dev".equals(ClientVersion)) {
            String msg = "您正在使用的是开发版，如有问题请在GitHub仓库中提出Issues";
            shakeHandPack.put("code", 2);
            shakeHandPack.put("msg", msg);
            serverClient.sendMessage(ServerSendEvent.shaked, shakeHandPack);
        }
        // 版本检查逻辑
        else if (!VersionManager.isVersionAllowed(ClientVersion, latestVersion)) {
            String msg = "插件版本过低，最新版本为:" + latestVersion + "，您当前版本为:" + ClientVersion + "。请更新插件后重试。";
            shakeHandPack.put("code",4);
            shakeHandPack.put("msg",msg);
            serverClient.sendMessage(ServerSendEvent.shaked,shakeHandPack);
            serverClient.shutdown(1008,msg);
            return false;
        }


        BotClient botClient = clientManager.getBotClient();

        if (botClient == null || !botClient.isConnecting()) {
            String msg = "BotClient尚未连接，已将您的请求加入等待队列，正在等待BotClient连接. 长时间未连接请联系机器人管理员.";
            shakeHandPack.put("code",5);
            shakeHandPack.put("msg",msg);
            serverClient.sendMessage(ServerSendEvent.shaked,shakeHandPack);
            clientManager.putWaitingBotClientList(serverId, serverClient);
            return false;
        }

        botClientAllowConnect(serverClient);

        return true;
    }

    @Override
    public boolean run() {
        return true;
    }
}
