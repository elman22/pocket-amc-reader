/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2020 Elman <holdingscythe@zoznam.sk>
    Copyright © 2017 Magnus Woxblom

    Pocket AMC Reader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pocket AMC Reader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pocket AMC Reader.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.holdingscythe.pocketamcreader.settings;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;
import com.woxthebox.draglistview.BoardView;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

/**
 * Created by elman on 27.4.2017.
 * <p>
 * Derived from Magnus Woxblom
 * https://github.com/woxblom/DragListView
 */
public class SettingsListFieldsFragment extends Fragment {

    private BoardView mBoardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_list_items_board, container, false);

        mBoardView = view.findViewById(R.id.board_view);
        mBoardView.setSnapToColumnsWhenScrolling(true);
        mBoardView.setSnapToColumnWhenDragging(true);
        mBoardView.setSnapDragItemToTouch(true);
        mBoardView.setCustomDragItem(new MyDragItem(getActivity(), R.layout.settings_list_item));
        mBoardView.setBoardListener(new BoardView.BoardListener() {
            @Override
            public void onItemDragStarted(int column, int row) {
            }

            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                saveSettings();
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
            }

            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) {
            }

            @Override
            public void onFocusedColumnChanged(int oldColumn, int newColumn) {
            }

            @Override
            public void onColumnDragStarted(int position) {
            }

            @Override
            public void onColumnDragChangedPosition(int oldPosition, int newPosition) {
            }

            @Override
            public void onColumnDragEnded(int position) {
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences preferences = SharedObjects.getInstance().preferences;
        String mFieldsLine1 = preferences.getString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE1,
                Movies.defaultListFieldsLine1);
        String mFieldsLine2 = preferences.getString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE2,
                Movies.defaultListFieldsLine2);
        String mFieldsLine3 = preferences.getString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE3,
                Movies.defaultListFieldsLine3);

        final ArrayList<SettingsListField> mFieldsArray1 = new ArrayList<>();
        final ArrayList<SettingsListField> mFieldsArray2 = new ArrayList<>();
        final ArrayList<SettingsListField> mFieldsArray3 = new ArrayList<>();
        final ArrayList<SettingsListField> mFieldsArrayUnused = new ArrayList<>();

        // Create array of all available fields
        for (SettingsListField fld : Movies.availableListFields) {
            String humanName = getString(fld.getResource());
            fld.setDisplayText(humanName);
            mFieldsArrayUnused.add(fld);
        }

        // Move visible fields to separate array lists
        final ArrayList<Pair<String, ArrayList<SettingsListField>>> loadedLines = new ArrayList<>();
        loadedLines.add(new Pair<>(mFieldsLine1, mFieldsArray1));
        loadedLines.add(new Pair<>(mFieldsLine2, mFieldsArray2));
        loadedLines.add(new Pair<>(mFieldsLine3, mFieldsArray3));

        for (Pair<String, ArrayList<SettingsListField>> loadedLine : loadedLines) {
            String loadedPrefs = loadedLine.first;
            ArrayList<SettingsListField> loadedArray = loadedLine.second;

            for (String fldDB : loadedPrefs.split(",")) {
                for (SettingsListField fld : mFieldsArrayUnused) {
                    if (fld.getDatabaseField().equals(fldDB)) {
                        loadedArray.add(fld);
                        mFieldsArrayUnused.remove(fld);
                        break;
                    }
                }
            }
        }

        // Create columns based on created array lists
        final ArrayList<ArrayList<SettingsListField>> columns = new ArrayList<>();
        columns.add(mFieldsArray1);
        columns.add(mFieldsArray2);
        columns.add(mFieldsArray3);
        columns.add(mFieldsArrayUnused);

        // Prepare names of the columns
        final String[] columnNames = new String[]{
                String.valueOf(getText(R.string.settings_cards_column1_title)),
                String.valueOf(getText(R.string.settings_cards_column2_title)),
                String.valueOf(getText(R.string.settings_cards_column3_title)),
                String.valueOf(getText(R.string.settings_cards_column4_title))
        };
        int currentColumn = 0;

        for (ArrayList<SettingsListField> mItemArray : columns) {
            final SettingsListFieldsAdapter listAdapter = new SettingsListFieldsAdapter(mItemArray, R.layout.settings_list_item, R.id.item_layout, true);
            final View header = View.inflate(getActivity(), R.layout.settings_list_header, null);
            ((TextView) header.findViewById(R.id.card_column_header_text)).setText(columnNames[currentColumn++]);
            mBoardView.addColumn(listAdapter, header, null, false);
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = SharedObjects.getInstance().preferences.edit();

        // find all columns and save them to the settings
        for (int f = 0; f < mBoardView.getColumnCount() - 1; f++) {
            DragItemAdapter adapter = mBoardView.getAdapter(f);

            ArrayList<String> prefsArrayList = new ArrayList<>();

            for (Object field : adapter.getItemList()) {
                SettingsListField fld = (SettingsListField) field;
                prefsArrayList.add(fld.getDatabaseField());
            }

            editor.putString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE + (f + 1),
                    Utils.arrayToString(prefsArrayList, ","));
        }

        editor.apply();

        Toast.makeText(getContext(), getString(R.string.pref_setting_list_saved), Toast.LENGTH_SHORT).show();

        if (S.INFO)
            Log.i(S.TAG, "Refresh requested (" + SettingsConstants.KEY_PREF_MOVIES_LIST_LINE + ")");
    }

    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.card_text)).getText();
            ((TextView) dragView.findViewById(R.id.card_text)).setText(text);
            CardView dragCard = dragView.findViewById(R.id.card);
            CardView clickedCard = clickedView.findViewById(R.id.card);

            dragCard.setMaxCardElevation(40);
            dragCard.setCardElevation(clickedCard.getCardElevation());
            // I know the dragView is a FrameLayout and that is why I can use setForeground below api level 23
            dragCard.setForeground(clickedView.getResources().getDrawable(R.drawable.card_view_drag_foreground));
        }

        @Override
        public void onMeasureDragView(View clickedView, View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            CardView clickedCard = clickedView.findViewById(R.id.card);
            int widthDiff = dragCard.getPaddingLeft() - clickedCard.getPaddingLeft() + dragCard.getPaddingRight() -
                    clickedCard.getPaddingRight();
            int heightDiff = dragCard.getPaddingTop() - clickedCard.getPaddingTop() + dragCard.getPaddingBottom() -
                    clickedCard.getPaddingBottom();
            int width = clickedView.getMeasuredWidth() + widthDiff;
            int height = clickedView.getMeasuredHeight() + heightDiff;
            dragView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            dragView.measure(widthSpec, heightSpec);
        }

        @Override
        public void onStartDragAnimation(View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 40);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }

        @Override
        public void onEndDragAnimation(View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 6);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }
    }
}
