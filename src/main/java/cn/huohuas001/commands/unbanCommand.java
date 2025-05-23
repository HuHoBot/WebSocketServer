package cn.huohuas001.commands;

import cn.huohuas001.tools.BanManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class unbanCommand extends baseCommand {
    @Override
    public boolean run(List<String> args) {
        if (args.isEmpty()) {
            log.info("用法: unban <serverId>");
            return false;
        }

        String serverId = args.get(0);

        if (BanManager.unbanServer(serverId)) {
            log.info("成功解封服务器: {}", serverId);
            return true;
        } else {
            log.error("解封失败，服务器可能未被封禁");
            return false;
        }

    }
}
