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
  $application                      = 'minidplus-authentication'
  $server_tomcat_max_threads        = 200
  $server_tomcat_min_spare_threads  = 10
  $health_show_details              = 'always'
  $auditlog_dir                     = '/var/log/minidplus-authentication/audit/'
  $auditlog_file                    = 'audit.log'
  $tomcat_tmp_dir                   = '/opt/minidplus-authentication/tmp'
}
