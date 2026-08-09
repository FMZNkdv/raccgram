package org.telegram.messenger;

import org.telegram.tgnet.ConnectionsManager;

public class ProxyConnectionHelper {

    public static void apply(boolean enabled, SharedConfig.ProxyInfo info) {
        if (!enabled || info == null) {
            ProxyTunnelService.stop(ApplicationLoader.applicationContext);
            ConnectionsManager.setProxySettings(enabled, info != null ? info.address : "", info != null ? info.port : 1080,
                    info != null ? info.username : "", info != null ? info.password : "", info != null ? info.secret : "");
            return;
        }

        if (info.proxyType == null || info.proxyType.equals("mtproto") || info.proxyType.equals("socks5")) {
            ProxyTunnelService.stop(ApplicationLoader.applicationContext);
            ConnectionsManager.setProxySettings(true, info.address, info.port, info.username, info.password, info.secret);
        } else {
            try {
                ProxyTunnelService.start(ApplicationLoader.applicationContext, info.getOutbound());
                ConnectionsManager.setProxySettings(true, "127.0.0.1", SingBoxProxyController.LOCAL_SOCKS_PORT, "", "", "");
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }
}
