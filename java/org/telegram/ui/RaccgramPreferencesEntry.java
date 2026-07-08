/*
 * крч с дизайном чуть негронка помогала
 */

package org.telegram.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.RaccgramConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class RaccgramPreferencesEntry extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int logoRow;
    private int headerRow;
    private int showIdRow;
    private int fakeVerifiedRow;
    private int hidePhoneRow;
    private int hideProxySponsorRow;
    private int section1Row;

    private int headerFiltersRow;
    private int hiddenKeywordsRow;
    private int hiddenUsersRow;
    private int section2Row;

    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        return true;
    }

    private String getVersionName() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    private void updateRows() {
        rowCount = 0;
        logoRow = rowCount++;
        headerRow = rowCount++;
        showIdRow = rowCount++;
        fakeVerifiedRow = rowCount++;
        hidePhoneRow = rowCount++;
        hideProxySponsorRow = rowCount++;
        section1Row = rowCount++;

        headerFiltersRow = rowCount++;
        hiddenKeywordsRow = rowCount++;
        hiddenUsersRow = rowCount++;
        section2Row = rowCount++;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Raccgram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (position == showIdRow) {
                RaccgramConfig.getInstance().setShowIdEnabled(!RaccgramConfig.getInstance().showIdEnabled);
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().showIdEnabled);
                }
            } else if (position == fakeVerifiedRow) {
                RaccgramConfig.getInstance().setFakeVerifiedEnabled(!RaccgramConfig.getInstance().fakeVerifiedEnabled);
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().fakeVerifiedEnabled);
                }
            } else if (position == hidePhoneRow) {
                RaccgramConfig.getInstance().setHidePhoneEnabled(!RaccgramConfig.getInstance().hidePhoneEnabled);
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hidePhoneEnabled);
                }
            } else if (position == hideProxySponsorRow) {
                RaccgramConfig.getInstance().setHideProxySponsorEnabled(!RaccgramConfig.getInstance().hideProxySponsorEnabled);
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hideProxySponsorEnabled);
                }
            } else if (position == hiddenKeywordsRow) {
                presentFragment(new RaccgramHiddenKeywordsActivity());
            } else if (position == hiddenUsersRow) {
                presentFragment(new RaccgramHiddenUsersActivity());
            }
        });

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_CHECK = 1;
        private static final int VIEW_TYPE_SETTINGS = 2;
        private static final int VIEW_TYPE_SHADOW = 3;
        private static final int VIEW_TYPE_LOGO = 4;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_CHECK || type == VIEW_TYPE_SETTINGS;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_LOGO:
                    view = new LogoHeaderCell(mContext);
                    view.setTag(RecyclerListView.TAG_NOT_SECTION);
                    break;
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    break;
                case VIEW_TYPE_CHECK:
                    view = new TextCheckCell(mContext);
                    break;
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case VIEW_TYPE_SETTINGS:
                default:
                    view = new TextSettingsCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == logoRow) {
                ((LogoHeaderCell) holder.itemView).setVersion(getVersionName());
            } else if (position == headerRow) {
                ((HeaderCell) holder.itemView).setText("General");
            } else if (position == showIdRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Show ID in profile", RaccgramConfig.getInstance().showIdEnabled, true);
                cell.setIcon(R.drawable.msg_info);
            } else if (position == fakeVerifiedRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Fake verified badge", RaccgramConfig.getInstance().fakeVerifiedEnabled, true);
                cell.setIcon(R.drawable.verified_check);

            } else if (position == hidePhoneRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide phone number", RaccgramConfig.getInstance().hidePhoneEnabled, true);
                cell.setIcon(R.drawable.msg_calls);
            } else if (position == hideProxySponsorRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide proxy sponsor channel", RaccgramConfig.getInstance().hideProxySponsorEnabled, false);
                cell.setIcon(R.drawable.msg_channel);
            } else if (position == headerFiltersRow) {
                ((HeaderCell) holder.itemView).setText("Message Filters");
            } else if (position == hiddenKeywordsRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextAndValue("Hidden Keywords", String.valueOf(RaccgramConfig.getInstance().getHiddenKeywords().size()), true);
                cell.setIcon(R.drawable.msg_message);
            } else if (position == hiddenUsersRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextAndValue("Hidden Users", String.valueOf(RaccgramConfig.getInstance().getHiddenUserIds().size()), false);
                cell.setIcon(R.drawable.msg_block2);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == logoRow) {
                return VIEW_TYPE_LOGO;
            } else if (position == headerRow || position == headerFiltersRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == showIdRow || position == fakeVerifiedRow || position == hidePhoneRow || position == hideProxySponsorRow) {
                return VIEW_TYPE_CHECK;
            } else if (position == hiddenKeywordsRow || position == hiddenUsersRow) {
                return VIEW_TYPE_SETTINGS;
            } else {
                return VIEW_TYPE_SHADOW;
            }
        }
    }

    private static class LogoHeaderCell extends LinearLayout {

        private final ImageView logoView;
        private final TextView titleTextView;
        private final TextView versionTextView;

        public LogoHeaderCell(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER_HORIZONTAL);
            setPadding(0, AndroidUtilities.dp(24), 0, AndroidUtilities.dp(20));

            logoView = new ImageView(context);
            logoView.setImageResource(R.drawable.racclogo);
            logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(logoView, LayoutHelper.createLinear(96, 96, Gravity.CENTER_HORIZONTAL));

            titleTextView = new TextView(context);
            titleTextView.setText("Raccgram");
            titleTextView.setTextSize(20);
            titleTextView.setTypeface(AndroidUtilities.bold());
            titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(titleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));

            versionTextView = new TextView(context);
            versionTextView.setTextSize(13);
            versionTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(versionTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 4, 0, 0));

            updateColors();
        }

        private void updateColors() {
            logoView.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.SRC_IN);
            titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            versionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        }

        public void setVersion(String version) {
            versionTextView.setText(version);
            updateColors();
        }
    }
}