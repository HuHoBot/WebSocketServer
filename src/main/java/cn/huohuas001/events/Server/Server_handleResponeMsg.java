package cn.huohuas001.events.Server;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;

public class Server_handleResponeMsg extends ServerEvent {

    public Server_handleResponeMsg(ClientManager clientManager) {
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
