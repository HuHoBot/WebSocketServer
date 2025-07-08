package cn.huohuas001.Events;

import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bot_handleQueryClientList extends BotEvent {
    public Bot_handleQueryClientList(ClientManager clientManager) {
        super(clientManager);
    }

    @Override
    public boolean run() {
        try {
            // 1. 参数校验
            JSONArray serverIdList = body.getJSONArray("serverIdList");
            if (serverIdList == null || serverIdList.isEmpty()) {
                log.warn("ServerId列表为空");
                return false;
            }

            // 2. 使用Java Stream API优化集合操作
            JSONArray clientList = serverIdList.stream()
                    .filter(serverId -> serverId instanceof String)
                    .map(serverId -> (String) serverId)
                    .map(serverIdStr -> {
                        String clientName = clientManager.queryOnlineClient(serverIdStr);
                        return clientName != null ? clientName : "Unknown Server"; // 处理可能的null返回值
                    })
                    .collect(JSONArray::new, JSONArray::add, JSONArray::addAll);

            // 3. 构造响应
            JSONObject response = new JSONObject();
            response.put("clientList", clientList);

            // 4. 发送响应并处理异常
            if (botClient != null) {
                return botClient.sendMessage(BotClientSendEvent.CALLBACK, response, packId);
            } else {
                log.error("BotClient未连接.");
                return false;
            }
        } catch (Exception e) {
            log.error("查询在线服务器时发现异常:", e);
            return false;
        }
    }
}
