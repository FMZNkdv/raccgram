package org.telegram.messenger;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import io.nekohasekai.libbox.BridgeOptions;
import io.nekohasekai.libbox.BridgeSession;
import io.nekohasekai.libbox.CommandServer;
import io.nekohasekai.libbox.CommandServerHandler;
import io.nekohasekai.libbox.ConnectionOwner;
import io.nekohasekai.libbox.InterfaceUpdateListener;
import io.nekohasekai.libbox.Libbox;
import io.nekohasekai.libbox.LocalDNSTransport;
import io.nekohasekai.libbox.NeighborUpdateListener;
import io.nekohasekai.libbox.NetworkInterfaceIterator;
import io.nekohasekai.libbox.Notification;
import io.nekohasekai.libbox.OverrideOptions;
import io.nekohasekai.libbox.PlatformInterface;
import io.nekohasekai.libbox.PlatformUser;
import io.nekohasekai.libbox.SetupOptions;
import io.nekohasekai.libbox.ShellSession;
import io.nekohasekai.libbox.StringIterator;
import io.nekohasekai.libbox.SystemProxyStatus;
import io.nekohasekai.libbox.TunOptions;
import io.nekohasekai.libbox.WIFIState;

public class SingBoxProxyController {

    public static final int LOCAL_SOCKS_PORT = 12080;
    private static final SingBoxProxyController INSTANCE = new SingBoxProxyController();

    private CommandServer commandServer;
    private boolean setupDone;
    private String runningConfigKey;

    public static SingBoxProxyController getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isRunning() {
        return commandServer != null;
    }

    public synchronized void start(JSONObject outbound) throws Exception {
        String key = outbound.toString();
        if (commandServer != null && key.equals(runningConfigKey)) {
            return;
        }
        stop();
        ensureSetup();

        String configJson = buildFullConfig(outbound).toString();

        commandServer = new CommandServer(new BasicCommandServerHandler(), new BasicPlatformInterface());
        commandServer.start();
        commandServer.startOrReloadService(configJson, new OverrideOptions());
        runningConfigKey = key;
    }

    public synchronized void stop() {
        if (commandServer != null) {
            try {
                commandServer.closeService();
            } catch (Exception ignored) {
            }
            try {
                commandServer.close();
            } catch (Exception ignored) {
            }
            commandServer = null;
            runningConfigKey = null;
        }
    }

    private void ensureSetup() {
        if (setupDone) {
            return;
        }
        Context context = ApplicationLoader.applicationContext;

        File baseDir = new File(context.getFilesDir(), "singbox");
        baseDir.mkdirs();
        File workingDir = new File(baseDir, "working");
        workingDir.mkdirs();
        File tempDir = new File(context.getCacheDir(), "singbox_tmp");
        tempDir.mkdirs();

        SetupOptions options = new SetupOptions();
        options.setBasePath(baseDir.getAbsolutePath());
        options.setWorkingPath(workingDir.getAbsolutePath());
        options.setTempPath(tempDir.getAbsolutePath());
        options.setFixAndroidStack(true);

        try {
            Libbox.setup(options);
        } catch (Exception e) {
            FileLog.e(e);
        }
        setupDone = true;
    }

    private JSONObject buildFullConfig(JSONObject outbound) throws Exception {
        JSONObject config = new JSONObject();

        JSONObject log = new JSONObject();
        log.put("level", "error");
        log.put("disabled", false);
        config.put("log", log);

        JSONArray inbounds = new JSONArray();
        JSONObject socksIn = new JSONObject();
        socksIn.put("type", "socks");
        socksIn.put("tag", "socks-in");
        socksIn.put("listen", "127.0.0.1");
        socksIn.put("listen_port", LOCAL_SOCKS_PORT);
        inbounds.put(socksIn);
        config.put("inbounds", inbounds);

        JSONArray outbounds = new JSONArray();
        outbounds.put(outbound);
        JSONObject direct = new JSONObject();
        direct.put("type", "direct");
        direct.put("tag", "direct");
        outbounds.put(direct);
        config.put("outbounds", outbounds);

        JSONObject route = new JSONObject();
        route.put("final", "proxy-out");
        config.put("route", route);

        return config;
    }

    private static class BasicCommandServerHandler implements CommandServerHandler {
        @Override
        public int connectSSHAgent() throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public SystemProxyStatus getSystemProxyStatus() throws Exception {
            return new SystemProxyStatus();
        }

        @Override
        public void serviceReload() throws Exception {
        }

        @Override
        public void serviceStop() throws Exception {
        }

        @Override
        public void setSystemProxyEnabled(boolean enabled) throws Exception {
        }

        @Override
        public void triggerNativeCrash() throws Exception {
        }

        @Override
        public void writeDebugMessage(String message) {
        }
    }

    private static class BasicPlatformInterface implements PlatformInterface {
        @Override
        public void autoDetectInterfaceControl(int fd) throws Exception {
        }

        @Override
        public void checkPlatformShell() throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public void clearDNSCache() {
        }

        @Override
        public void closeDefaultInterfaceMonitor(InterfaceUpdateListener listener) throws Exception {
        }

        @Override
        public void closeNeighborMonitor(NeighborUpdateListener listener) throws Exception {
        }

        @Override
        public BridgeSession createBridge(BridgeOptions options) throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public ConnectionOwner findConnectionOwner(int ipProtocol, String sourceAddress, int sourcePort, String destinationAddress, int destinationPort) throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public NetworkInterfaceIterator getInterfaces() throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public boolean includeAllNetworks() {
            return false;
        }

        @Override
        public LocalDNSTransport localDNSTransport() {
            return null;
        }

        @Override
        public String lookupSFTPServer() throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public PlatformUser lookupUser(String username) throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public ShellSession openShellSession(PlatformUser user, String command, StringIterator args, String workDir, int cols, int rows) throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public int openTun(TunOptions options) throws Exception {
            throw new Exception("tun mode not supported");
        }

        @Override
        public String readSystemSSHHostKey() throws Exception {
            throw new Exception("not supported");
        }

        @Override
        public WIFIState readWIFIState() {
            return null;
        }

        @Override
        public void registerMyInterface(String name) {
        }

        @Override
        public void sendNotification(Notification notification) throws Exception {
        }

        @Override
        public void startDefaultInterfaceMonitor(InterfaceUpdateListener listener) throws Exception {
        }

        @Override
        public void startNeighborMonitor(NeighborUpdateListener listener) throws Exception {
        }

        @Override
        public String tailscaleHostname() {
            return "";
        }

        @Override
        public boolean underNetworkExtension() {
            return false;
        }

        @Override
        public boolean usePlatformAutoDetectInterfaceControl() {
            return false;
        }

        @Override
        public boolean usePlatformBridge() {
            return false;
        }

        @Override
        public boolean usePlatformShell() {
            return false;
        }

        @Override
        public boolean useProcFS() {
            return true;
        }
    }
}
