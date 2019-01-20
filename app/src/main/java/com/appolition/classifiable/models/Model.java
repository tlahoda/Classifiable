package com.appolition.classifiable.models;

import com.appolition.classifiable.observables.ClassifiedObservable;
import com.appolition.classifiable_annotation.Classifiable;

public class Model extends ClassifiedObservable {
    private String foo = "foo";
    private String bar = "bar";
    private String baz = "baz";

    @Classifiable
    public String getFooBar() {
        return String.format("%s:%s", foo, bar);
    }

    @Classifiable
    public String getFoo() {
        return foo;
    }

    public Model setFoo(String foo) {
        this.foo = foo;

        notifyPropertyChanged(ModelClassifiers.foo);

        return this;
    }

    @Classifiable
    public String getBar() {
        return bar;
    }

    public Model setBar(String bar) {
        this.bar = bar;

        notifyPropertyChanged(ModelClassifiers.bar);

        return this;
    }

    @Classifiable
    public String getBaz() {
        return baz;
    }

    public Model setBaz(String baz) {
        this.baz = baz;

        notifyPropertyChanged(ModelClassifiers.baz);

        return this;
    }
}
