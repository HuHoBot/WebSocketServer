package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class handleBotQueryClientList extends BaseEvent{
    public handleBotQueryClientList(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        JSONArray serverIdList = body.getJSONArray("serverIdList");
        JSONArray clientList = new JSONArray();
        if(serverIdList == null){
            return false;
        }
        for(Object serverId : serverIdList){
            if(serverId instanceof String){
                String serverIdStr = (String) serverId;
                String clientName = clientManager.queryOnlineClient(serverIdStr);
                clientList.add(clientName);
            }
        }
        JSONObject response = new JSONObject();
        response.put("clientList",clientList);
        BotClient botClient = clientManager.getBotClient();
        return botClient.sendMessage(BotClientSendEvent.CALLBACK,response,packId);
    }
}
