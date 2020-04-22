<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="sections/header.jsp" />

<jsp:include page="sections/minidplusheader.jsp"/>

<main id="minidplus-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp" />
        <div class="Box_main" id="minidpluswrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot active"></div>
                <div class="fm-Progress_Dot"></div>
            </div>


            <form:form id="complete" action="#" modelAttribute="oneTimePassword" class="login js-makeProgress-2" method="post">
                <form:errors path="*" class="notification notification-error with-Icon icon-error" element="div"/>
                <c:set var="otpCodeHasBindError">
                    <form:errors path="otpCode"/>
                </c:set>
                    <fieldset>
                        <div class="fm-Fields">
                            <spring:message code='auth.ui.inputhelp.onetimecode'
                                            text='Skriv inn pinkode' var="pincodeHelpText"/>

                            <div class="fm-Field ${not empty otpCodeHasBindError ? ' error' : ''}">
                                <label for="otpCode"><spring:message code="auth.ui.prompt.otc" text="Kode fra SMS"/></label>
                                <form:input tabindex="1"
                                        maxlength="5"
                                        path="otpCode"
                                        type="tel"
                                        id="otpCode"
                                        placeholder="${pincodeHelpText}"
                                        value="${otpCode}"
                                        autocomplete="off" />
                            </div>

                        </div>
                    </fieldset>
                <jsp:include page="sections/controls.jsp"/>
                </form:form>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>