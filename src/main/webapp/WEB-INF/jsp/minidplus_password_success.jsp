<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

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
                <p><spring:message code="no.idporten.forgottenpassword.success.info1"/> <a
                        href="/minprofil"><span><spring:message
                        code="no.idporten.forgottenpassword.success.info2"/></span></a></p>

            </div>
            <!-- //todo må lede en plass PBLEID-20245 Controls placeholder with centered action button only. used in receipts -->
            <div class='fm-Controls with-Action with-Action-centered'>
                <button class='btn btn-Action' type='submit'><span><spring:message
                        code="no.idporten.button.continue"/></span></button>
            </div>
            <!-- //Controls placeholder with action button only -->

        </div>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"/>