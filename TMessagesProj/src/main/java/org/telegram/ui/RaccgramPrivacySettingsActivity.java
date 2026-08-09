package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.MessagesController;
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

public class RaccgramPrivacySettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int headerRow;
    private int hideTypingStatusRow;
    private int hideReadReceiptRow;
    private int invisibleStoryViewingRow;
    private int freezeLastSeenRow;
    private int bypassContentProtectionRow;
    private int sectionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        rowCount = 0;
        headerRow = rowCount++;
        hideTypingStatusRow = rowCount++;
        hideReadReceiptRow = rowCount++;
        invisibleStoryViewingRow = rowCount++;
        freezeLastSeenRow = rowCount++;
        bypassContentProtectionRow = rowCount++;
        sectionRow = rowCount++;
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Privacy");
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
            if (position == hideTypingStatusRow) {
                RaccgramConfig.getInstance().setHideTypingStatusEnabled(!RaccgramConfig.getInstance().hideTypingStatusEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hideTypingStatusEnabled);
            } else if (position == hideReadReceiptRow) {
                RaccgramConfig.getInstance().setHideReadReceiptEnabled(!RaccgramConfig.getInstance().hideReadReceiptEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hideReadReceiptEnabled);
            } else if (position == invisibleStoryViewingRow) {
                RaccgramConfig.getInstance().setInvisibleStoryViewingEnabled(!RaccgramConfig.getInstance().invisibleStoryViewingEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().invisibleStoryViewingEnabled);
            } else if (position == freezeLastSeenRow) {
                RaccgramConfig.getInstance().setFreezeLastSeenEnabled(!RaccgramConfig.getInstance().freezeLastSeenEnabled);
                if (RaccgramConfig.getInstance().freezeLastSeenEnabled) {
                    MessagesController.getInstance(currentAccount).raccgramForceOffline();
                }
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().freezeLastSeenEnabled);
            } else if (position == bypassContentProtectionRow) {
                RaccgramConfig.getInstance().setBypassContentProtectionEnabled(!RaccgramConfig.getInstance().bypassContentProtectionEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().bypassContentProtectionEnabled);
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
                ((HeaderCell) holder.itemView).setText("Privacy");
            } else if (position == hideTypingStatusRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide typing status", RaccgramConfig.getInstance().hideTypingStatusEnabled, true);
                cell.setIcon(R.drawable.msg_edit);
            } else if (position == hideReadReceiptRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide read receipt", RaccgramConfig.getInstance().hideReadReceiptEnabled, true);
                cell.setIcon(R.drawable.msg_markunread);
            } else if (position == invisibleStoryViewingRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Invisible story viewing", RaccgramConfig.getInstance().invisibleStoryViewingEnabled, true);
                cell.setIcon(R.drawable.msg_stories_stealth2);
            } else if (position == freezeLastSeenRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Freeze last seen / online", RaccgramConfig.getInstance().freezeLastSeenEnabled, true);
                cell.setIcon(R.drawable.msg_mute);
            } else if (position == bypassContentProtectionRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Bypass content protection", RaccgramConfig.getInstance().bypassContentProtectionEnabled, false);
                cell.setIcon(R.drawable.ic_lock_header);
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
