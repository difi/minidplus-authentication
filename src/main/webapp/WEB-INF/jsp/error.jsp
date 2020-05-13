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
                <spring:message code="no.idporten.module.minidplus.error.text1"/>
                <br><br><spring:message code="no.idporten.module.minidplus.error.text2"/>
                <c:if test="${not empty errorCode}">
                    <br><spring:message code="no.idporten.module.minidplus.error.text3"/> <c:out value="${errorCode}"
                                                                                                 escapeXml="true"/>
                </c:if>
                <form:errors path="*"/>
            </div>
            <c:if test="${not empty requestUrl}">
                <form:form method="get" action="${requestUrl}">
                    <c:forEach items="${params}" var="entry">
                        <input type="hidden" name="${entry.key}" value="${entry.value}"/>
                    </c:forEach>
                    <fieldset>
                        <div class="fm-Controls with-Action">
                            <button name="idporten.inputbutton.CLOSE" id="closeButton" tabindex="10"
                                    class="btn btn-Action">
                                <span><spring:message code="auth.ui.button.retry" text="Prøv på nytt"/></span>
                            </button>
                        </div>
                    </fieldset>
                </form:form>
            </c:if>
        </div>
    </section>
</main>
<jsp:include page="sections/footer.jsp"/>

