package com.tractive.android.etil.compiler.data;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class EtilTableAnnotatedClass {


    public static FieldAndColumnInfo generateField(VariableElement _variableElement) {
        FieldAndColumnInfo info = new FieldAndColumnInfo();

        info.fieldName = _variableElement.getSimpleName().toString();
        info.columnName = _variableElement.getAnnotation(EtilField.class).value();
        info.fieldType = _variableElement.asType().toString().toLowerCase();

        switch (info.fieldType) {
            case "int":
            case "java.lang.integer":
                info.accessMethod = "getInt";

                break;
            case "java.lang.string":
                info.accessMethod = "getString";
                break;
            case "double":
            case "java.lang.double":
                info.accessMethod = "getDouble";
                break;
            case "boolean":
            case "java.lang.boolean":
                //getBoolean does not exist in sqlite -> http://stackoverflow.com/questions/4088080/get-boolean-from-database-using-android-and-sqlite.
                // That is why we handle the field in the EditTableClasses separately for booleans.
                info.accessMethod = "getInt";
                break;
            case "float":
            case "java.lang.float":
                info.accessMethod = "getFloat";
                break;
            case "long":
            case "java.lang.long":
                info.accessMethod = "getLong";
                break;
            default:
                throw new IllegalArgumentException("EtilField Type is not supported: " + info.fieldType);

        }

        return info;
    }

    public void addField(VariableElement _member) throws IllegalArgumentException {
        mFieldAndColumnInfo.add(generateField(_member));
    }



    public void addFields(List<FieldAndColumnInfo> _fields) {
        mFieldAndColumnInfo.addAll(_fields);
    }

    public static class FieldAndColumnInfo {

        public String fieldName;
        public String columnName;
        public String accessMethod;
        public String fieldType;
    }

    private String mTableName;
    private TypeElement annotatedClassElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;
    private List<FieldAndColumnInfo> mFieldAndColumnInfo = new ArrayList<>();

    public EtilTableAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {
        annotatedClassElement = classElement;
        EtilTable annotation = classElement.getAnnotation(EtilTable.class);
        mTableName = annotation.value();

        if (mTableName == null || mTableName.equals("")) {
            throw new IllegalArgumentException(
                    String.format("value() in @%s for class %s is null or empty! that's not allowed",
                            EtilTable.class.getSimpleName(), classElement.getQualifiedName().toString()));
        }

        try {

            qualifiedSuperClassName = annotatedClassElement.getQualifiedName().toString();
            simpleTypeName = annotatedClassElement.getSimpleName().toString();


        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }


    public String getTableName() {
        return mTableName;
    }

    public String getSimpleTypeName() {
        return simpleTypeName;
    }

    public List<FieldAndColumnInfo> getFieldAndColumnInfo() {
        return mFieldAndColumnInfo;
    }

    public String getQualifiedSuperClassName() {
        return qualifiedSuperClassName;
    }

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}

