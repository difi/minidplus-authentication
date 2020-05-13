<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="sections/header.jsp"/>

<jsp:include page="sections/minidplusheader.jsp"/>

<main id="minidplus-main">
    <section class="Box">
        <jsp:include page="sections/box-header-password.jsp"/>
        <div class="Box_main" id="minidpluswrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot active"></div>
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot"></div>
            </div>

            <div class="notification with-Icon icon-email">
                <p><spring:message code="no.idporten.module.minid.step3.otc.info"
                                   text="You will now receive a single-use code by Email from Digdir."/></p>
            </div>

            <c:set var="otpCodeHasError">
                <form:errors path="*"/>
            </c:set>

            <form:form id="complete" action="#" modelAttribute="oneTimePassword" class="login"
                       method="post">
                <input type="hidden" id="type" name="otpType" value="email"/>
                <form:errors path="*" class="notification notification-error with-Icon icon-error" element="div"
                             htmlEscape="false"/>

                <fieldset>
                    <div class="fm-Fields">
                        <spring:message code='auth.ui.inputhelp.onetimecode'
                                        text='Skriv inn pinkode' var="pincodeHelpText"/>

                        <div class="fm-Field${not empty otpCodeHasError ? ' error' : ''}">
                            <label for="otpCode"><spring:message code="auth.ui.prompt.otc.email"
                                                                 text="Kode fra Email"/></label>
                            <form:input tabindex="1"
                                        autofocus="autofocus"
                                        maxlength="5"
                                        path="otpCode"
                                        type="tel"
                                        id="otpCode"
                                        placeholder="${pincodeHelpText}"
                                        value="${otpCode}"
                                        autocomplete="off"/>
                        </div>

                    </div>
                </fieldset>
                <jsp:include page="sections/controls.jsp"/>
            </form:form>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>