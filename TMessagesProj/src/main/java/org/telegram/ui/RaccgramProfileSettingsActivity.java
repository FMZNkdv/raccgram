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

public class RaccgramProfileSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int headerRow;
    private int showIdRow;
    private int fakeVerifiedRow;
    private int hidePhoneRow;
    private int localPremiumRow;
    private int sectionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        rowCount = 0;
        headerRow = rowCount++;
        showIdRow = rowCount++;
        fakeVerifiedRow = rowCount++;
        hidePhoneRow = rowCount++;
        localPremiumRow = rowCount++;
        sectionRow = rowCount++;
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Profile");
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
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().showIdEnabled);
            } else if (position == fakeVerifiedRow) {
                RaccgramConfig.getInstance().setFakeVerifiedEnabled(!RaccgramConfig.getInstance().fakeVerifiedEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().fakeVerifiedEnabled);
            } else if (position == hidePhoneRow) {
                RaccgramConfig.getInstance().setHidePhoneEnabled(!RaccgramConfig.getInstance().hidePhoneEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().hidePhoneEnabled);
            } else if (position == localPremiumRow) {
                RaccgramConfig.getInstance().setLocalPremiumEnabled(!RaccgramConfig.getInstance().localPremiumEnabled);
                if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(RaccgramConfig.getInstance().localPremiumEnabled);
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
                ((HeaderCell) holder.itemView).setText("Profile & Identity");
            } else if (position == showIdRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Show ID in profile", RaccgramConfig.getInstance().showIdEnabled, true);
                cell.setIcon(R.drawable.msg_info);
            } else if (position == fakeVerifiedRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Fake verified badge", RaccgramConfig.getInstance().fakeVerifiedEnabled, true);
                cell.setIcon(R.drawable.verified_profile);
            } else if (position == hidePhoneRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Hide phone number", RaccgramConfig.getInstance().hidePhoneEnabled, true);
                cell.setIcon(R.drawable.msg_calls);
            } else if (position == localPremiumRow) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                cell.setTextAndCheck("Local Premium", RaccgramConfig.getInstance().localPremiumEnabled, false);
                cell.setIcon(R.drawable.msg_settings_premium);
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
