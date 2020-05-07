package no.idporten.minidplus.service;

import lombok.RequiredArgsConstructor;
import no.idporten.domain.auth.IDPortenSAMLAttributeVersion;
import no.idporten.domain.sp.EidasSupport;
import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceproviderService {

    private final LdapTemplate ldapTemplate;

    private final ServiceProviderContextMapper contextMapper= new ServiceProviderContextMapper();

    /**
     * Retrieves the service providers associated with the given filter.
     * @param entityIdFilter a string for filtering the selection based on entity id using wildcards (*)
     */
    @Cacheable(cacheNames = "spCache", unless = "#result == null")
    public List<ServiceProvider> findByEntityIdFilter(final String entityIdFilter) {
        final AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "idporten-serviceprovider"));
        andFilter.and(new LikeFilter(LdapAttribute.ENTITYID.getName(), '*' + entityIdFilter + '*'));
        final List<ServiceProvider> list = ldapTemplate.search(LdapUtils.emptyLdapName(), andFilter.encode(), contextMapper);
        Collections.sort(list, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        return list;
    }

    /**
     * Retrieves all service providers from LDAP.
     *
     * @return service providers
     */
    @Cacheable(cacheNames="spCache", unless = "#result == null")
    public List<ServiceProvider> findAll() {
        final AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "idporten-serviceprovider"));
        final List<ServiceProvider> list = ldapTemplate.search(LdapUtils.emptyLdapName(), andFilter.encode(), contextMapper);
        Collections.sort(list, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        return list;
    }

    static class ServiceProviderContextMapper extends AbstractContextMapper<ServiceProvider> {

        @Override
        protected ServiceProvider doMapFromContext(final DirContextOperations ctx) {
            final ServiceProvider sp = new ServiceProvider(ctx.getStringAttribute(LdapAttribute.ENTITYID.getName()));
            sp.setName(ctx.getStringAttribute(LdapAttribute.NAME.getName()));
            sp.setDescription(ctx.getStringAttribute(LdapAttribute.DESCRIPTION.getName()));
            sp.setLogoPath(ctx.getStringAttribute(LdapAttribute.LOGO.getName()));
            sp.setUrl(ctx.getStringAttribute(LdapAttribute.URL.getName()));
            sp.setCreated(DateUtils.parseLocalDateTime(ctx.getStringAttribute(LdapAttribute.CREATED.getName())));
            sp.setUpdated(DateUtils.parseLocalDateTime(ctx.getStringAttribute(LdapAttribute.UPDATED.getName())));
            sp.setOnBehalfOfRequired(Boolean.valueOf(ctx.getStringAttribute(LdapAttribute.ONBEHALFOF_REQ.getName())));
            final String[] refServiceProviders = ctx.getStringAttributes(LdapAttribute.REF_SP.getName());
            if (refServiceProviders != null && refServiceProviders.length > 0) {
                final List<String> refSpList = Arrays.asList(refServiceProviders);
                sp.setReferringServiceProviders(refSpList);
            }
            sp.setDigitalcontactName(ctx.getStringAttribute(LdapAttribute.DIGITALCONTACT_NAME.getName()));
            sp.setIdpMetaData(ctx.getStringAttribute(LdapAttribute.IDP_METADATA.getName()));
            sp.setAlternativeLoginUrl(ctx.getStringAttribute(LdapAttribute.ALTERNATIVE_LOGIN_URL.getName()));
            sp.setAlternativeLoginLogo(ctx.getStringAttribute(LdapAttribute.ALTERNATIVE_LOGIN_LOGO.getName()));
            sp.setAlternativeTextNb(ctx.getStringAttribute(LdapAttribute.ALTERNATIVE_TEXT_NB.getName()));
            sp.setAlternativeTextNn(ctx.getStringAttribute(LdapAttribute.ALTERNATIVE_TEXT_NN.getName()));
            sp.setAlternativeTextEn(ctx.getStringAttribute(LdapAttribute.ALTERNATIVE_TEXT_EN.getName()));
            sp.setAlternativeTextSe(ctx.getStringAttribute(LdapAttribute.ALTERNATIVE_TEXT_SE.getName()));
            sp.setIdportenSamlAttributeVersion(IDPortenSAMLAttributeVersion.resolve(ctx.getStringAttribute(LdapAttribute.IDPORTEN_SAML_ATTRIBUTE_VERSION.getName())));
            sp.setEidasSupport(EidasSupport.resolve(ctx.getStringAttribute(LdapAttribute.EIDAS_SUPPORT.getName())));
            sp.setTurnOffDpiInformation(Boolean.valueOf(ctx.getStringAttribute(LdapAttribute.TURN_OFF_DPI_INFORMATION.getName())));
            sp.setOrgNumber(ctx.getStringAttribute(LdapAttribute.ORG_NUMBER.getName()));
            sp.setSupplierOrgNumber(ctx.getStringAttribute(LdapAttribute.SUPPLIER_ORG_NUMBER.getName()));
            sp.setActive(getBoolean(ctx.getStringAttribute(LdapAttribute.ACTIVE.getName()), true));
            return sp;
        }

        private static boolean getBoolean(final String value, final boolean defaultIfEmpty) {
            if (StringUtils.isEmpty(value)) {
                return defaultIfEmpty;
            }
            return Boolean.valueOf(value);
        }

    }

    /** LDAP attributes for a service provider. */
    enum LdapAttribute {

        ENTITYID("serviceprovider-entityid"),
        NAME("serviceprovider-name"),
        DESCRIPTION("serviceprovider-description"),
        LOGO("serviceprovider-logo"),
        URL("serviceprovider-url"),
        CREATED("serviceprovider-created-date"),
        UPDATED("serviceprovider-updated-date"),
        ONBEHALFOF_REQ("onbehalfof-required"),
        REF_SP("referring-serviceprovider"),
        DIGITALCONTACT_NAME("digitalcontact-name"),
        IDP_METADATA("idp-metadata"),
        ALTERNATIVE_LOGIN_URL("serviceprovider-alternative-login-url"),
        ALTERNATIVE_LOGIN_LOGO("serviceprovider-alternative-login-logo"),
        ALTERNATIVE_TEXT_NB("serviceprovider-alternative-text-nb"),
        ALTERNATIVE_TEXT_NN("serviceprovider-alternative-text-nn"),
        ALTERNATIVE_TEXT_EN("serviceprovider-alternative-text-en"),
        ALTERNATIVE_TEXT_SE("serviceprovider-alternative-text-se"),
        IDPORTEN_SAML_ATTRIBUTE_VERSION("idporten-saml-attribute-version"),
        EIDAS_SUPPORT("eidas-support"),
        TURN_OFF_DPI_INFORMATION("turn-off-dpi-information"),
        ORG_NUMBER("serviceprovider-org-number"),
        SUPPLIER_ORG_NUMBER("serviceprovider-supplier-org-number"),
        ACTIVE("serviceprovider-active");

        private final String name;

        LdapAttribute(final String name) {
            this.name= name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
