<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--[if lt IE 11]>
<p class="browsehappy">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade
    your browser</a> to improve your experience.</p>
<![endif]-->
<!-- SECTION: HEADER -->

<header class='h-Main'>

    <div class='h-Main_Content'>
        <c:if test="${sessionScope.serviceprovider !=null }">
            <a href="${sessionScope.serviceprovider.url}" class="h_Main_Content_Link">
            <span class="fa fa-angle-left fa-lg" aria-hidden="true"></span>
            <span><spring:message code="no.idporten.button.back" text="Tilbake"/></span>
        </a>
        </c:if>
        <!-- NOTE: placholder class must be present, even if it has no content -->
        <div class="h-Main_Content_Placeholder">
            <!-- NOTE: Present on pages with service provider name and logo. Visible on mobile sreens only. -->
            <c:if test="${sessionScope.serviceprovider !=null }">
                <span class="h-Main_Content_Provider"><spring:message
                        text="${sessionScope.serviceprovider.name}"/></span>
            </c:if>
        </div>
    </div>

</header>

<!-- /SECTION: HEADER -->