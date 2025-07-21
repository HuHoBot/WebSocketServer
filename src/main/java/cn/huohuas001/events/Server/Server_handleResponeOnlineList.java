package cn.huohuas001.events.Server;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONObject;

public class Server_handleResponeOnlineList extends ServerEvent {

    public Server_handleResponeOnlineList(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run(){
        if(client == null){
            return false;
        }
        if (!packId.isEmpty()) {
            JSONObject msg = body.getJSONObject("list");
            BotClient botClient = clientManager.getBotClient();
            botClient.callBack(msg,packId);
        }
        return true;
    }
}
