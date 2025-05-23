package cn.huohuas001.commands;

import cn.huohuas001.tools.ClientManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class listCommand extends baseCommand {
    public boolean run(List<String> args) {
        ClientManager clientManager = ClientManager.getInstance();
        log.info("当前在线服务器数量: {},BotClient状态 :{}", clientManager.queryOnlineClientCount(), clientManager.getBotClient() == null ? "未连接" : "已连接");
        return true;
    }
}
