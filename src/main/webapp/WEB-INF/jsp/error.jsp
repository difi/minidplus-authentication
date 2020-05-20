<%@ page import="no.idporten.minidplus.util.MinIdPlusButtonType" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="sections/header.jsp"/>

<jsp:include page="sections/minidplusheader.jsp"/>

<main id="minidpluss-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp"/>
        <div id="minidplusswrapper">
            <div class="notification notification-error with-Icon icon-error">
                <p><spring:message code="no.idporten.module.minidplus.error.text1"/></p>
                <p><spring:message code="no.idporten.module.minidplus.error.text2"/></p>
                <c:if test="${errorMsg!=null}">
                <p><spring:message code="no.idporten.module.minidplus.error.text3"
                                   arguments="${errorMsg}"/></p>
                <p>
                    </c:if>
                    <form:form action="retry"
                               method="post">

                        <button class='btn btn-Action' tabindex="1"
                                id="<%= MinIdPlusButtonType.NEXT.id() %>"
                                autofocus="autofocus"
                                name="<%= MinIdPlusButtonType.NEXT.id() %>">
                            <span><spring:message code="auth.ui.button.retry" text="OK"/></span>
                        </button>

                    </form:form>
                </p>
            </div>

        </div>
    </section>
</main>
<jsp:include page="sections/footer.jsp"/>

