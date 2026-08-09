package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.telegram.tgnet.TLRPC;

public class RaccgramConfig {

    private static final String PREFS_NAME = "raccgram";

    private static final String KEY_SHOW_ID = "show_id";
    private static final String KEY_FAKE_VERIFIED = "fake_verified";
    private static final String KEY_HIDE_PHONE = "hide_phone";
    private static final String KEY_HIDE_PROXY_SPONSOR = "hide_proxy_sponsor";
    private static final String KEY_HIDE_TYPING = "hide_typing_status";
    private static final String KEY_BYPASS_CONTENT_PROTECTION = "bypass_content_protection";
    private static final String KEY_HIDE_READ_RECEIPT = "hide_read_receipt";
    private static final String KEY_SHOW_SECONDS_IN_TIME = "show_seconds_in_time";
    private static final String KEY_INVISIBLE_STORY_VIEWING = "invisible_story_viewing";
    private static final String KEY_FREEZE_LAST_SEEN = "freeze_last_seen";
    private static final String KEY_LOCAL_PREMIUM = "local_premium";
    private static final String KEY_HIDE_FORWARDED_FROM = "hide_forwarded_from";
    private static final String KEY_GHOST_MODE = "ghost_mode";
    private static final String KEY_BADGE_USER_IDS = "badge_user_ids";
    private static final String KEY_KEYWORDS = "hidden_keywords";
    private static final String KEY_HIDDEN_USERS = "hidden_user_ids";

    private static volatile RaccgramConfig instance;

    public boolean showIdEnabled = true;
    public boolean fakeVerifiedEnabled = false;
    public boolean hidePhoneEnabled = false;
    public boolean hideProxySponsorEnabled = false;
    public boolean hideTypingStatusEnabled = false;
    public boolean bypassContentProtectionEnabled = false;
    public boolean hideReadReceiptEnabled = false;
    public boolean showSecondsInTimeEnabled = false;
    public boolean invisibleStoryViewingEnabled = false;
    public boolean freezeLastSeenEnabled = false;
    public boolean localPremiumEnabled = false;
    public boolean hideForwardedFromEnabled = false;
    public boolean ghostModeEnabled = false;

    private final Set<String> hiddenKeywords = new LinkedHashSet<>();
    private final Set<Long> hiddenUserIds = new LinkedHashSet<>();
    private final Set<Long> badgedUserIds = new LinkedHashSet<>();
    private final Set<Integer> ghostedMessageIds = java.util.Collections.synchronizedSet(new HashSet<>());

    public void addGhostedMessageId(int messageId) {
        ghostedMessageIds.add(messageId);
    }

    public boolean isGhostedMessage(int messageId) {
        return ghostedMessageIds.contains(messageId);
    }
    private volatile boolean badgeFetchedThisSession = false;

    private RaccgramConfig() {
        load();
        fetchBadgeListIfNeeded();
    }

