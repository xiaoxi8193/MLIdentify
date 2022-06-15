package com.ys.identify.processor.gcr;

import com.ys.identify.entity.GeneralCardResult;

/**
 * Common interface for post-processing plugins
 */
public interface GeneralCardProcessor {
    /**
     * Get recognition result
     * @return result
     */
    GeneralCardResult getResult();
}
