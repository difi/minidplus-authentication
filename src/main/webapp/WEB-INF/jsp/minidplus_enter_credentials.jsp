<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

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

            <c:set var ="warningCode" scope = "session" value='<%= request.getSession().getAttribute("idporten.feedback.WARNING") %>'/>

            <c:if test="${warningCode != null}">
                <div class="notification notification-error with-Icon icon-error">
                    <spring:message code="${warningCode}" text="Feil input"/>
                </div>
            </c:if>
            <form action="#" class="login js-makeProgress-1" method="post">
                    <fieldset>
                        <div class="fm-Fields">
                            <c:set var ="errorIdNumber" scope = "session" value='<%= request.getSession().getAttribute("idporten.input.PERSONAL_ID_NUMBER") %>'/>
                            <div class="fm-Field ${errorIdNumber != null ? ' error' : ''}">
                                <label for="idporten.input.PERSONAL_ID_NUMBER"><spring:message code="no.idporten.module.minidplus.input.personalidnumber" text="Personnummer"/></label>
                                <input  tabindex="1"
                                        maxlength="11"
                                        name="idporten.input.PERSONAL_ID_NUMBER"
                                        type="tel"
                                        id="idporten.input.PERSONAL_ID_NUMBER"
                                        placeholder="<spring:message code="no.idporten.module.minidplus.input.personalidnumber.help" text="(11 siffer)"/>"
                                        value="${requestScope.personalIdNumber}"
                                        autocomplete="off" />
                            </div>
                            <div class='fm-Field'>
                                <label for="idporten.input.PASSWORD"><spring:message code="no.idporten.module.minidplus.input.password" text="Passord"/></label>
                                <input  tabindex="2"
                                        maxlength="100"
                                        name="idporten.input.PASSWORD"
                                        type="password"
                                        id="idporten.input.PASSWORD"
                                        placeholder="<spring:message code="no.idporten.module.minidplus.input.password.help" text="Skriv inn passord"/>"
                                        value="${requestScope.password}"
                                        autocomplete="off" />
                            </div>
                            <div class='fm-form_link with-Link'>
                                <a href='#'><span><spring:message code="no.idporten.module.minid.settings.menu.fgtpwd" text="Glemt passord?"/></span></a>
                            </div>
                        </div>
                    </fieldset>
                <jsp:include page="sections/controls.jsp"/>
                </form>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>