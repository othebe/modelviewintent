package processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Map;

public interface ISpecProcessor {
    boolean canProcess(TypeElement typeElement);
    void process(Element element, ProcessingEnvironment processingEnv);

    Map<String, Spec> getSpecs();
}
