#params.pp
class minidplus_authentication::params {
  $java_home                        = hiera('platform::java_home')
  $log_root                         = '/var/log/'
  $log_level                        = 'WARN'
  $install_dir                      = '/opt/'
  $config_dir                       = '/etc/opt/'
  $group_id                         = 'no.idporten'
  $artifact_id                      = 'minidplus-web-authentication'
  $service_name                     = 'minidplus-authentication'
  $server_port                      = ''
  $linkmobility_url                 = hiera('common::pswincom_url')
  $linkmobility_account             = hiera('common::pswincom_account')
  $linkmobility_password            = hiera('common::pswincom_password')
  $linkmobility_connect_timeout     = 5000
  $linkmobility_read_timeout        = 10000
  $linkmobility_sender              = 'Digdir'
  $linkmobility_ttl                 = 600
  $application                      = 'minidplus-authentication'
  $server_tomcat_max_threads        = 200
  $server_tomcat_min_spare_threads  = 10
  $health_show_details              = 'always'
  $auditlog_dir                     = '/var/log/minidplus-authentication/audit/'
  $auditlog_file                    = 'audit.log'
  $tomcat_tmp_dir                   = '/opt/minidplus-authentication/tmp'
  $token_lifetime_seconds           = 600
  $ldap_url                         = hiera('idporten_opensso_opendj::url')
  $ldap_userdn                      = hiera('idporten_opensso_opendj::dn')
  $ldap_password                    = hiera('idporten_opensso_opendj::password')
  $ldap_base_minid                  = hiera('idporten_opensso_opendj::minid_base')
  $eventlog_jms_queuename           = hiera('idporten_logwriter::jms_queueName')
  $eventlog_jms_url                 = hiera('platform::jms_url')
  $email_url                        = join([hiera('kontaktinfo_backend::url'), 'rest/notifyUser/'])
  $email_connect_timeout            = 5000
  $email_read_timeout               = 10000
  $feature_security_level_check     = false
}
