package com.appolition.classifiable_processor.writers;

import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;

public class IOExceptionThrowingJavaFileWriter implements JavaFileWriter {
    @Override
    public void writeTo(String packageName, TypeSpec.Builder enumBuilder, Filer filer) throws IOException {
        throw new IOException("Failed on purpose");
    }
}
