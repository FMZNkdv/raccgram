package org.telegram.messenger;

import android.net.Uri;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ProxyLinkParser {

    public static class ParsedProxy {
        public String type;
        public String remark;
        public String server;
        public int port;
        public JSONObject outbound;
        public String rawLink;
    }

    public static boolean isSupportedLink(String link) {
        if (link == null) {
            return false;
        }
        String l = link.trim().toLowerCase();
        return l.startsWith("vless://") || l.startsWith("ss://") || l.startsWith("trojan://")
                || l.startsWith("vmess://") || l.startsWith("hysteria2://") || l.startsWith("hy2://")
                || l.startsWith("tuic://");
    }

    public static ParsedProxy parse(String link) throws Exception {
        link = link.trim();
        Uri uri = Uri.parse(link);
        String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "";
        switch (scheme) {
            case "vless":
                return parseVless(link, uri);
            case "trojan":
                return parseTrojan(link, uri);
            case "ss":
                return parseShadowsocks(link, uri);
            case "vmess":
                return parseVmess(link);
            case "hysteria2":
            case "hy2":
                return parseHysteria2(link, uri);
            case "tuic":
                return parseTuic(link, uri);
            default:
                throw new IllegalArgumentException("unsupported scheme " + scheme);
        }
    }

    private static Map<String, String> queryMap(Uri uri) {
        Map<String, String> map = new HashMap<>();
        for (String key : uri.getQueryParameterNames()) {
            map.put(key, uri.getQueryParameter(key));
        }
        return map;
    }

    private static JSONObject buildTlsObject(Map<String, String> q, String defaultSni) throws JSONException {
        String security = q.get("security");
        JSONObject tls = new JSONObject();
        if (security == null || security.equals("none")) {
            tls.put("enabled", false);
            return tls;
        }
        tls.put("enabled", true);
        String sni = q.containsKey("sni") ? q.get("sni") : defaultSni;
        if (sni != null) {
            tls.put("server_name", sni);
        }
        tls.put("insecure", "1".equals(q.get("allowInsecure")) || "1".equals(q.get("insecure")));
        String alpn = q.get("alpn");
        if (alpn != null) {
            JSONArray arr = new JSONArray();
            for (String a : alpn.split(",")) {
                arr.put(a);
            }
            tls.put("alpn", arr);
        }
        String fp = q.get("fp");
        if (fp != null && !fp.isEmpty()) {
            JSONObject utls = new JSONObject();
            utls.put("enabled", true);
            utls.put("fingerprint", fp);
            tls.put("utls", utls);
        }
        if ("reality".equals(security)) {
            JSONObject reality = new JSONObject();
            reality.put("enabled", true);
            reality.put("public_key", q.get("pbk"));
            if (q.containsKey("sid")) {
                reality.put("short_id", q.get("sid"));
            }
            tls.put("reality", reality);
        }
        return tls;
    }

    private static JSONObject buildTransportObject(Map<String, String> q) throws JSONException {
        String type = q.get("type");
        if (type == null) {
            type = "tcp";
        }
        JSONObject transport = new JSONObject();
        switch (type) {
            case "ws": {
                transport.put("type", "ws");
                transport.put("path", q.containsKey("path") ? decode(q.get("path")) : "/");
                if (q.containsKey("host")) {
                    JSONObject headers = new JSONObject();
                    headers.put("Host", q.get("host"));
                    transport.put("headers", headers);
                }
                return transport;
            }
            case "grpc": {
                transport.put("type", "grpc");
                transport.put("service_name", q.containsKey("serviceName") ? q.get("serviceName") : "");
                return transport;
            }
            case "http": {
                transport.put("type", "http");
                if (q.containsKey("path")) {
                    JSONArray arr = new JSONArray();
                    arr.put(decode(q.get("path")));
                    transport.put("path", arr);
                }
                if (q.containsKey("host")) {
                    JSONArray hosts = new JSONArray();
                    hosts.put(q.get("host"));
                    transport.put("host", hosts);
                }
                return transport;
            }
            default:
                return null;
        }
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    private static ParsedProxy parseVless(String link, Uri uri) throws JSONException {
        String uuid = uri.getUserInfo();
        String server = uri.getHost();
        int port = uri.getPort();
        Map<String, String> q = queryMap(uri);

        JSONObject outbound = new JSONObject();
        outbound.put("type", "vless");
        outbound.put("tag", "proxy-out");
        outbound.put("server", server);
        outbound.put("server_port", port);
        outbound.put("uuid", uuid);
        String flow = q.get("flow");
        if (flow != null && !flow.isEmpty()) {
            outbound.put("flow", flow);
        }
        outbound.put("packet_encoding", "xudp");
        outbound.put("tls", buildTlsObject(q, server));
        JSONObject transport = buildTransportObject(q);
        if (transport != null) {
            outbound.put("transport", transport);
        }

        ParsedProxy result = new ParsedProxy();
        result.type = "vless";
        result.remark = decode(uri.getFragment() != null ? uri.getFragment() : "VLESS");
        result.server = server;
        result.port = port;
        result.outbound = outbound;
        result.rawLink = link;
        return result;
    }

    private static ParsedProxy parseTrojan(String link, Uri uri) throws JSONException {
        String password = uri.getUserInfo();
        String server = uri.getHost();
        int port = uri.getPort();
        Map<String, String> q = queryMap(uri);

        JSONObject outbound = new JSONObject();
        outbound.put("type", "trojan");
        outbound.put("tag", "proxy-out");
        outbound.put("server", server);
        outbound.put("server_port", port);
        outbound.put("password", password);
        outbound.put("tls", buildTlsObject(q, server));
        JSONObject transport = buildTransportObject(q);
        if (transport != null) {
            outbound.put("transport", transport);
        }

        ParsedProxy result = new ParsedProxy();
        result.type = "trojan";
        result.remark = decode(uri.getFragment() != null ? uri.getFragment() : "Trojan");
        result.server = server;
        result.port = port;
        result.outbound = outbound;
        result.rawLink = link;
        return result;
    }

    private static ParsedProxy parseShadowsocks(String link, Uri uri) throws JSONException {
        String server;
        int port;
        String method;
        String password;
        String remark = uri.getFragment() != null ? decode(uri.getFragment()) : "Shadowsocks";

        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            String decoded = tryBase64Decode(userInfo);
            String[] parts = decoded.split(":", 2);
            method = parts[0];
            password = parts.length > 1 ? parts[1] : "";
            server = uri.getHost();
            port = uri.getPort();
        } else {
            String body = link.substring("ss://".length());
            int hashIdx = body.indexOf('#');
            if (hashIdx >= 0) {
                body = body.substring(0, hashIdx);
            }
            String decoded = tryBase64Decode(body);
            int atIdx = decoded.lastIndexOf('@');
            String creds = decoded.substring(0, atIdx);
            String hostPort = decoded.substring(atIdx + 1);
            String[] credParts = creds.split(":", 2);
            method = credParts[0];
            password = credParts.length > 1 ? credParts[1] : "";
            String[] hostPortParts = hostPort.split(":", 2);
            server = hostPortParts[0];
            port = Integer.parseInt(hostPortParts[1]);
        }

        JSONObject outbound = new JSONObject();
        outbound.put("type", "shadowsocks");
        outbound.put("tag", "proxy-out");
        outbound.put("server", server);
        outbound.put("server_port", port);
        outbound.put("method", method);
        outbound.put("password", password);

        ParsedProxy result = new ParsedProxy();
        result.type = "shadowsocks";
        result.remark = remark;
        result.server = server;
        result.port = port;
        result.outbound = outbound;
        result.rawLink = link;
        return result;
    }

    private static ParsedProxy parseVmess(String link) throws JSONException {
        String body = link.substring("vmess://".length());
        String decoded = tryBase64Decode(body);
        JSONObject cfg = new JSONObject(decoded);

        String server = cfg.optString("add");
        int port = cfg.optInt("port");

        JSONObject outbound = new JSONObject();
        outbound.put("type", "vmess");
        outbound.put("tag", "proxy-out");
        outbound.put("server", server);
        outbound.put("server_port", port);
        outbound.put("uuid", cfg.optString("id"));
        outbound.put("security", cfg.optString("scy", "auto"));
        outbound.put("alter_id", cfg.optInt("aid", 0));

        String net = cfg.optString("net", "tcp");
        if (net.equals("ws")) {
            JSONObject transport = new JSONObject();
            transport.put("type", "ws");
            transport.put("path", cfg.optString("path", "/"));
            String host = cfg.optString("host");
            if (!host.isEmpty()) {
                JSONObject headers = new JSONObject();
                headers.put("Host", host);
                transport.put("headers", headers);
            }
            outbound.put("transport", transport);
        } else if (net.equals("grpc")) {
            JSONObject transport = new JSONObject();
            transport.put("type", "grpc");
            transport.put("service_name", cfg.optString("path", ""));
            outbound.put("transport", transport);
        }

        String tlsFlag = cfg.optString("tls", "");
        JSONObject tls = new JSONObject();
        if (tlsFlag.equals("tls")) {
            tls.put("enabled", true);
            String sni = cfg.optString("sni", server);
            tls.put("server_name", sni);
        } else {
            tls.put("enabled", false);
        }
        outbound.put("tls", tls);

        ParsedProxy result = new ParsedProxy();
        result.type = "vmess";
        result.remark = cfg.optString("ps", "VMess");
        result.server = server;
        result.port = port;
        result.outbound = outbound;
        result.rawLink = link;
        return result;
    }

    private static ParsedProxy parseHysteria2(String link, Uri uri) throws JSONException {
        String password = uri.getUserInfo();
        String server = uri.getHost();
        int port = uri.getPort();
        Map<String, String> q = queryMap(uri);

        JSONObject outbound = new JSONObject();
        outbound.put("type", "hysteria2");
        outbound.put("tag", "proxy-out");
        outbound.put("server", server);
        outbound.put("server_port", port);
        outbound.put("password", password);
        if (q.containsKey("obfs")) {
            JSONObject obfs = new JSONObject();
            obfs.put("type", q.get("obfs"));
            obfs.put("password", q.get("obfs-password"));
            outbound.put("obfs", obfs);
        }
        outbound.put("tls", buildTlsObject(q, server));

        ParsedProxy result = new ParsedProxy();
        result.type = "hysteria2";
        result.remark = decode(uri.getFragment() != null ? uri.getFragment() : "Hysteria2");
        result.server = server;
        result.port = port;
        result.outbound = outbound;
        result.rawLink = link;
        return result;
    }

    private static ParsedProxy parseTuic(String link, Uri uri) throws JSONException {
        String userInfo = uri.getUserInfo();
        String[] creds = userInfo != null ? userInfo.split(":", 2) : new String[]{"", ""};
        String server = uri.getHost();
        int port = uri.getPort();
        Map<String, String> q = queryMap(uri);

        JSONObject outbound = new JSONObject();
        outbound.put("type", "tuic");
        outbound.put("tag", "proxy-out");
        outbound.put("server", server);
        outbound.put("server_port", port);
        outbound.put("uuid", creds[0]);
        outbound.put("password", creds.length > 1 ? creds[1] : "");
        outbound.put("congestion_control", q.containsKey("congestion_control") ? q.get("congestion_control") : "bbr");
        outbound.put("tls", buildTlsObject(q, server));

        ParsedProxy result = new ParsedProxy();
        result.type = "tuic";
        result.remark = decode(uri.getFragment() != null ? uri.getFragment() : "TUIC");
        result.server = server;
        result.port = port;
        result.outbound = outbound;
        result.rawLink = link;
        return result;
    }

    private static String tryBase64Decode(String s) {
        String normalized = s.replace('-', '+').replace('_', '/');
        int padding = normalized.length() % 4;
        if (padding != 0) {
            StringBuilder sb = new StringBuilder(normalized);
            for (int i = 0; i < 4 - padding; i++) {
                sb.append('=');
            }
            normalized = sb.toString();
        }
        try {
            byte[] bytes = Base64.decode(normalized, Base64.DEFAULT);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return s;
        }
    }
}
