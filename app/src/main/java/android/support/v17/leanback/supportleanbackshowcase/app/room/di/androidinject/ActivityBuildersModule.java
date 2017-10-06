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

package android.support.v17.leanback.supportleanbackshowcase.app.room.di.androidinject;

import android.app.Activity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.overview.LiveDataRowsActivity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.search.SearchActivity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.androidinjectorannotation.LiveDataOverviewActivitySubcomponent;
import android.support.v17.leanback.supportleanbackshowcase.app.room.di.scope.PerActivity;
import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;


/**
 * Traditionally, to do the dependency injection in android, we usually end up with the boilerplate
 * code like
 * public class FrombulationActivity extends Activity {
 *  @Inject Frombulator frombulator;
 *
 * @Override
 * public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 *   // DO THIS FIRST. Otherwise frombulator might be null!
 *   ((SomeApplicationBaseType) getContext().getApplicationContext())
 *       .getApplicationComponent()
 *       .newActivityComponentBuilder()
 *       .activity(this)
 *       .build()
 *       .inject(this);
 *   // ... now you can write the exciting code
 * }
 * }
 *
 * The key is to use sub-component to narrow down the scope from application to specific activity/
 * fragment. (This is why if the module or provision from the app component/ module is marked with
 * @Singleton the sub - component [no matter for fragment or activity] it should be marked as some
 * smaller scope like @PerFragment, or @PerActivity)
 *
 * Since the module dependent by the component should have the same scope, and the sub component
 * must have smaller scope than their parent component. This is also the reason to have subcomponent
 * deployed in dependency injection.
 *
 * Also, there are several thing we should pay attention to when we are using the subcomponent.
 *
 * The sub-component must declare the builder (The builder is the entrance for the sub-component,
 * and can be used to represent the sub graph in the dependency injection graph, so even when we are
 * using the newly added the android injector, we still have to define the builder explicitly)
 * interface inside of it, the interface should also contains a build method which return type is
 * the sub-component itself. The dagger can create the dependency injection graph rely on those
 * information.
 *
 * The benefits of using the sub-component is the sub components can use all the provision method
 * provided by the parent component, and inject to the target through the main component. (Dagger
 * it self won't generate the dagger_subcomponent for the subcomponents, it should be provided
 * through some methods defined in the parent component).
 *
 * We use this method to inject the required field for detail fragment with video background.
 *
 * The drawback for it is obvious. Those boiler plate code will confuse the people who read the code
 * and copy/ paste same code among different files will make program error prone.
 *
 * But in Dagger 2.10. There is a new injector can be used to simplify this process.
 *
 * This injector is AndroidInjector. To use this new infrastructure, several things need to be done.
 *
 *     1. The sub-component should be extended from Android Injector.
 *
 *     2. The sub-component's builder should be extended from AndroidInjector.Builder<targetClass>
 *        So it can be converted to AndroidInjector.Factory and contributes to the injector
 *        factories' map.
 *
 *     3. The sub-component (android injector) should be installed to the module depended on the
 *        parent components (e.g. Activities sub-components should be installed to app's component
 *        through the modules required by the app's component)
 *
 *     4. Also sub-component's builder (AndroidInjector.Builder) should be provided into a map
 *        based multi binding to a module required by the parent component in
 *        AndroidInjector.Factory format through @Binds annotation.
 *
 * After those procedure, all the following graph - creation/ inject will be performed by dagger
 * automatically. (It will be explained in the following part with the newly added
 * @ContributesAndroidInjector annotation).
 *
 * Then in Dagger 2.11. The previous process can be simplified further more. Using the
 * {@link @ContributesAndroidInjector} annotation, dagger will generate the sub-component we wrote
 * above for android - ui components. In this sample app, we use this approach to inject live data
 * fragment.
 *
 * The processing sequence is:
 * 1. There is a regular component (injector) which will be injected to SampleApplication.java.
 * 2. The application class itself will implement the implements HasActivityInjector interface. And
 *    override the following method
 *
 *    @override
 *    public AndroidInjector<Activity> activityInjector() {}
 *
 *    It will return the AndroidInjector<Activity> which is provided by this
 *    ActivityBuildersModule's @ContributesAndroidInjector annotated field.
 *    So in activity class (In this sample app, it should be LiveDataRowsActivity). The injector can
 *    be injected using AndroidInjection.inject(this)
 *    There are several things need to be clarified and pay attention to:
 *
 *    1). How did it work:
 *
 *       a. AndroidInjection.inject() gets a DispatchingAndroidInjector<Activity> from the
 *          Application and passes your activity to inject(Activity).
 *
 *       b. Then the DispatchingAndroidInjector will look up the AndroidInjector.Factory for your
 *          activity’s class (which is YourActivitySubcomponent.Builder).
 *
 *    2). Where to inject the activity using the AndroidInjection.inject()
 *
 *       For Activity, the injection should be finished before the super.onCreate(). The reason
 *       is the injection for fragment may happen in super.onCreate(). In order to make sure the
 *       dependency injection for the fragment is successful, the Activity must already be injected.
 *       Similarly, the preferable place to inject fragment should be before the function call of
 *       super.onAttach()
 *
 *    3). How to hook the dependency injection for fragment.
 *
 *        The relationship between activity and fragment is basically the same as the relationship
 *        between the activity and application. So the processing logic is:
 *
 *        a. Activity should implement the HasFragmentInjector and declare a field
 *
 *           @Inject
 *           DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
 *
 *           This injector should be returned by this overrided function
 *
 *           @Override
 *           public AndroidInjector<Fragment> fragmentInjector() {
 *               return dispatchingAndroidInjector;
 *           }
 *
 *           The previous procedure will provide necessary information for dagger system, when the
 *           dependency injection work is automated. (Finding appropriate injector through fragment
 *           multi binding map)
 *
 *        b. Activity's android injector (aka sub component) should contain a module which contains
 *           the required provision method which is required by the @inject field from the activity.
 *
 *           Since DispatchingAndroidInjector will finally look up the AndroidInjector.Factory suite
 *           for the fragment when the fragment ask for being injected (
 *           AndroidInjection.inject(this) before the onAttach() call).
 *
 *           The responsibility for this module is to install the sub - component of fragment (
 *           fragment's sub - component).
 *
 *       c. Similarly the activity should have another module which can provide the
 *          AndroidInjector.Factory explicitly through @Binds annotation. AndroidInjector.Factory
 *          is the super type of AndroidInjector.Builder from the inheritence architecture's
 *          perspective. So the subcomponent.Builder (AndroidInjector.Builder) can be provided as
 *          the parameter directly and return the factory we need for future look up.
 *
 *       d. But when we rely on the @ContributesAndroidInjector annotation. Those two modules can be
 *          merged into a single module.
 *
 *          The search activity subcomponent (can declare the dependency through
 *          @ContributesAndroidInjector(modules =
 *          SearchFragmentInjectorInstallmentFactoryBindingModule.class)). Inside the
 *          SearchFragmentInjectorInstallmentFactoryBindingModule, we don't have to install the
 *          subcomponent explicitly through
 *          @Modules(subcomponent = ...)
 *
 *          Also the sub-component don't have to declare the @Binds method explicitly.
 *
 *          It can be finished with
 *
 *           @Module
 *           public abstract class SearchFragmentInjectorInstallmentFactoryBindingModule {
 *           @ContributesAndroidInjector
 *           abstract SearchFragment contributeSearchFragment();
 *           }
 *
 *          On the one hand it will generate the subcomponent just inside the module (which is not
 *          a suggested way to write component, since the purpose of component is for re-using) and
 *          auto-generate the @Binds provision method.
 *
 *          The generated code looks like:
 *
 *           package android.support.v17.leanback.supportleanbackshowcase.app.room.di.app;
 *
 *           import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.search.SearchFragment;
 *           import android.support.v4.app.Fragment;
 *           import dagger.Binds;
 *           import dagger.Module;
 *           import dagger.Subcomponent;
 *           import dagger.android.AndroidInjector;
 *           import dagger.android.support.FragmentKey;
 *           import dagger.multibindings.IntoMap;

 *            @Module(
 *                subcomponents =
 *                SearchFragmentModule_ContributeSearchFragment.SearchFragmentSubcomponent.class
 *            )
 *            public abstract class SearchFragmentModule_ContributeSearchFragment {
 *            private SearchFragmentModule_ContributeSearchFragment() {}
 *
 *            @Binds
 *            @IntoMap
 *            @FragmentKey(SearchFragment.class)
 *            abstract AndroidInjector.Factory<? extends Fragment> bindAndroidInjectorFactory(
 *            SearchFragmentSubcomponent.Builder builder);
 *
 *            @Subcomponent
 *            public interface SearchFragmentSubcomponent extends AndroidInjector<SearchFragment> {
 *            @Subcomponent.Builder
 *            abstract class Builder extends AndroidInjector.Builder<SearchFragment> {}
 *            }
 *            }
 *
 *           As we have stated before, in this sample application, we have demonstrate the usage of
 *           all these three approaches. Since there is no silver bullet (the newly added annotation
 *           cannot solve all the problems), sometime to work around the restriction we have to
 *           write the boiler plate.
 *
 *           See {@link AppInjector to understand how to automated finish the dependency injection}
 */

@Module
public abstract class ActivityBuildersModule {

    @PerActivity
    @ContributesAndroidInjector(modules
            = {SearchActivityModule.class,
            SearchFragmentInjectorInstallmentFactoryBindingModule.class})
    abstract SearchActivity contributeToAndriodInjectorForSearchActivity();

    @Binds
    @IntoMap
    @ActivityKey(LiveDataRowsActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> bindActivityInjectorFactory(LiveDataOverviewActivitySubcomponent.Builder builder);
}
