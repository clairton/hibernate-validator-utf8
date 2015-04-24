package br.eti.clairton.validation;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

public class UTF8ResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {

    public UTF8ResourceBundleMessageInterpolator() {
        super(new UTF8ResourceBundleLocator(ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES));
    }
}
