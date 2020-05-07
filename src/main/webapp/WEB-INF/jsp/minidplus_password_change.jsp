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
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot active"></div>
                <div class="fm-Progress_Dot"></div>
            </div>

            <div class="notification">
                <p><spring:message code="auth.ui.fp.newpwd.tip.paragraph1"/></p>
                <p><spring:message code="auth.ui.fp.newpwd.tip.paragraph2"/></p>
            </div>

            <form:form id="complete" action="#" modelAttribute="passwordChange" class="login"
                       method="post">
                <form:errors path="*" class="notification notification-error" element="div"
                             htmlEscape="false"/>
                <c:set var="otpCodeHasError">
                    <form:errors path="*"/>
                </c:set>
                <fieldset>
                    <div class="fm-Fields">
                        <spring:message code='auth.ui.prompt.newpassword'
                                        text='Enter new password' var="passwordHelpText"/>

                        <spring:message code='auth.ui.prompt.repeat.newpassword'
                                        text='Re-enter password' var="reenterPasswordHelpText"/>

                        <div class="fm-Field${not empty otpCodeHasError ? ' error' : ''}">
                            <label for="newPassword"><spring:message code="auth.ui.prompt.newpassword"
                                                                     text="Create password"/></label>
                            <form:input tabindex="1"
                                        autofocus="autofocus"
                                        maxlength="100"
                                        path="newPassword"
                                        type="password"
                                        id="newPassword"
                                        placeholder="${passwordHelpText}"
                                        value="${newPassword}"
                                        autocomplete="off"/>

                            <label for="reenterPassword"><spring:message code="auth.ui.prompt.repeat.newpassword"
                                                                         text="Re-enter password"/></label>
                            <form:input tabindex="2"
                                        maxlength="100"
                                        path="reenterPassword"
                                        type="password"
                                        id="reenterPassword"
                                        placeholder="${reenterPasswordHelpText}"
                                        value="${reenterPassword}"
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