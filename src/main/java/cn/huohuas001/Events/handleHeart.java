package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.client.ServerClient;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONObject;

public class handleHeart extends BaseEvent{

    public handleHeart(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        if(client == null){
            BotClient botClient = clientManager.getBotClient();
            botClient.sendMessage(BotClientSendEvent.HEART,body,packId);
            return true;
        }
        client.sendMessage(ServerSendEvent.heart,body,packId);
        return true;
    }
}
