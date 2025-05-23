package cn.huohuas001.commands;

import cn.huohuas001.tools.ClientManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class shutdownCommand extends baseCommand {
    @Override
    public boolean run(List<String> args) {
        if (args.isEmpty()) {
            log.info("用法: shutdown <serverId>");
            return false;
        }

        String serverId = args.get(0);

        if (ClientManager.getInstance().shutDownClient(serverId)) {
            log.info("成功关闭服务器: {}", serverId);
        } else {
            log.error("关闭失败，无法查找到服务器.");
        }
        return true;
    }
}
