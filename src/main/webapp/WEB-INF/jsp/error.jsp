
<jsp:include page="sections/header.jsp" />

<main id="minidpluss-main">
    <section class="Box">
        <div id="minidplusswrapper">
            <div class="notification notification-error with-Icon icon-error">
                <spring:message code="no.idporten.module.minidplus.error.text1"/>
                <br><spring:message code="no.idporten.module.minidplus.error.text2"/>
                <c:if test="${not empty errorCode}">
                    <br><spring:message code="no.idporten.module.minidplus.error.text3"/> <c:out value="${errorCode}" escapeXml="true"/>
                </c:if>
            </div>

            <form method="post">
                <fieldset>
                    <div class="fm-Controls with-Action">
                        <button name="idporten.inputbutton.CLOSE" id="closeButton" tabindex="10" class="btn btn-Action">
                            <span><spring:message code="auth.ui.button.retry" text="Prøv på nytt"/></span>
                        </button>
                    </div>
                </fieldset>
            </form>
        </div>
    </section>
</main>

