<%@ page import="no.idporten.minidplus.domain.MinidPlusSessionAttributes" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!-- SECTION: FOOTER -->
<footer class="f-Main">
    <div class='f-Main_Content'>
        <div class='f-Main_Logo' aria-hidden="true"></div>
        <div class='f-Main_Info'>
            <a href="/opensso/support.jsp?service=<%= MinidPlusSessionAttributes.SERVICE_NAME%>"
               class='f-Main_Link'><span><spring:message code="no.idporten.module.common.footer.contactlinktext"
                                                         text="Contact form"/></span></a>
            <a href="tel:+4780030300" class='f-Main_Link'><span><spring:message code="no.idporten.module.common.footer.telephone" text="Tel: 800 30 300"/></span></a>
            <a href="<spring:message code='no.idporten.common.faq.link'/>" class='f-Main_Link'><span><spring:message
                    code="no.idporten.module.common.footer.commonquestions" text="Help to log in"/></span></a>
            <a href="<spring:message code='no.idporten.common.abouteid.link'/>"
               class='f-Main_Link'><span><spring:message code="no.idporten.module.common.footer.onieid"
                                                         text="Security and privacy"/></span></a>
            <p><spring:message code="no.idporten.module.common.footer.by" text="Operated by"/> <spring:message code="no.idporten.module.common.footer.digdir" text="the Norwegian Digitalisation Agency"/></p>
        </div>
    </div>
</footer>

<!-- /SECTION: FOOTER -->

<script type="text/javascript" src="js/vendor.min.js"></script>
<script type="text/javascript" src="js/header.js"></script>

<script src="js/jquery-3.5.1.slim.min.js">
</script>

</body>
