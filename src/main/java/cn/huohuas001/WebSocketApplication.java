package cn.huohuas001;

import cn.huohuas001.commands.*;
import cn.huohuas001.config.WebSocketConfig;
import cn.huohuas001.tools.BanManager;
import cn.huohuas001.tools.CommandHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
@Import({WebSocketConfig.class})
public class WebSocketApplication {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketApplication.class);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final Map<String, baseCommand> commandMap = new HashMap<>();
    public static void main(String[] args) {
        // 注册Ctrl+C钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("接收到关闭信号，正在清理资源...");
            running.set(false);
        }));
        BanManager.initializeDatabase();  // 初始化数据库

        registerCommand("ban", new banCommand());
        registerCommand("help", new helpCommand());
        registerCommand("exit", new exitCommand());
        registerCommand("unban", new unbanCommand());
        registerCommand("list", new listCommand());
        registerCommand("shutdown", new shutdownCommand());

        SpringApplication.run(WebSocketApplication.class,  args);
        new Thread(WebSocketApplication::startConsoleCommandLoop).start();
    }

    private static void registerCommand(String commandName, baseCommand command) {
        commandMap.put(commandName.toLowerCase(), command);
    }

    private static void startConsoleCommandLoop() {
        Scanner scanner = new Scanner(System.in);
        logger.info("控制台指令系统已启动，输入 'help' 查看可用指令");
        logger.info("按Ctrl+C可安全退出");

        try {
            while (running.get()) {
                System.out.print("> ");
                String command = scanner.nextLine().trim();

                if (command.isEmpty()) {
                    continue;
                }

                //切割命令参数（支持带引号的参数）
                List<String> params = CommandHelper.splitCommandParams(command);
                if (params.isEmpty()) {
                    continue;
                }
                String commandName = params.get(0);
                baseCommand cmdObj = commandMap.get(commandName.toLowerCase());

                if (cmdObj != null) {
                    cmdObj.run(params.subList(1, params.size()));
                } else {
                    logger.warn("未知指令: {}", command);
                }

            }
        } catch (NoSuchElementException ignored) {
        } catch (Exception e) {
            logger.error("控制台命令循环异常", e);
        } finally {
            scanner.close();
            logger.info("控制台命令循环已停止");
        }
    }
}
