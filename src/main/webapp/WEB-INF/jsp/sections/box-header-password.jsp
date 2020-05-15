<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!-- NOTE: provider is added twice: above box title (this one) and below box.
The one above title is displayed on desktop only. the one below on mobile only. -->

<div class='Box_Section Box_Section-ServiceProvider'>
    <div class='Box_Section_Title'><spring:message text="${serviceprovider.name}"/></div>
    <!-- <img src="/opensso/images/${serviceprovider.logoPath}" alt='${serviceprovider.name}' />-->
</div>
<div class='Box_header'>
    <h1 class='Box_header-title with-logo logo-eid-gray'><spring:message code="auth.ui.fp.user.caption"
                                                                         text="Reset Passord"/></h1>
    <div class="Box_header-provider"><img src="../images/svg/minid-m-kant.svg" alt="MinID logo"></div>
</div>
