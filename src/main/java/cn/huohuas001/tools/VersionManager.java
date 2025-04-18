package cn.huohuas001.tools;

public class VersionManager {
    public static boolean isVersionAllowed(String clientVersion, String latestVersion) {
        if ("dev".equals(clientVersion)) {
            return true; // 开发版直接允许
        }

        String[] clientParts = clientVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        int maxLength = Math.max(clientParts.length, latestParts.length);
        for (int i = 0; i < maxLength; i++) {
            int client = i < clientParts.length ? Integer.parseInt(clientParts[i]) : 0;
            int latest = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

            if (client > latest) return true;  // 客户端版本更高
            if (client < latest) return false; // 客户端版本更低
        }
        return true; // 版本完全相同
    }
}
