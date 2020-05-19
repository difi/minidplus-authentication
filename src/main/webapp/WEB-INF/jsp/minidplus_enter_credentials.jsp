<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="sections/header.jsp"/>

<jsp:include page="sections/minidplusheader.jsp"/>

<main id="minidplus-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp"/>
        <div class="Box_main" id="minidpluswrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot active"></div>
                <div class="fm-Progress_Dot"></div>
            </div>

            <c:set var="hasErrors">
                <form:errors path="*"/>
            </c:set>

            <form:form action="authorize" class="login" modelAttribute="userCredentials" method="post">
                <form:errors path="*" class="notification notification-error with-Icon icon-error" element="div" htmlEscape="false"/>

                <fieldset>
                    <div class="fm-Fields">
                        <div class="fm-Field${not empty hasErrors ? ' error' : ''}">
                            <spring:message code='no.idporten.module.minidplus.input.personalidnumber.help'
                                            text='(11 siffer)' var="personalIdNumberHelpText"/>
                            <label for="personalIdNumber"><spring:message
                                    code="no.idporten.module.minidplus.input.personalidnumber"
                                    text="Personnummer"/></label>
                            <form:input tabindex="1"
                                        autofocus="autofocus"
                                        maxlength="11"
                                        path="personalIdNumber"
                                        type="tel"
                                        id="personalIdNumber"
                                        placeholder="${personalIdNumberHelpText}"
                                        autocomplete="off"/>
                        </div>

                        <div class="fm-Field${not empty hasErrors  ? ' error' : ''}">
                            <spring:message code="no.idporten.module.minidplus.input.password.help"
                                            text="Skriv inn passord" var="passwordHelpText"/>
                            <label for="password"><spring:message code="no.idporten.module.minidplus.input.password"
                                                                  text="Passord"/></label>
                            <form:input tabindex="2"
                                        maxlength="100"
                                        path="password"
                                        type="password"
                                        id="password"
                                        placeholder="${passwordHelpText}"
                                        autocomplete="off"/>

                        </div>

                        <div class='fm-form_link with-Link'>
                            <a href="<c:url value='password?locale=${sessionScope.locale}'/>">
                                <span><spring:message
                                        code="no.idporten.module.minid.settings.menu.fgtpwd"
                                        text="Glemt passord"/></span></a>
                        </div>
                    </div>
                </fieldset>
                <jsp:include page="sections/controls.jsp"/>
                <jsp:include page="sections/registerFooter.jsp"/>
            </form:form>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>