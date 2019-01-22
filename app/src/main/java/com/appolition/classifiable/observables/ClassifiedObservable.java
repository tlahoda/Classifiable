package com.appolition.classifiable.observables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

/**
 * A base class that allows for notifications of property changes to be sent to the callbacks
 * Loosely based off of BaseObservable from Android databinding
 *
 * @param <ClassifiersType>, the type of classifiers to use for property change notifications
 */
public abstract class ClassifiedObservable<ClassifiersType extends Enum<ClassifiersType>> {
    /**
     * The ordinal for notifying all properties changed
     */
    private static final int _ALL = 0;

    /**
     * The map of classifiers and the callbacks to notify on property changes
     */
    @VisibleForTesting
    public Map<ClassifiersType, List<OnPropertChangedCallback<ClassifiersType>>> callbacks;

    /**
     * Adds a callback for a particular classifier
     *
     * @param classifier, The classifier of the property to listen to for changes
     * @param callback, the callback to call on a change to the specified property
     *
     * @return ClassifiedObservable<ClassifiersType>, a reference to this instance
     */
    public ClassifiedObservable<ClassifiersType> add(ClassifiersType classifier, OnPropertChangedCallback<ClassifiersType> callback) {
        synchronized (this) {
            if (callback != null) {
                if (callbacks == null) {
                    callbacks = new HashMap<>();
                }

                List<OnPropertChangedCallback<ClassifiersType>> enumCallbacks = callbacks.get(classifier);

                if (enumCallbacks == null) {
                    enumCallbacks = new ArrayList<>();

                    callbacks.put(classifier, enumCallbacks);
                }

                enumCallbacks.add(callback);
            }
        }

        return this;
    }

    /**
     * Removes a callback for a particular classifier
     *
     * @param classifier, the classifier for which to remove a callback
     * @param callback, the callback to remove
     *
     * @return ClassifiedObservable<ClassifiersType>, a reference to this instance
     */
    public ClassifiedObservable<ClassifiersType> remove(ClassifiersType classifier, @NonNull OnPropertChangedCallback<ClassifiersType> callback) {
        synchronized (this) {
            if (callback != null) {
                if (callbacks == null) {
                    return this;
                }

                List<OnPropertChangedCallback<ClassifiersType>> enumCallbacks = callbacks.get(classifier);

                if (enumCallbacks == null) {
                    return this;
                }

                enumCallbacks.remove(callback);

                if (enumCallbacks.size() == 0) {
                    callbacks.remove(classifier);
                }

                if (callbacks.size() == 0) {
                    callbacks = null;
                }
            }
        }

        return this;
    }

    /**
     * Removes all callbacks
     *
     * @return ClassifiedObservable<ClassifiersType>, a reference to this instance
     */
    public ClassifiedObservable<ClassifiersType> clear() {
        synchronized (this) {
            if (callbacks == null) {
                return this;
            }

            callbacks.clear();
            callbacks = null;
        }

        return this;
    }

    /**
     * Removes all callback for the specified classifier
     *
     * @param classifier, the classifier to clear all callbacks
     *
     * @return ClassifiedObservable<ClassifiersType>, a reference to this instance
     */
    public ClassifiedObservable<ClassifiersType> clear(ClassifiersType classifier) {
        synchronized (this) {
            if (callbacks == null || !callbacks.containsKey(classifier)) {
                return this;
            }

            callbacks.remove(classifier);

            if (callbacks.size() == 0) {
                callbacks = null;
            }
        }

        return this;
    }

    /**
     * Notifies all callbacks when the specified property changes
     *
     * @param classifier, the classifier to use for property change notifications
     *
     * @return ClassifiedObservable<ClassifiersType>, a reference to this instance
     */
    public ClassifiedObservable<ClassifiersType> notifyPropertyChanged(ClassifiersType classifier) {
        synchronized (this) {
            if (callbacks == null) {
                return this;
            }

            if (classifier.ordinal() == _ALL) {
                if (callbacks.get(classifier) != null) {
                    for (OnPropertChangedCallback callback : callbacks.get(classifier)) {
                        if (callback != null) {
                            callback.notifyPropertyChanged(classifier);
                        }
                    }
                }

                for (Map.Entry<ClassifiersType, List<OnPropertChangedCallback<ClassifiersType>>> entry : callbacks.entrySet()) {
                    if ((entry.getValue() == null || entry.getValue().size() == 0) && entry.getKey().ordinal() != _ALL) {
                        continue;
                    }

                    for (OnPropertChangedCallback<ClassifiersType> callback : entry.getValue()) {
                        if (callback != null) {
                            callback.notifyPropertyChanged(entry.getKey());
                        }
                    }
                }

                return this;
            }

            List<OnPropertChangedCallback<ClassifiersType>> enumCallbacks = callbacks.get(classifier);

            if (enumCallbacks == null || enumCallbacks.size() == 0) {
                return this;
            }

            for (OnPropertChangedCallback<ClassifiersType> callback : enumCallbacks) {
                if (callback != null) {
                    callback.notifyPropertyChanged(classifier);
                }
            }
        }

        return this;
    }

    /**
     * A callback to use for property change notifications
     *
     * @param <ClassifiersType>, the type of classifiers to use for property change notifications
     */
    public interface OnPropertChangedCallback<ClassifiersType extends Enum<ClassifiersType>> {
        /**
         * Called when the specified property changes
         *
         * @param classifier, the classifier to use for property change notifications
         */
        void notifyPropertyChanged(ClassifiersType classifier);
    }
}
