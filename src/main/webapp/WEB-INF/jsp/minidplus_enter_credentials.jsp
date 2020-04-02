<%@ page import="no.idporten.ui.impl.IDPortenFeedbackType" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="header.jsp" />

<main id="minidplus-main">
    <section class="Box">

        <div class="Box_main" id="minidpluswrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot active"></div>
                <div class="fm-Progress_Dot"></div>
            </div>

            <c:set var ="warningCode" scope = "session" value='<%= request.getSession().getAttribute("idporten.feedback.WARNING") %>'/>

            <c:if test="${warningCode != null}">
                <div class="notification notification-error with-Icon icon-error">
                    <spring:message code="${warningCode}" text="Feil input"/>
                </div>
            </c:if>

            <form action="#" class="login" method="post">
                    <fieldset>
                        <div class="fm-Fields">
                            <LABEL>Well, hello there, partner!</LABEL>
                        </div>
                    </fieldset>
                </form>

        </div>
    </section>
</main>

