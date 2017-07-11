/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v17.leanback.supportleanbackshowcase.app.rows;

import android.content.Context;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.app.rows.ChannelContents;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;


/**
 * An AddRemoveButtonPresenter is used to generate Add/Remove channel action view at the
 * beginning of each playlist row.
 *
 * This presenter matches the action view to that of the rest of
 * clips in each row in both expanded/collapsed modes.
 */
public class AddRemoveButtonPresenter extends Presenter {

    private final Context mContext;
    private int mRowHeight = 0;
    private int mExpandedRowHeight = 0;

    public AddRemoveButtonPresenter(Context context) {
        mContext = context;
        setupRowHeights();
    }

    /**
     * Calculates the ImageCardView's height when it is in activated and non-activated state.
     * A card is in activated state when its parent row receives focus in which case the card
     * expands with both title and description views displayed as well as the main ImageView.
     * In non-activated state, the card parent's row is not focused and only main ImageView is
     * displayed. This calculation is done in order to match the height of the AddRemove card height
     * to that of the rest of cards in each row when it's expanded or collapsed.
     */
    private void setupRowHeights() {
        if (mRowHeight == 0) {
            int cardHeight = mContext.getResources().getDimensionPixelSize(R.dimen.card_height);
            ImageCardView cardView = new ImageCardView(mContext);
            cardView.setTitleText("card title");
            cardView.setContentText("card description");
            cardView.setMainImageDimensions(ViewGroup.LayoutParams.WRAP_CONTENT, cardHeight);
            cardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mRowHeight = cardView.getMeasuredHeight();
            cardView.setActivated(true);
            cardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mExpandedRowHeight = cardView.getMeasuredHeight();
        }
    }

    public static class AddRemoveChannelViewHolder extends Presenter.ViewHolder {
        public static final int REMOVE_MODE = 0;
        public static final int ADD_MODE = 1;
        private int mAddRemoveMode;

        AddRemoveChannelViewHolder(View v) {
            super(v);
        }

        public void setMode(int mode) {
            mAddRemoveMode = mode;
            ImageCardView cardView = (ImageCardView) view;
            if (mode == REMOVE_MODE) {
                cardView.setMainImage(view.getContext().getResources().getDrawable(
                        R.drawable.ic_remove_row_circle_black_24dp, view.getContext().getTheme()));
            } else {
                cardView.setMainImage(view.getContext().getResources().getDrawable(
                        R.drawable.ic_add_row_circle_black_24dp, view.getContext().getTheme()));
            }
        }

        public int getMode() {
            return mAddRemoveMode;
        }
    }

    @Override
    public AddRemoveChannelViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setActivated(boolean activated) {
                setMainImageDimensions(activated ? mExpandedRowHeight : mRowHeight,
                        activated ? mExpandedRowHeight : mRowHeight);
            }
        };
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setMainImage(mContext.getDrawable(R.drawable.ic_add_row_circle_black_24dp));
        cardView.setBackground(mContext.getDrawable(R.drawable.row_darker_gray_ripple));
        cardView.setMainImageDimensions(cardView.isActivated() ? mExpandedRowHeight : mRowHeight,
                cardView.isActivated() ? mExpandedRowHeight : mRowHeight);
        return new AddRemoveChannelViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        ChannelContents playlist = (ChannelContents) item;
        boolean isRemoveChannel = playlist.isChannelPublished();
        ((AddRemoveChannelViewHolder) viewHolder).setMode(
                isRemoveChannel ? AddRemoveChannelViewHolder.REMOVE_MODE
                        : AddRemoveChannelViewHolder.ADD_MODE);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }
}
