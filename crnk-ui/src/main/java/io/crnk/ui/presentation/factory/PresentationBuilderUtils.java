package io.crnk.ui.presentation.factory;

import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.ui.presentation.element.ContainerElement;
import io.crnk.ui.presentation.element.PresentationElement;
import io.crnk.ui.presentation.element.SingularValueElement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PresentationBuilderUtils {

    public static String getLabel(ArrayDeque<MetaAttribute> attributePath) {
        return attributePath.stream().map(it -> it.getName()).collect(Collectors.joining("."));
    }

    public static List<PathSpec> computeIncludes(PresentationElement element) {
        return computeIncludes(Arrays.asList(element));
    }

    public static List<PathSpec> computeIncludes(List<PresentationElement> elements) {
        Set<PathSpec> includes = new HashSet<>();
        for (PresentationElement element : elements) {
            if (element instanceof SingularValueElement) {
                SingularValueElement valueElement = (SingularValueElement) element;
                Optional<PathSpec> optInclude = toInclude(valueElement.getAttributePath());
                if (optInclude.isPresent()) {
                    PathSpec include = optInclude.get();
                    includes.add(include);
                    if (element instanceof ContainerElement) {
                        List<PresentationElement> childElements = ((ContainerElement) element).getChildren();
                        List<PathSpec> nestedIncludes = computeIncludes(childElements);
                        nestedIncludes.forEach(nestedInclude -> includes.add(concat(include, nestedInclude)));
                    }
                }
            }
        }
        return new ArrayList<>(includes);
    }

    private static PathSpec concat(PathSpec path1, PathSpec path2) {
        List<String> elements = new ArrayList<>();
        elements.addAll(path1.getElements());
        elements.addAll(path2.getElements());
        return PathSpec.of(elements);
    }

    private static Optional<PathSpec> toInclude(PathSpec attributePath) {
        if (attributePath != null) {
            return Optional.of(attributePath);
        }
        return Optional.empty();

    }

    /*

    private toIncludePath(attributePath: Array<string>) {
		const includePath = [];

        for (let i = 0; i < attributePath.length; i++) {
            if (attributePath[i] === 'attributes') {
                break;
            }
            else if (attributePath[i] === 'relationships' && i < attributePath.length - 2) {
				const relationshipName = attributePath[i + 1];
				const dataType = attributePath[i + 2];
                if (dataType === 'data' || dataType === 'reference') {
                    includePath.push(relationshipName);
                    i += 2;
                }
                else {
                    throw new Error('cannot map relationship path in ' + attributePath + ', got ' + dataType +
                            ' but expected data or reference');
                }
            }
            else {
                break;
            }
        }
        return _.join(includePath, '.');
    }
     */
}
