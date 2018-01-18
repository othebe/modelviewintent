package processor;

import javax.lang.model.element.PackageElement;

public class Utils {
    public static final String getFullyQualifiedName(PackageElement packageElement, String className) {
        return packageElement.getQualifiedName().toString() + "." + className;
    }
}
