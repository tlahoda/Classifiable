package com.appolition.classifiable_processor;

import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;

public interface JavaFileWriter {
    void writeTo(String packageName, TypeSpec.Builder enumBuilder, Filer filer) throws IOException;
}
