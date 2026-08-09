package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.messenger.RaccgramConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class RaccgramChatsSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int headerRow;
    private int showSecondsInTimeRow;
    private int hideForwardedFromRow;
    private int ghostModeRow;
    private int hideProxySponsorRow;
    private int sectionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        rowCount = 0;
        headerRow = rowCount++;
        showSecondsInTimeRow = rowCount++;
        hideForwardedFromRow = rowCount++;
        ghostModeRow = rowCount++;
        hideProxySponsorRow = rowCount++;
        sectionRow = rowCount++;
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Chats");
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
            if (position == showSecondsInTimeRow) {
                RaccgramConfig.getInstance().setShowSecondsInTimeEnabled(!RaccgramConfig.getInstance().showSecondsInTimeEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().showSecondsInTimeEnabled);
            } else if (position == hideForwardedFromRow) {
                RaccgramConfig.getInstance().setHideForwardedFromEnabled(!RaccgramConfig.getInstance().hideForwardedFromEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hideForwardedFromEnabled);
            } else if (position == ghostModeRow) {
                RaccgramConfig.getInstance().setGhostModeEnabled(!RaccgramConfig.getInstance().ghostModeEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().ghostModeEnabled);
            } else if (position == hideProxySponsorRow) {
                RaccgramConfig.getInstance().setHideProxySponsorEnabled(!RaccgramConfig.getInstance().hideProxySponsorEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hideProxySponsorEnabled);
            }
        });

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;
        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_CHECK = 1;
        private static final int VIEW_TYPE_SHADOW = 2;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == VIEW_TYPE_CHECK;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    break;
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case VIEW_TYPE_CHECK:
                default:
                    view = new TextCheckCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == headerRow) {
                ((HeaderCell) holder.itemView).setText("Chats");
            } else if (position == showSecondsInTimeRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Show seconds in message time", RaccgramConfig.getInstance().showSecondsInTimeEnabled, true);
                cell.setIcon(R.drawable.msg_stories_timer);
            } else if (position == hideForwardedFromRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide \"Forwarded from\"", RaccgramConfig.getInstance().hideForwardedFromEnabled, true);
                cell.setIcon(R.drawable.msg_forward);
            } else if (position == ghostModeRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Show deleted messages", RaccgramConfig.getInstance().ghostModeEnabled, true);
                cell.setIcon(R.drawable.msg_delete);
            } else if (position == hideProxySponsorRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide proxy sponsor channel", RaccgramConfig.getInstance().hideProxySponsorEnabled, false);
                cell.setIcon(R.drawable.msg_channel);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == sectionRow) {
                return VIEW_TYPE_SHADOW;
            } else {
                return VIEW_TYPE_CHECK;
            }
        }
    }
}
