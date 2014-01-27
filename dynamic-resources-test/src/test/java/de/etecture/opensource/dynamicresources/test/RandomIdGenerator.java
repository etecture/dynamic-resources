package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.Generator;
import de.etecture.opensource.dynamicrepositories.api.Param;
import org.apache.commons.beanutils.ConvertUtils;

/**
 * The Class RandomIdGenerator.
 *
 * @author rhk
 * @version
 * @since
 */
public final class RandomIdGenerator implements Generator {

    /** The Constant DIGITS. */
    private static final char[] DIGITS = new char[]{'0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9'};

    public Object generateValue(Param definition) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(DIGITS[((Double) (Math.random() * DIGITS.length))
                    .intValue()]);
        }
        System.out.println("generated id: " + sb.toString());
        return ConvertUtils.convert(sb.toString(), definition.type());
    }
}
