package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;

public class handleResponeMsg extends BaseEvent{

    public handleResponeMsg(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run(){
        if(client == null){
            return false;
        }
        if (!packId.isEmpty()) {
            String msg = body.getString("msg");
            BotClient botClient = clientManager.getBotClient();
            botClient.callBack(msg,packId);
        }
        return true;
    }
}
