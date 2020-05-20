<%@ page import="no.idporten.minidplus.util.MinIdPlusButtonType" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="input" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="sections/header.jsp"/>

<jsp:include page="sections/minidplusheader.jsp"/>

<main id="minidpluss-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp"/>
        <div id="minidplusswrapper">
            <div class="notification notification-error with-Icon icon-error">
                <p><spring:message code="no.idporten.module.minidplus.invalidacr.info1"/></p>
                <form:form action="authorize" id="complete" class="login"
                           method="post">
                    <p>
                        <button class='btn btn-Action' tabindex="1" autofocus="autofocus"
                                id="<%= MinIdPlusButtonType.CANCEL.id() %>"
                                name="<%= MinIdPlusButtonType.CANCEL.id() %>">
                            <span><spring:message code="no.idporten.module.minidplus.invalidacr.linktext1"
                                                  arguments="${sessionScope.serviceprovider.name}"
                                                  htmlEscape="false"/></span>
                        </button>
                    </p>

                </form:form>
                <p><spring:message code="no.idporten.module.minidplus.invalidacr.info2"/></p>
                <p><a tabindex="2" href="<spring:message code='no.idporten.module.minidplus.registrationpage'/>">
                    <spring:message
                            code="no.idporten.module.minidplus.invalidacr.linktext2"/></a></p>
            </div>
        </div>
    </section>
</main>
<jsp:include page="sections/footer.jsp"/>

