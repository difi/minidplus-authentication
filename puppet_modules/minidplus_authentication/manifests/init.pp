class minidplus_authentication (
  String $log_root                                 = $minidplus_authentication::params::log_root,
  String $log_level                                = $minidplus_authentication::params::log_level,
  String $install_dir                              = $minidplus_authentication::params::install_dir,
  String $config_dir                               = $minidplus_authentication::params::config_dir,
  String $group_id                                 = $minidplus_authentication::params::group_id,
  $artifact_id                              = $minidplus_authentication::params::artifact_id,
  $service_name                             = $minidplus_authentication::params::service_name,
  $server_port                              = $minidplus_authentication::params::server_port,
  $application                              = $minidplus_authentication::params::application,
  $server_tomcat_max_threads                = $minidplus_authentication::params::server_tomcat_max_threads,
  $server_tomcat_min_spare_threads          = $minidplus_authentication::params::server_tomcat_min_spare_threads,
  $health_show_details                      = $minidplus_authentication::params::health_show_details,
  $auditlog_dir                             = $minidplus_authentication::params::auditlog_dir,
  $auditlog_file                            = $minidplus_authentication::params::auditlog_file,
  $tomcat_tmp_dir                           = $minidplus_authentication::params::tomcat_tmp_dir,

)inherits minidplus_authentication::params {

  include platform

  anchor { 'minidplus_authentication::begin': } ->
  class { '::minidplus_authentication::install': } ->
  class { '::minidplus_authentication::deploy': } ->
  class { '::minidplus_authentication::config': } ~>
  class { '::minidplus_authentication::service': } ->
  anchor { 'minidplus_authentication::end': }

}
