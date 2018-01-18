package processor.statemapper;

import com.squareup.javapoet.*;
import processor.ISpecProcessor;
import processor.Spec;
import processor.Utils;
import processor.statemapper.annotations.ActionHandler;
import processor.statemapper.annotations.StateMapperSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class StateMapperSpecProcessor implements ISpecProcessor {
    private final Map<String, Spec> specs;

    public StateMapperSpecProcessor() {
        this.specs = new HashMap<>();
    }

    @Override
    public boolean canProcess(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(StateMapperSpec.class.getCanonicalName());
    }

    @Override
    public void process(Element element, ProcessingEnvironment processingEnv) {
        StateMapperSpec annotation = element.getAnnotation(StateMapperSpec.class);

        PackageElement sourcePackageElement = processingEnv.getElementUtils().getPackageOf(element);
        String generatedClassName = annotation.generatedClassName();
        String fullyQualifiedName = Utils.getFullyQualifiedName(sourcePackageElement, generatedClassName);

        Spec spec = new Spec(sourcePackageElement, generatedClassName);

        TypeMirror modelMirror = null;
        try {
            annotation.modelClass();
        } catch (MirroredTypeException e) {
            modelMirror = e.getTypeMirror();
        }

        spec.add(
                ParameterizedTypeName.get(ClassName.get("core", "IStateMapper"), TypeName.get(String.class), TypeName.get(modelMirror)));

        FieldSpec base = getFieldSpec_base(element, false);
        spec.add(base);

        MethodSpec mapActionToState = getMethodSpec_mapActionToState(element, modelMirror, base);
        spec.add(mapActionToState);

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

    /** TODO (othebe): Read ActionGeneratorSpec for all available actions. */
    private MethodSpec getMethodSpec_mapActionToState(Element element, TypeMirror modelMirror, FieldSpec base) {
        ParameterSpec action = ParameterSpec.builder(String.class, "action")
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("mapActionToState")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(modelMirror))
                .addParameter(action);

        for (Element enclosedElement : element.getEnclosedElements()) {
            ActionHandler actionHandler = enclosedElement.getAnnotation(ActionHandler.class);
            if (actionHandler != null) {
                String annotationAction = actionHandler.value();
                builder.beginControlFlow("if ($N.equals($S))", action, annotationAction);
                builder.addStatement("return $N." + enclosedElement.toString(), base);
                builder.endControlFlow();
            }
        }

        builder.addStatement("throw new $T($S)", RuntimeException.class, "Unhandled action type");

        return builder.build();
    }
}
