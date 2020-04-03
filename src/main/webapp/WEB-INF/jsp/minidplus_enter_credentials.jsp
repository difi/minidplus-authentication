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
            <div>
                <img  src="<c:url value='/images/corona.jpg' />" alt="image" />
            </div>
            <span style="color: brown">Velkommen //todo fjern dette og bildet over :P </span>
            <form action="#" class="login" method="post">
                    <fieldset>
                        <div class="fm-Fields">
                            <c:set var ="errorSocialSecurityNumber" scope = "session" value='<%= request.getSession().getAttribute("idporten.input.SOCIAL_SECURITY_NUMBER") %>'/>
                            <div class="fm-Field ${errorBirthDate != null ? ' error' : ''}">
                                <label for="idporten.input.SOCIAL_SECURITY_NUMBER"><spring:message code="no.idporten.module.minidplus.input.socialsecuritynumber" text="Personnummer"/></label>
                                <input  tabindex="2"
                                        maxlength="11"
                                        name="idporten.input.SOCIAL_SECURITY_NUMBER"
                                        type="tel"
                                        id="idporten.input.SOCIAL_SECURITY_NUMBER"
                                        placeholder="<spring:message code="no.idporten.module.minidplus.input.socialsecuritynumber.help" text="(11 siffer)"/>"
                                        value="${requestScope.socialSecurityNumber}"
                                        autocomplete="off" />
                            </div>

                        </div>
                        <div class="fm-Controls with-Normal with-Action">
                            <button name="idporten.inputbutton.NEXT" id="nextbtn" tabindex="10" class="btn btn-Action" type="submit">
                                <span><spring:message code="no.idporten.button.next" text="Neste"/></span>
                            </button>
                            <button name="idporten.inputbutton.CANCEL" id="cancelButton" tabindex="11" class="btn btn-Normal">
                                <span><spring:message code="auth.ui.button.cancel" text="Avbryt"/></span>
                            </button>
                        </div>
                    </fieldset>
                </form>

        </div>
    </section>
</main>

