package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.RaccgramConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class RaccgramHiddenUsersActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int headerRow;
    private final ArrayList<Long> hiddenUsers = new ArrayList<>();
    private int firstUserRow;
    private int addRow;
    private int sectionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        reloadRows();
        return true;
    }

    private void reloadRows() {
        hiddenUsers.clear();
        hiddenUsers.addAll(RaccgramConfig.getInstance().getHiddenUserIds());

        rowCount = 0;
        headerRow = rowCount++;
        firstUserRow = rowCount;
        rowCount += hiddenUsers.size();
        addRow = rowCount++;
        sectionRow = rowCount++;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Hidden Users");
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
            if (position == addRow) {
                showAddDialog();
            } else if (position >= firstUserRow && position < firstUserRow + hiddenUsers.size()) {
                showDeleteMenu(view, hiddenUsers.get(position - firstUserRow));
            }
        });

        return fragmentView;
    }

    private void showDeleteMenu(View anchor, long userId) {
        if (getParentActivity() == null) return;
        ItemOptions.makeOptions(this, anchor)
                .add(R.drawable.msg_delete, LocaleController.getString(R.string.Delete), true, () -> {
                    RaccgramConfig.getInstance().removeHiddenUser(userId);
                    reloadRows();
                    listAdapter.notifyDataSetChanged();
                })
                .setScrimViewBackground(listView.getClipBackground(anchor))
                .show();
    }

    private void showAddDialog() {
        if (getParentActivity() == null) return;

        LinearLayout container = new LinearLayout(getParentActivity());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(8), dp(24), 0);

        EditTextBoldCursor editText = new EditTextBoldCursor(getParentActivity());
        editText.setTextSize(16);
        editText.setHintText("User ID");
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setBackground(Theme.createEditTextDrawable(getParentActivity(), true));
        editText.setPadding(dp(4), dp(4), dp(4), dp(4));
        container.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));

        AlertDialog dialog = new AlertDialog.Builder(getParentActivity())
                .setTitle("Add User ID")
                .setView(container)
                .setPositiveButton("Add", (d, w) -> {
                    String text = editText.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        RaccgramConfig.getInstance().addHiddenUser(Long.parseLong(text.trim()));
                        reloadRows();
                        listAdapter.notifyDataSetChanged();
                        Toast.makeText(getParentActivity(), "Added: " + text.trim(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .create();
        showDialog(dialog);
        editText.requestFocus();
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_ITEM = 1;
        private static final int VIEW_TYPE_ADD = 2;
        private static final int VIEW_TYPE_SHADOW = 3;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_ITEM || type == VIEW_TYPE_ADD;
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
                case VIEW_TYPE_ITEM:
                case VIEW_TYPE_ADD:
                default:
                    view = new TextSettingsCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == headerRow) {
                ((HeaderCell) holder.itemView).setText("Messages from these user IDs will be hidden");
            } else if (position == addRow) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
                cell.setText("Add User ID", false);
            } else if (position >= firstUserRow && position < firstUserRow + hiddenUsers.size()) {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                cell.setText(String.valueOf(hiddenUsers.get(position - firstUserRow)), true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == addRow) {
                return VIEW_TYPE_ADD;
            } else if (position == sectionRow) {
                return VIEW_TYPE_SHADOW;
            } else {
                return VIEW_TYPE_ITEM;
            }
        }
    }
}
