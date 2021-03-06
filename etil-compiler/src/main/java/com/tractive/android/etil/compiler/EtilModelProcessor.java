package com.tractive.android.etil.compiler;

import com.google.auto.service.AutoService;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;
import com.tractive.android.etil.annotations.MultiEtilTable;
import com.tractive.android.etil.compiler.data.EtilTableAnnotatedClass;
import com.tractive.android.etil.compiler.data.MultiSingleEtilTableAnnotatedClass;
import com.tractive.android.etil.compiler.data.SingleEtilTableAnnotatedClass;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
public class EtilModelProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;
    private Types mTypeUtils;


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(EtilTable.class.getCanonicalName(), MultiEtilTable.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        HashMap<String, SingleEtilTableAnnotatedClass> singleEtilTableClasses = new HashMap<>();
        HashMap<String, MultiSingleEtilTableAnnotatedClass> multiEtilTableClasses = new HashMap<>();
        HashMap<String, List<EtilTableAnnotatedClass.FieldAndColumnInfo>> additionalTableClasses = new HashMap<>();
        EtilMapperGenerator classes = new EtilMapperGenerator();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(EtilTable.class)) {
            TypeElement typeElement = (TypeElement) annotatedElement;
            try {
                SingleEtilTableAnnotatedClass
                        annotatedClass = new SingleEtilTableAnnotatedClass(typeElement);

                if (!isValidClass(annotatedClass)) {
                    return true;
                }

                singleEtilTableClasses.put(typeElement.getSimpleName().toString(), annotatedClass);
                classes.add(annotatedClass);
            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());
                return true;
            }
        }

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MultiEtilTable.class)) {
            TypeElement typeElement = (TypeElement) annotatedElement;
            try {
                MultiSingleEtilTableAnnotatedClass
                        annotatedClass = new MultiSingleEtilTableAnnotatedClass(typeElement);

                if (!isValidClass(annotatedClass)) {
                    return true;
                }

                multiEtilTableClasses.put(typeElement.getSimpleName().toString(), annotatedClass);
                classes.add(annotatedClass);
            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());
                return true;
            }
        }

        for (Element annotatedMember : roundEnv.getElementsAnnotatedWith(EtilField.class)) {

            VariableElement variableElement = (VariableElement) annotatedMember;

            if (!isValidMember(variableElement)) {
                error(annotatedMember, "Field is not public");
                return true;
            }

            String simpleNameClass = variableElement.getEnclosingElement().getSimpleName().toString();
            SingleEtilTableAnnotatedClass singleTable = singleEtilTableClasses.get(simpleNameClass);
            MultiSingleEtilTableAnnotatedClass multiTable = multiEtilTableClasses.get(simpleNameClass);

            try {
                if (singleTable == null && multiTable != null) {
                    multiTable.addField(variableElement);
                } else if (singleTable != null && multiTable == null) {
                    singleTable.addField(variableElement);
                } else {

                    List<EtilTableAnnotatedClass.FieldAndColumnInfo> fieldInfos = additionalTableClasses.get(simpleNameClass);

                    if (fieldInfos == null) {
                        fieldInfos = new ArrayList<>();
                        additionalTableClasses.put(simpleNameClass, fieldInfos);
                    }

                    fieldInfos.add(EtilTableAnnotatedClass.generateField(variableElement));
                }
            } catch (IllegalArgumentException e) {
                error(null, e.getMessage());
            }
        }

        for (Map.Entry<String, List<EtilTableAnnotatedClass.FieldAndColumnInfo>> additionalEntry : additionalTableClasses.entrySet()) {
            for (Map.Entry<String, SingleEtilTableAnnotatedClass> etilEntry : singleEtilTableClasses.entrySet()) {

                TypeMirror typeMirror = etilEntry.getValue().getTypeElement().getSuperclass();

                boolean foundSuperType = true;

                while (typeMirror != null && !typeMirror.toString().equals("java.lang.Object") && foundSuperType) {

                    String name = typeMirror.toString().substring(typeMirror.toString().lastIndexOf(".") + 1, typeMirror.toString().length());

                    if (additionalEntry.getKey().equals(name)) {
                        etilEntry.getValue().addFields(additionalEntry.getValue());
                    }

                    foundSuperType = false;

                    for (TypeMirror supertype : mTypeUtils.directSupertypes(typeMirror)) {
                        if (supertype instanceof DeclaredType && mTypeUtils.asElement(supertype).getKind() != ElementKind.INTERFACE) {
                            foundSuperType = true;
                            typeMirror = ((DeclaredType) supertype).asElement().asType();
                        }
                    }
                }
            }
        }

        try {
            classes.generateCode(mFiler);
            return true;
        } catch (IOException e) {
            error(null, e.getMessage());
        }

        return false;
    }

    private boolean isValidMember(Element _annotatedMember) {

        return _annotatedMember.getModifiers().contains(Modifier.PUBLIC);
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mTypeUtils = processingEnv.getTypeUtils();
    }

    private boolean isValidClass(com.tractive.android.etil.compiler.data.EtilTableAnnotatedClass item) {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.", classElement.getQualifiedName().toString());
            return false;
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @% or @%",
                    classElement.getQualifiedName().toString(), EtilTable.class.getSimpleName(), MultiEtilTable.class.getSimpleName());
            return false;
        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return true;
                }
            }
        }

        // No empty constructor found
        error(classElement, "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
        return false;
    }

    private void error(Element _element, String _message, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(_message, args), _element);
    }
}
