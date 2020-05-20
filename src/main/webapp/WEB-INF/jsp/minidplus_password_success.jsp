<%@ page import="no.idporten.minidplus.util.MinIdPlusButtonType" %>
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
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot active"></div>
            </div>

            <div class='receipt with-Link'>
                <h2><spring:message code="no.idporten.forgottenpassword.success"
                                    text="Your password has been changed"/></h2>

            </div>

            <form:form action="#" class="login" method="post">
                <fieldset>
                    <input type="hidden" id="type" name="success" value="true"/>
                    <div class='fm-Controls with-Action with-Action-centered'>
                        <button autofocus="autofocus" type="submit" class='btn btn-Normal' tabindex="11"
                                id="<%= MinIdPlusButtonType.NEXT.id() %>"
                                name="<%= MinIdPlusButtonType.NEXT.id() %>">
                            <span><spring:message code="no.idporten.button.continue"/></span>
                        </button>
                    </div>
                </fieldset>
            </form:form>

        </div>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>