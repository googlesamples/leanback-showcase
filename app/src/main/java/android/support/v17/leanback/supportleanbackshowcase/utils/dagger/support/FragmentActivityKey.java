package android.support.v17.leanback.supportleanbackshowcase.utils.dagger.support;

import android.support.v4.app.FragmentActivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import dagger.MapKey;

@MapKey
@Target({ElementType.METHOD})
public @interface FragmentActivityKey {
    Class<? extends FragmentActivity> value();
}