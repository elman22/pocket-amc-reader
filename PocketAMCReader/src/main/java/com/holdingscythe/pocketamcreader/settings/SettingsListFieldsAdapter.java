/**
 * Created by elman on 28.4.2017.
 * <p>
 * Derived from Magnus Woxblom
 * https://github.com/woxblom/DragListView
 */

package com.holdingscythe.pocketamcreader.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.holdingscythe.pocketamcreader.R;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

class SettingsListFieldsAdapter extends DragItemAdapter<SettingsListField, SettingsListFieldsAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    SettingsListFieldsAdapter(ArrayList<SettingsListField> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).getDisplayText();
        holder.mText.setText(text);
        holder.itemView.setTag(mItemList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).getId();
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.card_text);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), view.getContext().getText(R.string.card_click_hint), Toast
                    .LENGTH_SHORT).show();
        }

    }
}
