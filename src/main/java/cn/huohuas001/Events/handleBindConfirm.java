package cn.huohuas001.Events;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class handleBindConfirm extends BaseEvent{

    public handleBindConfirm(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        BotClient botClient = clientManager.getBotClient();
        botClient.sendRequestAndAwaitResponse(BotClientSendEvent.GET_CONFIRM_DATA, new JSONObject(),packId).thenAccept(response -> {
            JSONObject serverTempData = response.getJSONObject("serverTempData");
            String serverId = serverTempData.getString("serverId");
            String group_openid = serverTempData.getString("groupId");
            String author = serverTempData.getString("author");
            if(serverId == null){
                return;
            }
            try {
                JSONObject config = ClientManager.getServerConfig(serverId);
                if(client.sendMessage(ServerSendEvent.sendConfig,config,packId)){
                    JSONObject bindServerPack = new JSONObject();
                    bindServerPack.put("group",group_openid);
                    bindServerPack.put("serverConfig",config);
                    botClient.sendMessage(BotClientSendEvent.BIND_SERVER,bindServerPack,packId);
                    JSONObject addAdminPack = new JSONObject();
                    addAdminPack.put("group",group_openid);
                    addAdminPack.put("author",author);
                    botClient.sendMessage(BotClientSendEvent.ADD_ADMIN,addAdminPack,packId);
                    botClient.callBack("已向服务端下发配置文件，并添加您为机器人管理员，如有需要请使用`/管理帮助`来查看管理员命令帮助",packId);
                }else{
                    botClient.callBack("无法向Id为"+serverId+"的服务器下发配置文件，请管理员检查连接状态",packId);
                }
            } catch (Exception e) {
                log.error("[BindConfirm] 获取服务器配置时出现异常{}",e.getMessage());
            }
        });
        return false;
    }
}
