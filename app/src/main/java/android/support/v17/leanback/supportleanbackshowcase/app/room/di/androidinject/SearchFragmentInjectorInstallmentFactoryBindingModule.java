package android.support.v17.leanback.supportleanbackshowcase.app.room.di.androidinject;

import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.search.SearchFragment;

import android.support.v17.leanback.supportleanbackshowcase.app.room.di.scope.PerFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by lijingjiang on 9/25/17.
 */

@Module
public abstract class SearchFragmentInjectorInstallmentFactoryBindingModule {
  @PerFragment
  @ContributesAndroidInjector(modules = SearchFragmentUIModule.class)
  abstract SearchFragment contributeSearchFragment();
}
