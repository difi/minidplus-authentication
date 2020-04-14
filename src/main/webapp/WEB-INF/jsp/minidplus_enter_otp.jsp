<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="sections/header.jsp" />

<jsp:include page="sections/minidplusheader.jsp"></jsp:include>
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
            <form id="complete" action="#" class="login js-makeProgress-2" method="post">
                    <fieldset>
                        <div class="fm-Fields">
                            <c:set var ="errorPinCode" scope = "session" value='<%= request.getSession().getAttribute("idporten.input.PIN_CODE") %>'/>
                            <div class="fm-Field ${errorPinCode != null ? ' error' : ''}">
                                <label for="idporten.input.PIN_CODE"><spring:message code="auth.ui.prompt.otc" text="Kode fra SMS"/></label>
                                <input  tabindex="1"
                                        maxlength="5"
                                        name="idporten.input.PIN_CODE"
                                        type="tel"
                                        id="idporten.input.PIN_CODE"
                                        placeholder="<spring:message code="auth.ui.inputhelp.onetimecode" text="(5 siffer)"/>"
                                        value="${requestScope.personalIdNumber}"
                                        autocomplete="off" />
                            </div>

                        </div>
                    </fieldset>
                <jsp:include page="sections/controls.jsp"></jsp:include>
                </form>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"></jsp:include>