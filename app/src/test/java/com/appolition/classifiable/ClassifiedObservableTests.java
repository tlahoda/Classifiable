package com.appolition.classifiable;

import com.appolition.classifiable.observables.ClassifiedObservable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ClassifiedObservableTests {
    private static class Foo extends ClassifiedObservable<FooClassifiers> {
        private String url;

        public Foo setUrl(String url) {
            this.url = url;

            notifyPropertyChanged(FooClassifiers.URL);

            return this;
        }
    }

    //@Parcel
    private enum FooClassifiers {
        _ALL,

        URL
    }

    private Foo foo;

    private boolean firstCallbackCalled;
    private boolean secondCallbackCalled;

    @Before
    public void setup() {
        foo = new Foo();

        firstCallbackCalled = false;
        secondCallbackCalled = false;
    }

    @Test
    public void add_NullCallbackAdded() throws InterruptedException {
        foo.add(FooClassifiers.URL, null);

        assertNull("Callback was added", foo.callbacks);
    }

    @Test
    public void add_CallbackAdded() throws InterruptedException {
        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<>());

        assertNotNull("Callbacks not null", foo.callbacks);

        assertTrue("Callback was not added", foo.callbacks.size() == 1);

        List<ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>> callbacks = foo.callbacks.get(FooClassifiers.URL);

        assertEquals("Callback  was not added", 1, callbacks.size());
    }



    @Test
    public void remove_NoCallbackAdded() {
        foo.remove(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<>());

        assertNull("Callbacks map was created", foo.callbacks);
    }

    @Test
    public void remove_AllCallbackAdded_UrlCallbackRemoved() {
        ClassifiedObservable.OnPropertChangedCallback<FooClassifiers> callback = new ClassifiedObservable.OnPropertChangedCallback<>();

        foo.add(FooClassifiers._ALL, new ClassifiedObservable.OnPropertChangedCallback<>());

        foo.remove(FooClassifiers.URL, callback);

        assertNotNull("Callbacks map was created", foo.callbacks);

        assertNull("Classifier callback list was created", foo.callbacks.get(FooClassifiers.URL));
    }

    @Test
    public void remove_NullCallbackAdded() {
        ClassifiedObservable.OnPropertChangedCallback<FooClassifiers> callback = null;

        foo.add(FooClassifiers.URL, callback);

        assertNull("Callback map was not created", foo.callbacks);

        foo.remove(FooClassifiers.URL, callback);

        assertNull("Callback map was created", foo.callbacks);
    }

    @Test
    public void remove_CallbackAdded() {
        ClassifiedObservable.OnPropertChangedCallback<FooClassifiers> callback = new ClassifiedObservable.OnPropertChangedCallback<>();

        foo.add(FooClassifiers.URL, callback);

        assertNotNull("Callback map was not created", foo.callbacks);

        assertNotNull("Callback list was not created", foo.callbacks.get(FooClassifiers.URL));

        assertEquals("Callback was not add", 1, foo.callbacks.get(FooClassifiers.URL).size());

        foo.remove(FooClassifiers.URL, callback);

        assertNull("Callback was not removed", foo.callbacks);
    }

//    @Test
//    public void remove_CallbackAdded_ListNull() {
//        ClassifiedObservable.OnPropertChangedCallback<FooClassifiers> callback = new ClassifiedObservable.OnPropertChangedCallback<>();
//
//        foo.add(FooClassifiers.URL, callback);
//
//        assertNotNull("Callback map was not created", foo.callbacks);
//
//        assertNotNull("Callback list was not created", foo.callbacks.get(FooClassifiers.URL));
//
//        assertEquals("Callback was not add", 1, foo.callbacks.get(FooClassifiers.URL).size());
//
//        foo.callbacks.put(FooClassifiers.URL, null);
//
//        foo.remove(FooClassifiers.URL, callback);
//
//        assertNull("Callback was not removed", foo.callbacks.get(FooClassifiers.URL));
//    }






















    @Test
    public void clear_NoCallbackAdded() {
        foo.clear();

        assertNull(foo.callbacks);
    }

    @Test
    public void clear_CallbackAdded() {
        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<>());
        foo.clear();

        assertNull("Callbacks were not cleared", foo.callbacks);
    }

    @Test
    public void clearClassifier_NoCallbackAdded() {
        foo.clear(FooClassifiers.URL);

        assertNull("Callbacks were not cleared", foo.callbacks);
    }

    @Test
    public void clearClassifier_CallbackAdded() {
        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<>());
        foo.clear(FooClassifiers.URL);

        assertNull("Callbacks were not cleared", foo.callbacks);
    }

    @Test
    public void notifyPropertChanged_NoCallbackAdded() throws InterruptedException {
        assertNull("Callbacks not null", foo.callbacks);

        foo.notifyPropertyChanged(FooClassifiers.URL);

        assertFalse("Callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_NullCallbackAdded_SinglePropertyChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, null);

        assertNull("Callback was added", foo.callbacks);

        foo.setUrl("bar");

        latch.await(5, TimeUnit.MILLISECONDS);

        assertFalse("Callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_CallbackAdded_SinglePropertyChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>() {
            @Override
            public void notifyPropertyChanged(FooClassifiers classifier) {
                firstCallbackCalled = true;

                latch.countDown();
            }
        });

        foo.setUrl("bar");

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("Callback was not called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_DefaultCallbackAdded_SinglePropertyChanged() throws InterruptedException {
        //CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<>());

        foo.setUrl("bar");

        //latch.await(5, TimeUnit.MILLISECONDS);

        assertFalse("Callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_CallbackAdded_SinglePropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>() {
            @Override
            public void notifyPropertyChanged(FooClassifiers classifier) {
                firstCallbackCalled = true;

                latch.countDown();
            }
        });

        foo.callbacks.put(FooClassifiers.URL, null);

        foo.notifyPropertyChanged(FooClassifiers.URL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertFalse("URL callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_NoCallbackAdded_AllPropertiesChanged() throws InterruptedException {
        assertNull("Callbacks not null", foo.callbacks);

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        assertFalse("Callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_CallbackAdded_AllPropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>() {
            @Override
            public void notifyPropertyChanged(FooClassifiers classifier) {
                firstCallbackCalled = true;

                latch.countDown();
            }
        });

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("Callback was not called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_AllCallbackAdded_AllPropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers._ALL, new ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>() {
            @Override
            public void notifyPropertyChanged(FooClassifiers classifier) {
                firstCallbackCalled = true;

                latch.countDown();
            }
        });

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("Callback was not called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_AllCallbackAdded_UrlCallbackAdded_AllPropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        foo.add(FooClassifiers._ALL, new ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>() {
            @Override
            public void notifyPropertyChanged(FooClassifiers classifier) {
                firstCallbackCalled = true;

                latch.countDown();
            }
        });

        foo.add(FooClassifiers.URL, new ClassifiedObservable.OnPropertChangedCallback<FooClassifiers>() {
            @Override
            public void notifyPropertyChanged(FooClassifiers classifier) {
                secondCallbackCalled = true;

                latch.countDown();
            }
        });

        foo.callbacks.put(FooClassifiers.URL, null);

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("_ALL callback was called", firstCallbackCalled);

        assertFalse("URL callback was not called", secondCallbackCalled);
    }
}
