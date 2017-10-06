package android.support.v17.leanback.supportleanbackshowcase.app.room.di.action;


import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.app.SampleApplication;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.action.qualifier.LoadingActionQualifier;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.action.qualifier.PlayActionQualifier;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.action.qualifier.PreviewActionQualifier;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.action.qualifier.RentActionQualifier;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.scope.PerFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v4.content.res.ResourcesCompat;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Created by jingjiangli on 9/20/17.
 */

@Module
public class ActionsModule {

  public static final int ACTION_PLAY = 1;
  public static final int ACTION_RENT = 2;
  public static final int ACTION_PREVIEW = 3;
  public static final int ACTION_LOADING = 4;

  @PerFragment
  @PlayActionQualifier
  @Provides
  Action providePlayAction() {

    return new Action(ACTION_PLAY,
        SampleApplication.getInstance().getString(R.string.livedata_action_play));
  }

  @PerFragment
  @RentActionQualifier
  @Provides
  Action provideRentAction() {

    return new Action(ACTION_RENT,
        SampleApplication.getInstance().getString(R.string.livedata_actoin_rent),
        SampleApplication.getInstance().getString(R.string.livedata_rent_price), ResourcesCompat
        .getDrawable(
            SampleApplication.getInstance().getResources(),
            R.drawable.ic_favorite_border_white_24dp,
            SampleApplication.getInstance().getTheme()));
  }

  @PerFragment
  @PreviewActionQualifier
  @Provides
  Action providePreviewAction() {
    return new Action(ACTION_PREVIEW,
        SampleApplication.getInstance().getString(R.string.livedata_action_preview));
  }

  @PerFragment
  @LoadingActionQualifier
  @Provides
  Action provideLoadingAction() {
    return new Action(ACTION_LOADING,
        SampleApplication.getInstance().getString(R.string.livedata_action_loading));
  }
}

