package cn.huohuas001.tools;

import cn.huohuas001.Events.Server_handleShakeHand;
import cn.huohuas001.client.BotClient;
import cn.huohuas001.client.ServerClient;
import com.alibaba.fastjson2.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class ClientManager {
    // 注册的服务器
    private static final Map<String, ServerClient> registeredServers = new ConcurrentHashMap<>();
    // 未注册的服务器
    private static final Map<String, ServerClient> absentRegisteredServers = new ConcurrentHashMap<>();
    //等待BotClient连接队列
    private static final Map<String, ServerClient> waitingBotClientList = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TIMEOUT = 10_000; // 15秒超时
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Setter
    private BotClient botClient = null;
    private static ClientManager Instance = null;
    private final Object sendLock = new Object();

    // 初始化时启动定时任务
    static {
        // 每隔5秒检查一次心跳
        scheduler.scheduleAtFixedRate(
                ClientManager::checkHeartbeats,
                0, 5, TimeUnit.SECONDS
        );
    }

    public ClientManager() {
        Instance = this;
    }

    public static ClientManager getInstance() {
        if (Instance == null) {
            Instance = new ClientManager();
        }
        return Instance;
    }

    public Object getSendLock() {
        return sendLock;
    }

    // 心跳检测逻辑
    private static void checkHeartbeats() {
        // 合并两个 Map 的检测逻辑
        checkHeartbeatsForMap(registeredServers);
        checkHeartbeatsForMap(absentRegisteredServers);
    }

    private static void checkHeartbeatsForMap(Map<String, ServerClient> clientMap) {
        clientMap.values().removeIf(client -> {
            if (client.isTimeout(HEARTBEAT_TIMEOUT)) {
                log.info("[ClientManager]  客户端({})心跳超时，自动断开, ServerId: {}", client.getRemoteAddress(), client.getServerId());
                client.close(1000, "Timeout.");
                return true; // 从 Map 中移除
            }
            return false;
        });
    }

    /**
     * 注册服务器
     * @param serverId 服务器id
     * @param serverClient 服务器对象
     * @return 是否注册成功
     */
    public boolean registerServer(String serverId, ServerClient serverClient){
        registeredServers.put(serverId, serverClient);
        return true;
    }

    /**
     * 注销服务器
     * @param serverId 服务器id
     * @return 是否注销成功
     */
    public boolean unRegisterServer(String serverId){
        if(registeredServers.containsKey(serverId)){
            registeredServers.remove(serverId);
            return true;
        }
        return false;
    }

    /**
     * 判断服务器是否已注册
     * @param serverId 服务器id
     * @return 是否注册
     */
    public boolean isRegisteredServer(String serverId){
        return registeredServers.containsKey(serverId);
    }

    public void putAbsentServer(String serverId, ServerClient serverClient) {
        absentRegisteredServers.put(serverId, serverClient);
    }

    public void putWaitingBotClientList(String serverId, ServerClient serverClient) {
        waitingBotClientList.put(serverId, serverClient);
    }

    public void removeWaitingBotClientList(String serverId) {
        waitingBotClientList.remove(serverId);
    }

    public void reShakeWaitingServer() {
        new HashMap<>(waitingBotClientList).forEach((serverId, serverClient) -> {
            Server_handleShakeHand.botClientAllowConnect(serverClient);
            removeWaitingBotClientList(serverId);
        });
    }


    public ServerPackage getServerPackageById(String serverId){
        if(registeredServers.containsKey(serverId)){
            return new ServerPackage(serverId, registeredServers.get(serverId));
        }
        if(absentRegisteredServers.containsKey(serverId)){
            return new ServerPackage(serverId, absentRegisteredServers.get(serverId));
        }
        return new ServerPackage("",null);
    }

    public ServerPackage getServerPackageBySession(WebSocketSession session){
        AtomicReference<ServerPackage> serverPackage = new AtomicReference<>();
        registeredServers.forEach((serverId,  serverInfo) -> {
            if (serverInfo.equals(session))  {
                serverPackage.set(new ServerPackage(serverId, serverInfo));
            }
        });
        if(serverPackage.get() == null){
            absentRegisteredServers.forEach((serverId,  serverInfo) -> {
                if (serverInfo.equals(session))  {
                    serverPackage.set(new ServerPackage(serverId, serverInfo));
                }
            });
        }
        if(serverPackage.get() == null){
            return new ServerPackage("",null);
        }
        return serverPackage.get();
    }

    public static String generateHashKey(String inputString, int saltLength, boolean useSalt) throws Exception {
        String salt = "";
        if (useSalt) {
            // 仅当需要加盐时生成随机盐值
            SecureRandom secureRandom = new SecureRandom();
            byte[] saltBytes = new byte[saltLength];
            secureRandom.nextBytes(saltBytes);
            salt = HexFormat.of().formatHex(saltBytes);
        }

        // 拼接输入字符串和盐值（当需要时）
        String combined = inputString + salt;

        // 计算SHA256哈希
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = messageDigest.digest(combined.getBytes());

        // 将哈希结果转换为十六进制字符串
        return HexFormat.of().formatHex(hashBytes);
    }

    @Deprecated
    public static String generateHashKey(String inputString, int saltLength) throws Exception {
        return generateHashKey(inputString, saltLength, true);
    }

    public static JSONObject getServerConfig(String serverId, boolean isMoreGroup) throws Exception {
        String HashKey = generateHashKey(serverId, 16, isMoreGroup);
        JSONObject config = new JSONObject();
        config.put("serverId",serverId);
        config.put("hashKey",HashKey);
        return config;
    }

    public String queryOnlineClient(String serverId) {
        ServerPackage serverPackage = getServerPackageById(serverId);
        if(serverPackage != null){
            if(serverPackage.getServerClient() != null){
                return serverPackage.getServerClient().getName();
            }
            return "";
        }
        return "";
    }

    public int queryOnlineClientCount() {
        return registeredServers.size();
    }

    public boolean shutDownClient(String serverId) {
        if (registeredServers.containsKey(serverId)) {
            ServerClient serverClient = registeredServers.get(serverId);
            serverClient.shutdown(1000, "Server shutdown.");
            return true;
        } else {
            return false;
        }
    }

    public BotClient getBotClient(){
        return this.botClient;
    }
}
