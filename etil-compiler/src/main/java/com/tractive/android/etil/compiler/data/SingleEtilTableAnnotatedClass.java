package com.tractive.android.etil.compiler.data;

import com.tractive.android.etil.annotations.EtilTable;

import javax.lang.model.element.TypeElement;

/**
 * Created by stephan on 25/08/16.
 */
public class SingleEtilTableAnnotatedClass extends EtilTableAnnotatedClass {
    private final String mTableName;

    public String getTableName() {
        return mTableName;
    }

    public SingleEtilTableAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {
        super(classElement);

        EtilTable annotation = classElement.getAnnotation(EtilTable.class);
        mTableName = annotation.value();

        if (mTableName == null || mTableName.equals("")) {
            throw new IllegalArgumentException(
                    String.format("value() in @%s for class %s is null or empty! that's not allowed",
                            EtilTable.class.getSimpleName(), classElement.getQualifiedName().toString()));
        }
    }
}
