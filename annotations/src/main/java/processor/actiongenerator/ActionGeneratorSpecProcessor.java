package processor.actiongenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;

import processor.Spec;
import processor.Utils;
import processor.actiongenerator.annotations.ActionGeneratorSpec;
import com.squareup.javapoet.*;
import processor.ISpecProcessor;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

public class ActionGeneratorSpecProcessor implements ISpecProcessor {
    private final Map<String, Spec> specs;

    public ActionGeneratorSpecProcessor() {
        this.specs = new HashMap<>();
    }

    @Override
    public boolean canProcess(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(ActionGeneratorSpec.class.getCanonicalName());
    }

    @Override
    public void process(Element element, ProcessingEnvironment processingEnv) {
        ActionGeneratorSpec annotation = element.getAnnotation(ActionGeneratorSpec.class);

        PackageElement sourcePackageElement = processingEnv.getElementUtils().getPackageOf(element);
        String generatedClassName = annotation.generatedClassName();
        String fullyQualifiedName = Utils.getFullyQualifiedName(sourcePackageElement, generatedClassName);

        Spec spec = new Spec(sourcePackageElement, generatedClassName);
        spec.add(ParameterizedTypeName.get(ClassName.get("core", "IActionGenerator"), TypeName.get(String.class)));

        FieldSpec base = getFieldSpec_base(element, false);
        spec.add(base);

        FieldSpec emitActionFunc = getFieldSpec_emitActionFunc();
        spec.add(emitActionFunc);

        FieldSpec actionObservable = getFieldSpec_actionObservable(emitActionFunc);
        spec.add(actionObservable);

        MethodSpec getActionObservable = getMethodSpec_getActionObservable(actionObservable);
        spec.add(getActionObservable);

        MethodSpec triggerAction = getMethodSpec_triggerAction(emitActionFunc);
        spec.add(triggerAction);

        specs.put(fullyQualifiedName, spec);
    }

    @Override
    public Map<String, Spec> getSpecs() {
        return specs;
    }

    private FieldSpec getFieldSpec_base(Element base, boolean isInjected) {
        return FieldSpec.builder(TypeName.get(base.asType()), "base", Modifier.PRIVATE)
                .initializer("new $T()", TypeName.get(base.asType()))
                .build();
    }

    private FieldSpec getFieldSpec_actionObservable(FieldSpec emitActionFunc) {
        ParameterizedTypeName Observable_ActionClass = ParameterizedTypeName.get(ClassName.get(Observable.class), TypeName.get(String.class));
        ParameterizedTypeName Emitter_ActionClass = ParameterizedTypeName.get(ClassName.get(Emitter.class), TypeName.get(String.class));
        ParameterizedTypeName Action1_Emitter_ActionClass = ParameterizedTypeName.get(ClassName.get(Action1.class), Emitter_ActionClass);
        ParameterizedTypeName Func1_ActionClass_Void = ParameterizedTypeName.get(ClassName.get(Func1.class), TypeName.get(String.class), TypeName.VOID.box());


        return FieldSpec.builder(Observable_ActionClass, "actionObservable", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$T.create($L, $T.NONE).doOnUnsubscribe($L).share()",
                        Observable.class,
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(Action1_Emitter_ActionClass)
                                .addMethod(MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(void.class)
                                        .addParameter(Emitter_ActionClass, "emitter")
                                        .addStatement("$N = $L",
                                                emitActionFunc,
                                                TypeSpec.anonymousClassBuilder("")
                                                        .addSuperinterface(Func1_ActionClass_Void)
                                                        .addMethod(MethodSpec.methodBuilder("call")
                                                                .addAnnotation(Override.class)
                                                                .addModifiers(Modifier.PUBLIC)
                                                                .returns(TypeName.VOID.box())
                                                                .addParameter(TypeName.get(String.class), "action")
                                                                .addStatement("emitter.onNext(action)")
                                                                .addStatement("return null")
                                                                .build())
                                                        .build())
                                        .build())
                                .build(),
                        Emitter.BackpressureMode.class,
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(Action0.class)
                                .addMethod(MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(void.class)
                                        .addStatement("$N = null", emitActionFunc)
                                        .build())
                                .build())
                .build();
    }

    private FieldSpec getFieldSpec_emitActionFunc() {
        ParameterizedTypeName Func1_ActionClass_Void = ParameterizedTypeName.get(ClassName.get(Func1.class), TypeName.get(String.class), TypeName.VOID.box());

        return FieldSpec.builder(Func1_ActionClass_Void, "emitActionFunc", Modifier.PRIVATE)
                .build();
    }

    private MethodSpec getMethodSpec_getActionObservable(FieldSpec actionObservable) {
        ParameterizedTypeName Observable_ActionClass = ParameterizedTypeName.get(ClassName.get(Observable.class), TypeName.get(String.class));

        return MethodSpec.methodBuilder("getActionObservable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(Observable_ActionClass)
                .addStatement("return $N", actionObservable)
                .build();
    }

    private MethodSpec getMethodSpec_triggerAction(FieldSpec emitActionFunc) {
        return MethodSpec.methodBuilder("triggerAction")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.get(String.class), "action")
                .beginControlFlow("if ($N != null)", emitActionFunc)
                .addStatement("$N.call(action)", emitActionFunc)
                .endControlFlow()
                .build();
    }
}
