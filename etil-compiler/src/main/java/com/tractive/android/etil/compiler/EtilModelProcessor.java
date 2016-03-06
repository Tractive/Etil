package com.tractive.android.etil.compiler;

import com.google.auto.service.AutoService;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by stephan on 03/03/16.
 */
@AutoService(Processor.class)
public class EtilModelProcessor extends AbstractProcessor {

    private Messager messager;


    private Elements elementUtils;
    private Filer filer;


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(EtilTable.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        HashMap<String, EtilTableAnnotatedClass> etilTableClasses = new HashMap<>();
        EtilTableClasses classes = new EtilTableClasses();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(EtilTable.class)) {
            TypeElement typeElement = (TypeElement) annotatedElement;
            try {
                EtilTableAnnotatedClass annotatedClass = new EtilTableAnnotatedClass(typeElement);

                if (!isValidClass(annotatedClass)) {
                    return true;
                }

                etilTableClasses.put(typeElement.getSimpleName().toString(), annotatedClass);
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

            EtilTableAnnotatedClass table = etilTableClasses.get(variableElement.getEnclosingElement().getSimpleName().toString());

            if (table == null) {
                error(annotatedMember, "Model is missing EtilTable annotation");
                return true;
            }

            try {
                table.addField(variableElement);

            } catch (IllegalArgumentException e) {
                error(null, e.getMessage());

            }

        }

        try {
            classes.generateCode(elementUtils, filer);
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
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    private boolean isValidClass(EtilTableAnnotatedClass item) {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.", classElement.getQualifiedName().toString());
            return false;
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(), EtilTable.class.getSimpleName());
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
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(_message, args), _element);
    }
}
