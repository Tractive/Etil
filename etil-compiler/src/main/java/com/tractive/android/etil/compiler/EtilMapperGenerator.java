package com.tractive.android.etil.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.tractive.android.etil.compiler.data.EtilTableAnnotatedClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;


public class EtilMapperGenerator {

    public static final String CLASS_NAME = "EtilMapper";
    public List<com.tractive.android.etil.compiler.data.EtilTableAnnotatedClass> mEtilTableClasses = new ArrayList<>();

    private final ClassName mContentValuesClass;
    private final ClassName mCursorClass;
    private final ClassName mStringClass;
    private final TypeVariableName mTypeVariableT;


    public EtilMapperGenerator() {
        mCursorClass = ClassName.get("android.database", "Cursor");
        mContentValuesClass = ClassName.get("android.content", "ContentValues");
        mTypeVariableT = TypeVariableName.get("T");
        mStringClass = ClassName.get("java.lang", "String");
    }

    public void add(com.tractive.android.etil.compiler.data.EtilTableAnnotatedClass _annotatedClass) {
        mEtilTableClasses.add(_annotatedClass);

    }

    private MethodSpec generateMapCursorToModelMethod() {
        MethodSpec.Builder cursorToModelMethod = MethodSpec.methodBuilder("mapCursorToModel")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(AnnotationSpec
                        .builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())

                .returns(mTypeVariableT)
                .addTypeVariable(mTypeVariableT)
                .addParameter(ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), mTypeVariableT), "_class")
                .addParameter(mCursorClass, "_cursor")
                .beginControlFlow("switch (_class.getSimpleName())");

        for (EtilTableAnnotatedClass etilTableClass : mEtilTableClasses) {
            cursorToModelMethod.addCode("case $S:\n", etilTableClass.getSimpleTypeName())
                    .addStatement("return (T) cursorTo" + etilTableClass.getSimpleTypeName() + "(_cursor)");
        }

        cursorToModelMethod.addCode("default:\n")
                .addStatement("throw new java.lang.IllegalArgumentException($S)", "Model is not defined via annotations")
                .endControlFlow();

        return cursorToModelMethod.build();
    }

    private MethodSpec generateGetTableFromModelClassMethod() {
        MethodSpec.Builder getTableFromModel = MethodSpec.methodBuilder("getTableNameFromModelClass")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(AnnotationSpec
                        .builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())

                .returns(mStringClass)
                .addTypeVariable(mTypeVariableT)
                .addParameter(ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), mTypeVariableT), "_modelClass")
                .beginControlFlow("switch (_modelClass.getSimpleName())");

        for (EtilTableAnnotatedClass etilTableClass : mEtilTableClasses) {
            getTableFromModel.addCode("case $S:\n", etilTableClass.getSimpleTypeName())
                    .addStatement("return $S", etilTableClass.getTableName());
        }

        getTableFromModel.addCode("default:\n")
                .addStatement("throw new java.lang.IllegalArgumentException($S)", "Model is not defined via annotations")
                .endControlFlow();

        return getTableFromModel.build();
    }

    private MethodSpec generateGetTableFromModelMethod() {
        MethodSpec.Builder getTableFromModel = MethodSpec.methodBuilder("getTableNameFromModel")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(AnnotationSpec
                        .builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())

                .returns(mStringClass)
                .addTypeVariable(mTypeVariableT)
                .addParameter(mTypeVariableT, "_model")
                .addStatement("return getTableNameFromModelClass(_model.getClass())");
        return getTableFromModel.build();
    }

    private MethodSpec generateMapModelToContentValuesMethod() {
        MethodSpec.Builder modelToContentValuesMethod = MethodSpec.methodBuilder("mapModelToContentValues")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(mTypeVariableT)
                .addParameter(mTypeVariableT, "_model")
                .returns(mContentValuesClass)
                .beginControlFlow("switch (_model.getClass().getSimpleName())");

        for (EtilTableAnnotatedClass etilTableClass : mEtilTableClasses) {
            modelToContentValuesMethod.addCode("case \"" + etilTableClass.getSimpleTypeName() + "\":\n")
                    .addStatement("return " + decapitalize(etilTableClass.getSimpleTypeName()) + "ToContentValues(_model)");
        }

        modelToContentValuesMethod.addCode("default:\n")
                .addStatement("throw new java.lang.IllegalArgumentException($S)", "Model is not defined via annotations")
                .endControlFlow();

        return modelToContentValuesMethod.build();


    }


    public void generateCode(Filer _filer) throws IOException {
        if (mEtilTableClasses.size() == 0) {
            return;
        }

        TypeSpec helloWorld = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(generateMapCursorToModelMethod())
                .addMethod(generateMapModelToContentValuesMethod())
                .addMethod(generateGetTableFromModelMethod())
                .addMethod(generateGetTableFromModelClassMethod())
                .addMethods(generateCursorToModelMethods())
                .addMethods(generateModelToContentValuesMethods())
                .build();

        JavaFile javaFile = JavaFile.builder("com.tractive.android.etil", helloWorld)
                .build();

        javaFile.writeTo(_filer);

    }


    private Iterable<MethodSpec> generateModelToContentValuesMethods() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (EtilTableAnnotatedClass _etilTableClass : mEtilTableClasses) {

            MethodSpec.Builder builder = MethodSpec
                    .methodBuilder(decapitalize(_etilTableClass.getSimpleTypeName()) + "ToContentValues")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addTypeVariable(mTypeVariableT)
                    .addParameter(mTypeVariableT, "_model")
                    .addStatement("$L contentValues = new $L()", mContentValuesClass, mContentValuesClass)
                    .addStatement("$L model =  ($L) _model", _etilTableClass.getTypeElement(), _etilTableClass.getTypeElement());

            for (EtilTableAnnotatedClass.FieldAndColumnInfo _info : _etilTableClass.getFieldAndColumnInfo()) {
                if (_info.columnName.equals("_id")) {
                    continue;
                }
                if (_info.fieldType.equals("boolean") || _info.fieldType.equals("java.lang.boolean")) {
                    builder.addStatement("contentValues.put($S, model." + _info.fieldName + " ? 1 : 0)", _info.columnName);
                } else {
                    builder.addStatement("contentValues.put($S, model." + _info.fieldName + ")", _info.columnName);
                }
            }

            builder.returns(mContentValuesClass)
                    .addStatement("return contentValues");
            methodSpecs.add(builder.build());
        }

        return methodSpecs;
    }

    private Iterable<MethodSpec> generateCursorToModelMethods() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (EtilTableAnnotatedClass _etilTableClass : mEtilTableClasses) {

            MethodSpec.Builder builder = MethodSpec
                    .methodBuilder("cursorTo" + _etilTableClass.getSimpleTypeName())
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addParameter(mCursorClass, "_cursor")
                    .addStatement("$L model = new $L()", _etilTableClass.getTypeElement(), _etilTableClass.getTypeElement());

            for (com.tractive.android.etil.compiler.data.EtilTableAnnotatedClass.FieldAndColumnInfo _info : _etilTableClass.getFieldAndColumnInfo()) {
                if (_info.fieldType.equals("boolean") || _info.fieldType.equals("java.lang.boolean")) {
                    builder.addStatement("model." + _info.fieldName + " = " + "_cursor.getInt(_cursor.getColumnIndex(\"" + _info.columnName + "\")) != 0");
                } else {
                    builder.addStatement(
                            "model." + _info.fieldName + " = " + "_cursor." + _info.accessMethod + "(_cursor.getColumnIndex(\"" + _info.columnName + "\"))");
                }
            }

            builder.returns(ClassName.get(_etilTableClass.getTypeElement()))
                    .addStatement("return model");
            methodSpecs.add(builder.build());

        }

        return methodSpecs;
    }

    private static String decapitalize(String _string) {
        if (_string == null || _string.length() == 0) {
            return _string;
        }
        char c[] = _string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }


}
