package cn.huohuas001.events.Server;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.events.Bot.EventEnum.BotClientSendEvent;
import cn.huohuas001.events.Server.EventEnum.ServerSendEvent;
import cn.huohuas001.tools.ClientManager;

public class Server_handleHeart extends ServerEvent {

    public Server_handleHeart(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        if(client == null){
            BotClient botClient = clientManager.getBotClient();
            botClient.sendMessage(BotClientSendEvent.HEART,body,packId);
            botClient.updateLastHeartbeatTime();
            return true;
        }
        client.sendMessage(ServerSendEvent.heart,body,packId);
        client.updateLastHeartbeatTime();
        return true;
    }
}
