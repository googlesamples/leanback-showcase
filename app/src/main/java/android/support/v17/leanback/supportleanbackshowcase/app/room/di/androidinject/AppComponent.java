package android.support.v17.leanback.supportleanbackshowcase.app.room.di.androidinject;


import android.app.Application;
import android.support.v17.leanback.supportleanbackshowcase.app.room.controller.app.SampleApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
    AndroidInjectionModule.class,
    AppModule.class,
    ActivityBuildersModule.class,
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(SampleApplication sampleApplication);
}
