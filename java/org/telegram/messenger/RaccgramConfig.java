package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

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
    private static final String KEY_KEYWORDS = "hidden_keywords";
    private static final String KEY_HIDDEN_USERS = "hidden_user_ids";

    private static volatile RaccgramConfig instance;

    public boolean showIdEnabled = true;
    public boolean fakeVerifiedEnabled = false;
    public boolean hidePhoneEnabled = false;
    public boolean hideProxySponsorEnabled = false;

    private final Set<String> hiddenKeywords = new LinkedHashSet<>();
    private final Set<Long> hiddenUserIds = new LinkedHashSet<>();

    private RaccgramConfig() {
        load();
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