package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;

public class handleHeart extends BaseEvent{

    public handleHeart(ClientManager clientManager) {
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
