class minidplus_authentication (
  String $log_root                         = $minidplus_authentication::params::log_root,
  String $log_level                        = $minidplus_authentication::params::log_level,
  String $install_dir                      = $minidplus_authentication::params::install_dir,
  String $config_dir                       = $minidplus_authentication::params::config_dir,
  String $group_id                         = $minidplus_authentication::params::group_id,
  String $artifact_id                      = $minidplus_authentication::params::artifact_id,
  String $service_name                     = $minidplus_authentication::params::service_name,
  String $server_port                      = $minidplus_authentication::params::server_port,
  String $application                      = $minidplus_authentication::params::application,
  Integer $server_tomcat_max_threads       = $minidplus_authentication::params::server_tomcat_max_threads,
  Integer $server_tomcat_min_spare_threads = $minidplus_authentication::params::server_tomcat_min_spare_threads,
  String $health_show_details              = $minidplus_authentication::params::health_show_details,
  String $auditlog_dir                     = $minidplus_authentication::params::auditlog_dir,
  String $auditlog_file                    = $minidplus_authentication::params::auditlog_file,
  String $tomcat_tmp_dir                   = $minidplus_authentication::params::tomcat_tmp_dir,
  String $linkmobility_url                 = $minidplus_authentication::params::linkmobility_url,
  String $linkmobility_account             = $minidplus_authentication::params::linkmobility_account,
  String $linkmobility_password            = $minidplus_authentication::params::linkmobility_password,
  Integer $linkmobility_connect_timeout    = $minidplus_authentication::params::linkmobility_connect_timeout,
  Integer $linkmobility_read_timeout       = $minidplus_authentication::params::linkmobility_read_timeout,
  String $linkmobility_sender              = $minidplus_authentication::params::linkmobility_sender,
  Integer $token_lifetime_seconds          = $minidplus_authentication::params::token_lifetime_seconds,
  Integer $code_lifetime_seconds           = $minidplus_authentication::params::code_lifetime_seconds,
  String $ldap_url                         = $minidplus_authentication::params::ldap_url,
  String $ldap_userdn                      = $minidplus_authentication::params::ldap_userdn,
  String $ldap_password                    = $minidplus_authentication::params::ldap_password,
  String $ldap_base_minid                  = $minidplus_authentication::params::ldap_base_minid,
  String $eventlog_jms_queuename           = $minidplus_authentication::params::eventlog_jms_queuename,
  String $eventlog_jms_url                 = $minidplus_authentication::params::eventlog_jms_url,
  String $email_url                        = $minidplus_authentication::params::email_url,
  Integer $email_port                      = $minidplus_authentication::params::email_port,
  String $email_from_address               = $minidplus_authentication::params::email_from_address,
  Boolean $email_starttls_enabled          = $minidplus_authentication::params::email_starttls_enabled,
  Integer $email_connect_timeout           = $minidplus_authentication::params::email_connect_timeout,
  Integer $email_read_timeout              = $minidplus_authentication::params::email_read_timeout,
  String $email_allowed_filter             = $minidplus_authentication::params::email_allowed_filter,
  Boolean $email_debug_enabled             = $minidplus_authentication::params::email_debug_enabled,
  Integer $sms_onetimepassword_ttl_seconds = $minidplus_authentication::params::sms_onetimepassword_ttl_seconds,
  String $minidonthefly_registrationUri    = $minidplus_authentication::params::minidonthefly_registrationUri,
  String $basic_auth_client_id             = $minidplus_authentication::params::basic_auth_client_id,
  String $basic_auth_client_secret         = $minidplus_authentication::params::basic_auth_client_secret,
  Boolean $sms_filter_enabled              = $minidplus_authentication::params::sms_filter_enabled,
  String $sms_filter_filename              = $minidplus_authentication::params::sms_filter_filename,
  Integer $session_ttl_seconds             = $minidplus_authentication::params::session_ttl_seconds,
  Boolean $callback_method_post            = $minidplus_authentication::params::callback_method_post,
  Boolean $openam_client_id                = $minidplus_authentication::params::openam_client_id,
  Boolean $openam_client_secret            = $minidplus_authentication::params::openam_client_secret,
  Boolean $openam_redirect_uri             = $minidplus_authentication::params::openam_redirect_uri

) inherits minidplus_authentication::params {

  include platform

  anchor { 'minidplus_authentication::begin': } ->
  class { '::minidplus_authentication::install': } ->
  class { '::minidplus_authentication::deploy': } ->
  class { '::minidplus_authentication::config': } ~>
  class { '::minidplus_authentication::service': } ->
  anchor { 'minidplus_authentication::end': }

}
