package processor.modelrenderer;

import com.squareup.javapoet.*;
import processor.ISpecProcessor;
import processor.Spec;
import processor.Utils;
import processor.modelrenderer.annotations.ModelRendererSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

public class ModelRendererSpecProcessor implements ISpecProcessor {
    private final Map<String, Spec> specs;

    public ModelRendererSpecProcessor() {
        this.specs = new HashMap<>();
    }

    @Override
    public boolean canProcess(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(ModelRendererSpec.class.getCanonicalName());
    }

    @Override
    public void process(Element element, ProcessingEnvironment processingEnv) {
        ModelRendererSpec annotation = element.getAnnotation(ModelRendererSpec.class);

        PackageElement sourcePackageElement = processingEnv.getElementUtils().getPackageOf(element);
        String generatedClassName = annotation.generatedClassName();
        String fullyQualifiedName = Utils.getFullyQualifiedName(sourcePackageElement, generatedClassName);

        TypeMirror modelMirror = null;
        try {
            annotation.modelClass();
        } catch (MirroredTypeException e) {
            modelMirror = e.getTypeMirror();
        }

        Spec spec = new Spec(sourcePackageElement, generatedClassName);
        spec.add(ParameterizedTypeName.get(ClassName.get("core", "IModelRenderer"), TypeName.get(modelMirror)));

        FieldSpec base = getFieldSpec_base(element, false);
        spec.add(base);

        MethodSpec render = getMethodSpec_render(modelMirror, base);
        spec.add(render);

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

    private MethodSpec getMethodSpec_render(TypeMirror modelMirror, FieldSpec base) {
        ParameterSpec state = ParameterSpec.builder(TypeName.get(modelMirror), "state")
                .build();

        return MethodSpec.methodBuilder("render")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(state)
                .addStatement("$N.render($N)", base, state)
                .build();
    }
}
