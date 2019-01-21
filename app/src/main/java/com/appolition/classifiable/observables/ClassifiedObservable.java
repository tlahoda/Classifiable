package com.appolition.classifiable.observables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import org.parceler.Parcel;

//Based off of BaseObservable from Android databinding
@Parcel
public class ClassifiedObservable<EnumType extends Enum<EnumType>> {
    private transient Map<EnumType, List<OnPropertChangedCallback<EnumType>>> callbacks;

    public ClassifiedObservable<EnumType> add(EnumType enumType, @NonNull OnPropertChangedCallback<EnumType> callback) {
        synchronized (this) {
            if (callbacks == null) {
                callbacks = new HashMap<>();
            }
        }

        List<OnPropertChangedCallback<EnumType>> enumCallbacks = callbacks.get(enumType);

        if (enumCallbacks == null) {
            enumCallbacks = new ArrayList<>();

            callbacks.put(enumType, enumCallbacks);
        }

        enumCallbacks.add(callback);

        return this;
    }

    public ClassifiedObservable<EnumType> remove(EnumType enumType, @NonNull OnPropertChangedCallback<EnumType> callback) {
        synchronized (this) {
            if (callbacks == null) {
                return this;
            }
        }

        List<OnPropertChangedCallback<EnumType>> enumCallbacks = callbacks.get(enumType);

        if (enumCallbacks == null) {
            return this;
        }

        enumCallbacks.remove(callback);

        if (enumCallbacks.size() == 0) {
            callbacks.remove(enumType);
        }

        return this;
    }

    public ClassifiedObservable<EnumType> clear() {
        synchronized (this) {
            if (callbacks == null) {
                return this;
            }
        }

        callbacks.clear();

        return this;
    }

    public ClassifiedObservable<EnumType> clear(EnumType enumType) {
        synchronized (this) {
            if (!callbacks.containsKey(enumType)) {
                return this;
            }
        }

        callbacks.remove(enumType);

        return this;
    }

    public ClassifiedObservable<EnumType> notifyPropertyChanged(EnumType enumType) {
        synchronized (this) {
            if (callbacks == null) {
                return this;
            }
        }

        List<OnPropertChangedCallback<EnumType>> enumCallbacks = callbacks.get(enumType);

        if (enumCallbacks == null || enumCallbacks.size() == 0) {
            return this;
        }

        for (OnPropertChangedCallback<EnumType> callback : enumCallbacks) {
            callback.notitfyChanged(enumType);
        }

        return this;
    }

    //This is an static class instead of an interface for Parceler
    @Parcel
    public static class OnPropertChangedCallback<EnumType extends Enum<EnumType>> {
        public void notitfyChanged(EnumType enumType) {
            //noop
        }
    }
}
