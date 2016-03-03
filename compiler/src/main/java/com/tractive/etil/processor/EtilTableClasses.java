package com.tractive.etil.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * Created by stephan on 03/03/16.
 */
public class EtilTableClasses {

    public static final String CLASS_NAME = "EtilMapper";


    public List<EtilTableAnnotatedClass> mEtilTableClasses = new ArrayList<>();
    private ClassName mCursorClass;


    public void add(EtilTableAnnotatedClass _annotatedClass) {
        mEtilTableClasses.add(_annotatedClass);

    }

    public void generateCode(Elements _elementUtils, Filer _filer) throws IOException {
        if (mEtilTableClasses.size() == 0) {
            return;
        }


        mCursorClass = ClassName.get("android.database", "Cursor");

        MethodSpec.Builder main = MethodSpec.methodBuilder("mapCursorToModel")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(AnnotationSpec
                        .builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())

                .returns(TypeVariableName.get("T"))
                .addTypeVariable(TypeVariableName.get("T"))
                .addParameter(ParameterizedTypeName.get(ClassName.get("java.lang","Class"), TypeVariableName.get("T")), "_class")
                .addParameter(mCursorClass, "_cursor")
                .beginControlFlow("switch (_class.getSimpleName())");

        for (EtilTableAnnotatedClass _modelClass : mEtilTableClasses) {
            main.addCode("case \"" + _modelClass.getSimpleTypeName() + "\":\n")
                    .addStatement("return (T) cursorTo" + _modelClass.getSimpleTypeName() + "(_cursor)");
        }

        main.addCode("default:\n")
                .addStatement("throw new java.lang.IllegalArgumentException(\"Model is not defined via annotations\")")
                .endControlFlow();

        TypeSpec helloWorld = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main.build())
                .addMethods(createMappingMethods())
                .build();

        JavaFile javaFile = JavaFile.builder("com.tractive.android.etil", helloWorld)
                .build();

        javaFile.writeTo(_filer);

    }

    private Iterable<MethodSpec> createMappingMethods() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (EtilTableAnnotatedClass _modelClass : mEtilTableClasses) {

            MethodSpec.Builder builder = MethodSpec
                    .methodBuilder("cursorTo" + _modelClass.getSimpleTypeName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(mCursorClass, "_cursor")
                    .addStatement("$L model = new $L()", _modelClass.getTypeElement(), _modelClass.getTypeElement());

            for (EtilTableAnnotatedClass.FieldAndColumnInfo _info : _modelClass.getFieldAndColumnInfo()) {
                builder.addStatement(
                        "model." + _info.fieldName + " = " + "_cursor." + _info.accessMethod + "(_cursor.getColumnIndex(\"" + _info.columnName + "\"))");
            }

            builder.returns(ClassName.get(_modelClass.getTypeElement()))
                    .addStatement("return model");
            methodSpecs.add(builder.build());

        }

        return methodSpecs;
    }


}
