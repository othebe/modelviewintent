package processor;

import processor.actiongenerator.annotations.ActionGeneratorSpec;
import com.google.auto.service.AutoService;
import processor.actiongenerator.ActionGeneratorSpecProcessor;
import processor.coordinator.CoordinatorSpecProcessor;
import processor.coordinator.annotations.CoordinatorSpec;
import processor.modelrenderer.ModelRendererSpecProcessor;
import processor.modelrenderer.annotations.ModelRendererSpec;
import processor.statemapper.StateMapperSpecProcessor;
import processor.statemapper.annotations.StateMapperSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MVIProcessor extends AbstractProcessor {
    private final ISpecProcessor[] specProcessors = {
            new ActionGeneratorSpecProcessor(),
            new StateMapperSpecProcessor(),
            new ModelRendererSpecProcessor(),
            new CoordinatorSpecProcessor()
    };

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(ActionGeneratorSpec.class.getCanonicalName());
        types.add(StateMapperSpec.class.getCanonicalName());
        types.add(ModelRendererSpec.class.getCanonicalName());
        types.add(CoordinatorSpec.class.getCanonicalName());

        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            generateFiles();
        } else {
            for (TypeElement typeElement : annotations) {
                for (ISpecProcessor specProcessor : specProcessors) {
                    if (specProcessor.canProcess(typeElement)) {
                        for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                            specProcessor.process(element, processingEnv);
                        }
                    }
                }
            }
        }

        return false;
    }

    /** TODO (othebe): Allow for multiple specs to share the same file */
    private void generateFiles() {
        for (ISpecProcessor specProcessor : specProcessors) {
            for (Spec spec : specProcessor.getSpecs().values()) {
                try {
                    spec.buildJavaFile().writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
                }
            }
        }
    }
}
