package io.crnk.meta.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MetaAttributePath implements Iterable<MetaAttribute> {

    public static final char PATH_SEPARATOR_CHAR = '.';

    public static final String PATH_SEPARATOR = ".";

    public static final MetaAttributePath EMPTY_PATH = new MetaAttributePath();

    private MetaAttribute[] pathElements;

    public MetaAttributePath(List<? extends MetaAttribute> pathElements) {
        this(pathElements.toArray(new MetaAttribute[pathElements.size()]));
    }

    public MetaAttributePath(MetaAttribute... pathElements) {
        if (pathElements == null) {
            throw new IllegalArgumentException("pathElements must not be null.");
        }
        this.pathElements = pathElements;
    }

    public MetaAttributePath subPath(int startIndex) {
        MetaAttribute[] range = Arrays.copyOfRange(pathElements, startIndex, pathElements.length);
        return new MetaAttributePath(range);
    }

    public MetaAttributePath subPath(int startIndex, int endIndex) {
        MetaAttribute[] range = Arrays.copyOfRange(pathElements, startIndex, endIndex);
        return new MetaAttributePath(range);
    }

    protected MetaAttribute[] newArray(int length) {
        return new MetaAttribute[length];
    }

    protected MetaAttributePath to(MetaAttribute... pathElements) {
        return new MetaAttributePath(pathElements);
    }

    public int length() {
        return pathElements.length;
    }

    public MetaAttribute getElement(int index) {
        return pathElements[index];
    }

    public MetaAttribute getLast() {
        if (pathElements != null && pathElements.length > 0) {
            return pathElements[pathElements.length - 1];
        }
        return null;
    }

    public MetaAttributePath concat(MetaAttribute... pathElements) {
        ArrayList<MetaAttribute> list = new ArrayList<>();
        list.addAll(Arrays.asList(this.pathElements));
        list.addAll(Arrays.asList(pathElements));
        return to(list.toArray(newArray(0)));
    }

    public String render(String delimiter) {
        if (pathElements.length == 0) {
            return "";
        } else if (pathElements.length == 1) {
            return pathElements[0].getName();
        } else {
            StringBuilder builder = new StringBuilder(pathElements[0].getName());
            for (int i = 1; i < pathElements.length; i++) {
                builder.append(delimiter);
                builder.append(pathElements[i].getName());
            }
            return builder.toString();
        }
    }

    @Override
    public String toString() {
        return render(PATH_SEPARATOR);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(pathElements);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaAttributePath) {
            MetaAttributePath other = (MetaAttributePath) obj;
            return Arrays.equals(pathElements, other.pathElements);
        }
        return false;
    }

    @Override
    public Iterator<MetaAttribute> iterator() {
        return Collections.unmodifiableList(Arrays.asList(pathElements)).iterator();
    }
}
