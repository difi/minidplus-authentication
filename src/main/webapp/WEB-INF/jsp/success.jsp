
<jsp:include page="sections/header.jsp" />

<jsp:include page="sections/minidplusheader.jsp"></jsp:include>
<main id="minidplus-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp" />
        <div class="Box_main" id="minidpluswrapper">
            <h1>SUCCESS!</h1>
            <h2>En placeholder for redirect tilbake til idporten</h2>
            <h3>Code = <%=session.getAttribute("sid")%>
            </h3>
            <div>
                <img  src="images/corona.jpg" alt="image" />
            </div>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"></jsp:include>