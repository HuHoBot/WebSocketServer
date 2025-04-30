package cn.huohuas001.tools;

public class VersionManager {
    public static boolean isVersionAllowed(String clientVersion, String latestVersion) {
        if ("dev".equals(clientVersion)) {
            return true;
        }

        String[] clientParts = clientVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        int maxLength = Math.max(clientParts.length, latestParts.length);
        for (int i = 0; i < maxLength; i++) {
            // 提取纯数字部分（处理类似"5-hotfix"的情况）
            String clientStr = i < clientParts.length ?
                    clientParts[i].replaceAll("[^0-9]", "") : "0";
            String latestStr = i < latestParts.length ?
                    latestParts[i].replaceAll("[^0-9]", "") : "0";

            // 处理空字符串的情况（当某段完全没有数字时）
            int client = clientStr.isEmpty() ? 0 : Integer.parseInt(clientStr);
            int latest = latestStr.isEmpty() ? 0 : Integer.parseInt(latestStr);

            if (client > latest) return true;
            if (client < latest) return false;
        }
        return true;
    }
}