    public static RaccgramConfig getInstance() {
        if (instance == null) {
            synchronized (RaccgramConfig.class) {
                if (instance == null) {
                    instance = new RaccgramConfig();
                }
            }
        }
        return instance;
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void load() {
        SharedPreferences p = prefs();
        showIdEnabled = p.getBoolean(KEY_SHOW_ID, true);
        fakeVerifiedEnabled = p.getBoolean(KEY_FAKE_VERIFIED, false);
        hidePhoneEnabled = p.getBoolean(KEY_HIDE_PHONE, false);
        hideProxySponsorEnabled = p.getBoolean(KEY_HIDE_PROXY_SPONSOR, false);
        hideTypingStatusEnabled = p.getBoolean(KEY_HIDE_TYPING, false);
        bypassContentProtectionEnabled = p.getBoolean(KEY_BYPASS_CONTENT_PROTECTION, false);
        hideReadReceiptEnabled = p.getBoolean(KEY_HIDE_READ_RECEIPT, false);
        showSecondsInTimeEnabled = p.getBoolean(KEY_SHOW_SECONDS_IN_TIME, false);
        invisibleStoryViewingEnabled = p.getBoolean(KEY_INVISIBLE_STORY_VIEWING, false);
        freezeLastSeenEnabled = p.getBoolean(KEY_FREEZE_LAST_SEEN, false);
        localPremiumEnabled = p.getBoolean(KEY_LOCAL_PREMIUM, false);
        hideForwardedFromEnabled = p.getBoolean(KEY_HIDE_FORWARDED_FROM, false);
        ghostModeEnabled = p.getBoolean(KEY_GHOST_MODE, false);

        hiddenKeywords.clear();
        Set<String> savedKeywords = p.getStringSet(KEY_KEYWORDS, null);
        if (savedKeywords != null) {
            hiddenKeywords.addAll(savedKeywords);
        }

        hiddenUserIds.clear();
        Set<String> savedUsers = p.getStringSet(KEY_HIDDEN_USERS, null);
        if (savedUsers != null) {
            for (String s : savedUsers) {
                try {
                    hiddenUserIds.add(Long.parseLong(s));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        badgedUserIds.clear();
        Set<String> savedBadges = p.getStringSet(KEY_BADGE_USER_IDS, null);
        if (savedBadges != null) {
            for (String s : savedBadges) {
                try {
                    badgedUserIds.add(Long.parseLong(s));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }


    public void setShowIdEnabled(boolean value) {
        showIdEnabled = value;
        prefs().edit().putBoolean(KEY_SHOW_ID, value).apply();
    }

    public void setFakeVerifiedEnabled(boolean value) {
        fakeVerifiedEnabled = value;
        prefs().edit().putBoolean(KEY_FAKE_VERIFIED, value).apply();
    }

    public void setHidePhoneEnabled(boolean value) {
        hidePhoneEnabled = value;
        prefs().edit().putBoolean(KEY_HIDE_PHONE, value).apply();
    }

    public void setHideProxySponsorEnabled(boolean value) {
        hideProxySponsorEnabled = value;
        prefs().edit().putBoolean(KEY_HIDE_PROXY_SPONSOR, value).apply();
    }

    public void setHideTypingStatusEnabled(boolean value) {
        hideTypingStatusEnabled = value;
        prefs().edit().putBoolean(KEY_HIDE_TYPING, value).apply();
    }

    public void setBypassContentProtectionEnabled(boolean value) {
        bypassContentProtectionEnabled = value;
        prefs().edit().putBoolean(KEY_BYPASS_CONTENT_PROTECTION, value).apply();
    }

    public void setHideReadReceiptEnabled(boolean value) {
        hideReadReceiptEnabled = value;
        prefs().edit().putBoolean(KEY_HIDE_READ_RECEIPT, value).apply();
    }

    public void setShowSecondsInTimeEnabled(boolean value) {
        showSecondsInTimeEnabled = value;
        prefs().edit().putBoolean(KEY_SHOW_SECONDS_IN_TIME, value).apply();
    }

    public void setInvisibleStoryViewingEnabled(boolean value) {
        invisibleStoryViewingEnabled = value;
        prefs().edit().putBoolean(KEY_INVISIBLE_STORY_VIEWING, value).apply();
    }

    public void setFreezeLastSeenEnabled(boolean value) {
        freezeLastSeenEnabled = value;
        prefs().edit().putBoolean(KEY_FREEZE_LAST_SEEN, value).apply();
    }

    public void setLocalPremiumEnabled(boolean value) {
        localPremiumEnabled = value;
        prefs().edit().putBoolean(KEY_LOCAL_PREMIUM, value).apply();
    }

    public void setHideForwardedFromEnabled(boolean value) {
        hideForwardedFromEnabled = value;
        prefs().edit().putBoolean(KEY_HIDE_FORWARDED_FROM, value).apply();
    }

    public void setGhostModeEnabled(boolean value) {
        ghostModeEnabled = value;
        prefs().edit().putBoolean(KEY_GHOST_MODE, value).apply();
    }


    public Set<String> getHiddenKeywords() {
        return Collections.unmodifiableSet(hiddenKeywords);
    }

    public void addHiddenKeyword(String keyword) {
        if (TextUtils.isEmpty(keyword)) return;
        hiddenKeywords.add(keyword.trim().toLowerCase(Locale.ROOT));
        persistKeywords();
    }

    public void removeHiddenKeyword(String keyword) {
        if (keyword == null) return;
        hiddenKeywords.remove(keyword.trim().toLowerCase(Locale.ROOT));
        persistKeywords();
    }

    private void persistKeywords() {
        prefs().edit().putStringSet(KEY_KEYWORDS, new HashSet<>(hiddenKeywords)).apply();
    }


    public Set<Long> getHiddenUserIds() {
        return Collections.unmodifiableSet(hiddenUserIds);
    }

    public void addHiddenUser(long userId) {
        hiddenUserIds.add(userId);
        persistUsers();
    }

    public void removeHiddenUser(long userId) {
        hiddenUserIds.remove(userId);
        persistUsers();
    }

    public boolean isUserHidden(long userId) {
        return hiddenUserIds.contains(userId);
    }

    private void persistUsers() {
        Set<String> asStrings = new HashSet<>();
        for (Long id : hiddenUserIds) {
            asStrings.add(String.valueOf(id));
        }
        prefs().edit().putStringSet(KEY_HIDDEN_USERS, asStrings).apply();
    }


    public ArrayList<TLRPC.Dialog> filterProxySponsor(ArrayList<TLRPC.Dialog> dialogs, MessagesController messagesController) {
        if (!hideProxySponsorEnabled || dialogs == null || dialogs.isEmpty() || messagesController == null) {
            return dialogs;
        }
        boolean hasSponsor = false;
        for (int i = 0; i < dialogs.size(); i++) {
            TLRPC.Dialog d = dialogs.get(i);
            if (d != null && messagesController.isPromoDialog(d.id, false) && messagesController.promoDialogType == MessagesController.PROMO_TYPE_PROXY) {
                hasSponsor = true;
                break;
            }
        }
        if (!hasSponsor) {
            return dialogs;
        }
        ArrayList<TLRPC.Dialog> filtered = new ArrayList<>(dialogs.size());
        for (int i = 0; i < dialogs.size(); i++) {
            TLRPC.Dialog d = dialogs.get(i);
            if (d != null && messagesController.isPromoDialog(d.id, false) && messagesController.promoDialogType == MessagesController.PROMO_TYPE_PROXY) {
                continue;
            }
            filtered.add(d);
        }
        return filtered;
    }


    public boolean isBadgeUser(long userId) {
        return badgedUserIds.contains(userId);
    }

    public void fetchBadgeListIfNeeded() {
        if (badgeFetchedThisSession) {
            return;
        }
        badgeFetchedThisSession = true;

        new Thread(() -> {
            try {
                URL url = new URL("http://78.154.103.37:12017/badges.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                } finally {
                    connection.disconnect();
                }

                JSONArray array = new JSONArray(response.toString());
                Set<Long> fetched = new LinkedHashSet<>();
                for (int i = 0; i < array.length(); i++) {
                    fetched.add(array.getLong(i));
                }

                synchronized (badgedUserIds) {
                    badgedUserIds.clear();
                    badgedUserIds.addAll(fetched);
                }
                Set<String> asStrings = new HashSet<>();
                for (Long id : fetched) {
                    asStrings.add(String.valueOf(id));
                }
                prefs().edit().putStringSet(KEY_BADGE_USER_IDS, asStrings).apply();
            } catch (Exception e) {
                Log.e("RaccgramConfig", "failed to fetch badge list", e);
            }
        }, "RaccgramBadgeFetch").start();
    }


    public boolean shouldHideMessage(MessageObject obj) {
        if (obj == null || obj.messageOwner == null) return false;
        if (obj.isOut() || obj.messageOwner.action != null) return false;

        long senderId = obj.getSenderId();
        if (hiddenUserIds.contains(senderId)) {
            return true;
        }

        if (!hiddenKeywords.isEmpty()) {
            String text = obj.messageOwner.message;
            if (!TextUtils.isEmpty(text)) {
                String lower = text.toLowerCase(Locale.ROOT);
                for (String kw : hiddenKeywords) {
                    if (!TextUtils.isEmpty(kw) && lower.contains(kw)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
