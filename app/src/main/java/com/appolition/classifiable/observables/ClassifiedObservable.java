package com.appolition.classifiable.observables;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;

//Based off of BaseObservable
public class ClassifiedObservable implements Observable {
    private transient PropertyChangeRegistry callbacks;

    public ClassifiedObservable() {
        callbacks = new PropertyChangeRegistry();
    }

    @Override
    public void addOnPropertyChangedCallback(@NonNull OnPropertyChangedCallback callback) {
        synchronized (this) {
            if (callbacks == null) {
                callbacks = new PropertyChangeRegistry();
            }
        }

        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(@NonNull OnPropertyChangedCallback callback) {
        synchronized (this) {
            if (callbacks == null) {
                return;
            }
        }

        callbacks.remove(callback);
    }

    public ClassifiedObservable notifyPropertyChanged(int propertyId) {
        synchronized (this) {
            if (callbacks == null) {
                return this;
            }
        }

        callbacks.notifyChange(this, propertyId);

        return this;
    }
}
