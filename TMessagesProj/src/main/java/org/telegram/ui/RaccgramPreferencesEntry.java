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
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class RaccgramPreferencesEntry extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int logoRow;
    private int headerRow;
    private int profileCategoryRow;
    private int privacyCategoryRow;
    private int chatsCategoryRow;
    private int filtersCategoryRow;
    private int sectionRow;

    private int rowCount;

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
        profileCategoryRow = rowCount++;
        privacyCategoryRow = rowCount++;
        chatsCategoryRow = rowCount++;
        filtersCategoryRow = rowCount++;
        sectionRow = rowCount++;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        return true;
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
            if (position == profileCategoryRow) {
                presentFragment(new RaccgramProfileSettingsActivity());
            } else if (position == privacyCategoryRow) {
                presentFragment(new RaccgramPrivacySettingsActivity());
            } else if (position == chatsCategoryRow) {
                presentFragment(new RaccgramChatsSettingsActivity());
            } else if (position == filtersCategoryRow) {
                presentFragment(new RaccgramFiltersSettingsActivity());
            }
        });

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private static final int VIEW_TYPE_LOGO = 0;
        private static final int VIEW_TYPE_HEADER = 1;
        private static final int VIEW_TYPE_SETTINGS = 2;
        private static final int VIEW_TYPE_SHADOW = 3;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == VIEW_TYPE_SETTINGS;
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
                ((HeaderCell) holder.itemView).setText("Categories");
            } else if (position == profileCategoryRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextAndValue("Profile & Identity", "4", true);
                cell.setIcon(R.drawable.msg_info);
            } else if (position == privacyCategoryRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextAndValue("Privacy", "5", true);
                cell.setIcon(R.drawable.ic_lock_header);
            } else if (position == chatsCategoryRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextAndValue("Chats", "4", true);
                cell.setIcon(R.drawable.msg_channel);
            } else if (position == filtersCategoryRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextAndValue("Message Filters", "2", false);
                cell.setIcon(R.drawable.msg_message);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == logoRow) {
                return VIEW_TYPE_LOGO;
            } else if (position == headerRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == sectionRow) {
                return VIEW_TYPE_SHADOW;
            } else {
                return VIEW_TYPE_SETTINGS;
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
