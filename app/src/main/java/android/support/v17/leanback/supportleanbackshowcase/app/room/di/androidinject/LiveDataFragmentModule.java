package android.support.v17.leanback.supportleanbackshowcase.app.room.di.androidinject;

import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.overview.LiveDataFragment;

import android.support.v17.leanback.supportleanbackshowcase.app.room.di.presenter.PresenterModule;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.scope.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by lijingjiang on 9/25/17.
 */
@Module
public abstract class LiveDataFragmentModule {

  @PerFragment
  @ContributesAndroidInjector(modules = {PresenterModule.class})
  abstract LiveDataFragment contributeLiveDataFragment();
}
