package android.support.v17.leanback.supportleanbackshowcase.app.room.di.androidinject;

import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.detail.LiveDataDetailViewWithVideoBackgroundFragment;

import android.support.v17.leanback.supportleanbackshowcase.app.room.di.presenter.PresenterModule;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.scope.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by lijingjiang on 9/25/17.
 */

// generated fragment sub component will be added to this module
// this module will be installed to a higher level component the activity's component
// so module + contribute injector annotation make sure we don't have the previous two steps
//
@Module
public abstract class LiveDataDetailViewWithVideoBackgroundFragmentModule {

  @PerFragment
  @ContributesAndroidInjector (modules = {PresenterModule.class})
  abstract LiveDataDetailViewWithVideoBackgroundFragment contributeLiveDataDetailViewWithVideoBackgroundFragment();

}
