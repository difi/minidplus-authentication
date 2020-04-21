package no.idporten.minidplus.config;

import no.minid.ldap.dao.MinidDao;
import no.minid.ldap.dao.MinidLdapDaoImpl;
import no.minid.service.MinIDService;
import no.minid.service.impl.MinIDServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class MinidConfig {

    @Bean
    @Autowired
    public MinIDService minIDService(LdapConfig ldapConfig) {
        MinIDServiceImpl minIDService = new MinIDServiceImpl();
        minIDService.setMinidDao(minidDao(ldapContextSource(ldapConfig)));
        return minIDService;
    }

    private MinidDao minidDao(ContextSource contextSource) {
        MinidLdapDaoImpl minidLdapDao = new MinidLdapDaoImpl();
        minidLdapDao.setContextSource(contextSource);
        return minidLdapDao;
    }

    private ContextSource ldapContextSource(LdapConfig ldapConfig) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapConfig.getUrls());
        contextSource.setBase(ldapConfig.getBase());
        contextSource.setUserDn(ldapConfig.getUsername());
        contextSource.setPassword(ldapConfig.getPassword());
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

}
