package com.tractive.android.etil.compiler.data;

import javax.lang.model.element.TypeElement;

/**
 * Created by stephan on 25/08/16.
 */
public class MultiSingleEtilTableAnnotatedClass extends EtilTableAnnotatedClass {
    public MultiSingleEtilTableAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {
        super(classElement);
    }
}
