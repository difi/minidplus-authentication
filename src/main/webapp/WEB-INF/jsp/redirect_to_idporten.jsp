<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="sections/header.jsp"/>

<jsp:include page="sections/minidplusheader.jsp"/>

<body id="body" onload="javascript:document.forms[0].submit()" >
<main id="minidplus-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp"/>
        <div class="Box_main" id="minidpluswrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot active"></div>
            </div>
            <div class="notification with-Normal">
                <p><spring:message code="no.idporten.module.minidplus.backtoidporten"/></p>
            </div>
            <form:form id="complete" action="${redirectUrl}" class="login"
                       method="post">

            </form:form>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>