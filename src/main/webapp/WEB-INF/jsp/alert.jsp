<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="sections/header.jsp"/>

<jsp:include page="sections/minidplusheader.jsp"/>

<main id="minidpluss-main">
    <section class="Box Box-noBorder">
        <div class="Box_main">
            <div class="notification with-Icon">
                <h1><spring:message code="no.idporten.warning.header"/></h1>
                <spring:message code="${alertMsg}" arguments="${linkValue}"/>
            </div>
        </div>
    </section>
</main>
<jsp:include page="sections/footer.jsp"/>

