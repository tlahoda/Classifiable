package com.appolition.classifiable_processor.writers;

import com.appolition.classifiable_processor.writers.JavaFileWriter;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;

public class ProcessingEnvironmentJavaFileWriter implements JavaFileWriter {
    @Override
    public void writeTo(String packageName, TypeSpec.Builder enumBuilder, Filer filer) throws IOException {
        JavaFile.builder(packageName, enumBuilder.build())
                .indent("    ")
                .build().writeTo(filer);
    }
}
