<%@ page import="no.idporten.minidplus.util.MinidPlusButtonType" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!-- Controls placeholder with two buttons -->
<div class='fm-Controls with-Normal with-Action'>
    <button class='btn btn-Action' tabindex="10" id="<%= MinidPlusButtonType.NEXT.id() %>"
            name="<%= MinidPlusButtonType.NEXT.id() %>"
            type='submit'><span><spring:message code="no.idporten.button.next" text="Neste"/></span>
    </button>
    <button class='btn btn-Normal' tabindex="11" id="<%= MinidPlusButtonType.CANCEL.id() %>"
            name="<%= MinidPlusButtonType.CANCEL.id() %>"
            type='submit'><span><spring:message code="auth.ui.button.cancel" text="Avbryt"/></span>
    </button>
</div>
<!-- //Controls placeholder with two buttons -->
