package de.etecture.opensource.dynamicresources.extension;

import java.util.Comparator;

/**
 *
 * @author rhk
 */
public class IgnoreCaseStringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o2 == null) {
            return -1;
        } else {
            return o1.compareToIgnoreCase(o2);
        }
    }
}
