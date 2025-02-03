package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.client.ServerClient;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONObject;

public class handleBotSendPack2Server extends BaseEvent{

    public handleBotSendPack2Server(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        String serverId = body.getString("serverId");
        String type = body.getString("type");
        JSONObject data = body.getJSONObject("data");
        ServerClient client = clientManager.getServerPackageById(serverId).getServerClient();
        BotClient botClient = clientManager.getBotClient();
        if(client != null){
            boolean sendRet = client.sendMessage(ServerSendEvent.valueOf(type),data,packId);
            JSONObject statusPack = new JSONObject();
            statusPack.put("status",sendRet);
            botClient.sendMessage(BotClientSendEvent.CALLBACK,statusPack,packId);
        }else{
            JSONObject statusPack = new JSONObject();
            statusPack.put("status",false);
            botClient.sendMessage(BotClientSendEvent.CALLBACK,statusPack,packId);
        }
        return true;
    }
}
