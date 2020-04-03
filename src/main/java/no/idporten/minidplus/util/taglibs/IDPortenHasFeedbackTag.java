package no.idporten.minidplus.util.taglibs;

import no.idporten.ui.impl.IDPortenFeedbackType;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag used to wrap around end user feedback messages in ID-porten.  
 */
public class IDPortenHasFeedbackTag extends TagSupport {

    private static final long serialVersionUID = 668211635526396329L;

    private transient IDPortenFeedbackType type;

    /**
     * Skips body if feedback type does not have any feedback. Else evals body.
     */
    @Override
    public int doStartTag() throws JspException {
        if (type == null) {
            return SKIP_BODY;
        }
        final String feedback = (String) pageContext.getSession().getAttribute("idporten.feedback." + type);
        if (StringUtils.isEmpty(feedback)) {
            return SKIP_BODY;
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Sets feedback type.
     *
     * @param type feedback type
     */
    public void setType(final IDPortenFeedbackType type) {
        this.type = type;
    }

}
