package org.cdpg.dx.common.util;

import org.cdpg.dx.common.exception.*;

public class ThrowableUtils {

    private ThrowableUtils() {
        // Utility class, prevent instantiation
    }

    public static boolean isSafeToExpose(Throwable throwable) {
        return throwable instanceof IllegalArgumentException || throwable instanceof QueueAlreadyExistsException || throwable instanceof DxBadRequestException || throwable instanceof  DxUnauthorizedException;}
}
