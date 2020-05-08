package no.idporten.minidplus.validator;

import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class InputTerminator {

    public static void clearAllInput(Object target, BindingResult result, Model model) {
        BeanPropertyBindingResult result2 = new BeanPropertyBindingResult(target, result.getObjectName());
        for (ObjectError error : result.getGlobalErrors()) {
            result2.addError(error);
        }
        for (FieldError error : result.getFieldErrors()) {
            result2.addError(new FieldError(error.getObjectName(), error.getField(), null, error.isBindingFailure(), error.getCodes(), error.getArguments(), error.getDefaultMessage()));
        }

        model.addAllAttributes(result2.getModel());
    }
}
