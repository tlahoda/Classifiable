/**Copyright 2019 Thomas Lahoda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
    public void add_NullCallbackAdded() {
        foo.add(FooClassifiers.URL, null);

        assertNull("Callback was added", foo.callbacks);
    }

    @Test
    public void add_CallbackAdded() {
        foo.add(FooClassifiers.URL, () -> {
            //noop
        });

        assertNotNull("Callbacks not null", foo.callbacks);

        assertEquals("Callback was not added", 1, foo.callbacks.size());

        List<ClassifiedObservable.OnPropertChangedCallback> callbacks = foo.callbacks.get(FooClassifiers.URL);

        assertEquals("Callback  was not added", 1, callbacks.size());
    }

    @Test
    public void remove_NoCallbackAdded() {
        foo.remove(FooClassifiers.URL, () -> {
            //noop
        });

        assertNull("Callbacks map was created", foo.callbacks);
    }

    @Test
    public void remove_AllCallbackAdded_UrlCallbackRemoved() {
        ClassifiedObservable.OnPropertChangedCallback callback = () -> {
            //noop
        };

        foo.add(FooClassifiers._ALL, () -> {
            //noop
        });

        foo.remove(FooClassifiers.URL, callback);

        assertNotNull("Callbacks map was created", foo.callbacks);

        assertNull("Classifier callback list was created", foo.callbacks.get(FooClassifiers.URL));
    }

    @Test
    public void remove_NullCallbackAdded() {
        ClassifiedObservable.OnPropertChangedCallback callback = null;

        foo.add(FooClassifiers.URL, callback);

        assertNull("Callback map was not created", foo.callbacks);

        foo.remove(FooClassifiers.URL, callback);

        assertNull("Callback map was created", foo.callbacks);
    }

    @Test
    public void remove_CallbackAdded() {
        ClassifiedObservable.OnPropertChangedCallback callback = () -> {
            //noop
        };

        foo.add(FooClassifiers.URL, callback);

        assertNotNull("Callback map was not created", foo.callbacks);

        assertNotNull("Callback list was not created", foo.callbacks.get(FooClassifiers.URL));

        assertEquals("Callback was not add", 1, foo.callbacks.get(FooClassifiers.URL).size());

        foo.remove(FooClassifiers.URL, callback);

        assertNull("Callback was not removed", foo.callbacks);
    }

    @Test
    public void clear_NoCallbackAdded() {
        foo.clear();

        assertNull(foo.callbacks);
    }

    @Test
    public void clear_CallbackAdded() {
        foo.add(FooClassifiers.URL, () -> {
            //noop
        });
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
        foo.add(FooClassifiers.URL, () -> {
            //noop
        });
        foo.clear(FooClassifiers.URL);

        assertNull("Callbacks were not cleared", foo.callbacks);
    }

    @Test
    public void notifyPropertChanged_NoCallbackAdded() {
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

        foo.add(FooClassifiers.URL, () -> {
            firstCallbackCalled = true;

            latch.countDown();
        });

        foo.setUrl("bar");

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("Callback was not called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_DefaultCallbackAdded_SinglePropertyChanged() {
        foo.add(FooClassifiers.URL, () -> {
            //noop
        });

        foo.setUrl("bar");

        assertFalse("Callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_CallbackAdded_SinglePropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, () -> {
            firstCallbackCalled = true;

            latch.countDown();
        });

        foo.callbacks.put(FooClassifiers.URL, null);

        foo.notifyPropertyChanged(FooClassifiers.URL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertFalse("URL callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_NoCallbackAdded_AllPropertiesChanged() {
        assertNull("Callbacks not null", foo.callbacks);

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        assertFalse("Callback was called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_CallbackAdded_AllPropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers.URL, () -> {
            firstCallbackCalled = true;

            latch.countDown();
        });

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("Callback was not called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_AllCallbackAdded_AllPropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        foo.add(FooClassifiers._ALL, () -> {
            firstCallbackCalled = true;

            latch.countDown();
        });

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("Callback was not called", firstCallbackCalled);
    }

    @Test
    public void notifyPropertChanged_AllCallbackAdded_UrlCallbackAdded_AllPropertiesChanged() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        foo.add(FooClassifiers._ALL, () -> {
            firstCallbackCalled = true;

            latch.countDown();
        });

        foo.add(FooClassifiers.URL, () -> {
            secondCallbackCalled = true;

            latch.countDown();
        });

        foo.callbacks.put(FooClassifiers.URL, null);

        foo.notifyPropertyChanged(FooClassifiers._ALL);

        latch.await(5, TimeUnit.MILLISECONDS);

        assertTrue("_ALL callback was called", firstCallbackCalled);

        assertFalse("URL callback was not called", secondCallbackCalled);
    }
}
