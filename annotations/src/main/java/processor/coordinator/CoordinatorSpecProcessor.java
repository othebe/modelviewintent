package processor.coordinator;

import com.squareup.javapoet.*;
import processor.ISpecProcessor;
import processor.Spec;
import processor.Utils;
import processor.actiongenerator.annotations.ActionGeneratorSpec;
import processor.coordinator.annotations.CoordinatorSpec;
import processor.coordinator.annotations.CoordinatorSpecGroup;
import processor.modelrenderer.annotations.ModelRendererSpec;
import processor.statemapper.annotations.StateMapperSpec;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.Map;

public class CoordinatorSpecProcessor implements ISpecProcessor {
    private final static String GENERATED_CLASSNAME_POSTFIX = "Coordinator";

    private final Map<String, Spec> specs;

    public CoordinatorSpecProcessor() {
        this.specs = new HashMap<>();
    }

    @Override
    public boolean canProcess(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(CoordinatorSpec.class.getCanonicalName());
    }

    @Override
    public void process(Element element, ProcessingEnvironment processingEnv) {
        CoordinatorSpec annotation = element.getAnnotation(CoordinatorSpec.class);

        PackageElement sourcePackageElement = processingEnv.getElementUtils().getPackageOf(element);
        String generatedClassName = annotation.generatedClassName();
        String fullyQualifiedName = Utils.getFullyQualifiedName(sourcePackageElement, generatedClassName);

        Spec spec = new Spec(sourcePackageElement, generatedClassName);

        FieldSpec subscriptionMap = getFieldSpec_subscriptionMap();
        spec.add(subscriptionMap);

        for (CoordinatorSpecGroup group: annotation.groups()) {
            TypeMirror actionGeneratorMirror = null;
            TypeMirror stateMapperMirror = null;
            TypeMirror modelRendererMirror = null;

            try {
                group.actionGeneratorSpec();
            } catch (MirroredTypeException e) {
                actionGeneratorMirror = e.getTypeMirror();
            }

            try {
                group.stateMapperSpec();
            } catch (MirroredTypeException e) {
                stateMapperMirror = e.getTypeMirror();
            }

            try {
                group.modelRendererSpec();
            } catch (MirroredTypeException e) {
                modelRendererMirror = e.getTypeMirror();
            }

            spec.add(getMethodSpec_listen(actionGeneratorMirror, stateMapperMirror, modelRendererMirror, subscriptionMap, processingEnv));
        }

        specs.put(fullyQualifiedName, spec);
    }

    @Override
    public Map<String, Spec> getSpecs() {
        return specs;
    }

    private FieldSpec getFieldSpec_subscriptionMap() {
        ParameterizedTypeName Map_IActionGenerator_Subscription = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get("core", "IActionGenerator"), TypeName.get(Subscription.class));

        return FieldSpec.builder(Map_IActionGenerator_Subscription, "subscriptionMap", Modifier.PRIVATE)
                .initializer("new $T()", HashMap.class)
                .build();
    }

    private MethodSpec getMethodSpec_listen(TypeMirror actionGeneratorMirror, TypeMirror stateMapperMirror, TypeMirror modelRendererMirror, FieldSpec subscriptionMap, ProcessingEnvironment processingEnv) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();

        Element actionGeneratorElement = typeUtils.asElement(actionGeneratorMirror);
        String actionGeneratorClassName = typeUtils.asElement(actionGeneratorMirror).getAnnotation(ActionGeneratorSpec.class).generatedClassName();
        String actionGeneratorPackageName = elementUtils.getPackageOf(actionGeneratorElement).getQualifiedName().toString();

        Element stateMapperElement = typeUtils.asElement(stateMapperMirror);
        String stateMapperClassName = typeUtils.asElement(stateMapperMirror).getAnnotation(StateMapperSpec.class).generatedClassName();
        String stateMapperPackageName = elementUtils.getPackageOf(stateMapperElement).getQualifiedName().toString();

        Element modelRendererElement = typeUtils.asElement(modelRendererMirror);
        String modelRendererClassName = typeUtils.asElement(modelRendererMirror).getAnnotation(ModelRendererSpec.class).generatedClassName();
        String modelRendererPackageName = elementUtils.getPackageOf(modelRendererElement).getQualifiedName().toString();

        ModelRendererSpec modelRendererSpec = modelRendererElement.getAnnotation(ModelRendererSpec.class);
        TypeMirror modelClassMirror = null;
        try {
            modelRendererSpec.modelClass();
        } catch (MirroredTypeException e) {
            modelClassMirror = e.getTypeMirror();
        }

        ParameterSpec actionGenerator = ParameterSpec.builder(ClassName.get(actionGeneratorPackageName, actionGeneratorClassName), "actionGenerator").build();
        ParameterSpec stateMapper = ParameterSpec.builder(ClassName.get(stateMapperPackageName, stateMapperClassName), "stateMapper").build();
        ParameterSpec modelRenderer = ParameterSpec.builder(ClassName.get(modelRendererPackageName, modelRendererClassName), "modelRenderer").build();
        ParameterSpec action = ParameterSpec.builder(String.class, "action").build();
        ParameterSpec state = ParameterSpec.builder(TypeName.get(modelClassMirror), "state").build();
        ParameterSpec throwable = ParameterSpec.builder(TypeName.get(Throwable.class), "throwable").build();

        ParameterizedTypeName Func1_ActionClass_ModelClass = ParameterizedTypeName.get(ClassName.get(Func1.class), TypeName.get(String.class), TypeName.get(modelClassMirror));
        ParameterizedTypeName Action1_ModelClass = ParameterizedTypeName.get(ClassName.get(Action1.class), TypeName.get(modelClassMirror));
        ParameterizedTypeName Action1_Throwable = ParameterizedTypeName.get(ClassName.get(Action1.class), TypeName.get(Throwable.class));

        FieldSpec subscription = FieldSpec.builder(Subscription.class, "subscription").build();

        return MethodSpec.methodBuilder("listen")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(actionGenerator)
                .addParameter(stateMapper)
                .addParameter(modelRenderer)
                .returns(Subscription.class)
                .addStatement("$T $N = $N.getActionObservable().map($L).subscribe($L, $L)",
                        Subscription.class,
                        subscription,
                        actionGenerator,
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(Func1_ActionClass_ModelClass)
                                .addMethod(MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(TypeName.get(modelClassMirror))
                                        .addParameter(action)
                                        .addStatement("return $N.mapActionToState($N)", stateMapper, action)
                                        .build())
                                .build(),
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(Action1_ModelClass)
                                .addMethod(MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(state)
                                        .returns(void.class)
                                        .addStatement("$N.render($N)", modelRenderer, state)
                                        .build())
                                .build(),
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(Action1_Throwable)
                                .addMethod(MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(throwable)
                                        .returns(void.class)
                                        .addStatement("throw new $T($N)", RuntimeException.class, throwable)
                                        .build())
                                .build())
//                .addStatement("$N.put($N, $N)", subscriptionMap, subscriptionMap, subscription)
                .addStatement("return $N", subscription)
                .build();
    }
}
