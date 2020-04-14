<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="sections/header.jsp" />

<jsp:include page="sections/minidplusheader.jsp"></jsp:include>
<main id="minidplus-main">
    <section class="Box">
        <jsp:include page="sections/box-header.jsp" />
        <div class="Box_main" id="minidpluswrapper">
            <h1>SUCCESS!</h1>
            <h2>But not really. A placeholder for redirect tilbake til tjenestetilbyder</h2>
            <div>
                <img  src="<c:url value='/images/corona.jpg' />" alt="image" />
            </div>

        </div>
    </section>
</main>

<jsp:include page="sections/footer.jsp"></jsp:include>